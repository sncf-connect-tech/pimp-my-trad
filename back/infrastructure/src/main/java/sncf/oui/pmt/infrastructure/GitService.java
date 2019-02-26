/*
 *
 *  * Copyright (C) 2018 VSCT
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package sncf.oui.pmt.infrastructure;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.EmtpyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;
import sncf.oui.pmt.domain.CloneException;
import sncf.oui.pmt.domain.CloneService;
import sncf.oui.pmt.domain.ConflictFlag;
import sncf.oui.pmt.domain.ConflictingFileHandle;
import sncf.oui.pmt.domain.SyncService;
import sncf.oui.pmt.domain.project.ProjectMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GitService implements CloneService, SyncService, ConflictingFileHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);
    private CredentialsProvider credentials;
    private ProjectMetadata projectMetadata;

    @Value("${dataroot}")
    private String root;

    private final AuthenticationDetails auth;

    @Autowired
    public GitService(CredentialsProvider credentials, AuthenticationDetails auth) {
        this.credentials = credentials;
        this.auth = auth;
    }

    private Mono<Path> getAbsolutePath(Path dir) {
        return auth.getUser()
                .map(name -> Paths.get(root, name).resolve(dir));
    }

    private Git makeGit(File file) throws IOException {
        Repository repo = new FileRepositoryBuilder()
                .setGitDir(Paths.get(file.toString(), ".git").toFile())
                .build();
        return Git.wrap(repo);
    }

    private int aheadCount(Repository repo, String branchName) throws IOException {
        BranchTrackingStatus status = BranchTrackingStatus.of(repo, branchName);
        return status.getAheadCount();
    }

    private void handleNoConflict(Git git, List<String> files, FluxSink<String> sink) throws GitAPIException, IOException {
        // add files
        AddCommand add = git.add();
        files.forEach(add::addFilepattern);
        add.call();
        // commit (maybe amend)
        try {
            git.commit()
                    .setAllowEmpty(false)
                    .setAmend(aheadCount(git.getRepository(), "develop") > 0)
                    .setMessage("pmt-auto-commit")
                    .call();
        } catch (EmtpyCommitException ignored) {
            LOGGER.info("nothing to commit");
        } finally {
            LOGGER.info("resuming");
            // rebase
            PullResult pull = git.pull()
                    .setCredentialsProvider(credentials)
                    .setRebase(true)
                    .call();
            // push
            boolean success = pull.getRebaseResult() == null || !pull.getRebaseResult().getStatus().equals(RebaseResult.Status.STOPPED);
            LOGGER.info("attempt push: " + (success ? "yes" : "no"));
            if (success) {
                try {
                    git.push().setCredentialsProvider(credentials).call();
                } catch (TransportException ignored) {
                    LOGGER.info("push not needed");
                }
            } else {
                git.status().call().getConflicting().forEach(sink::next);
            }
        }
    }

    private void handleConflict(Git git, Path projectRoot, List<String> files, FluxSink<String> sink) throws GitAPIException {
        AddCommand add = git.add();
        files.forEach(f -> {
            Path tmp = projectRoot.resolve(tmpPathFor(f));
            Path finalPath = projectRoot.resolve(f);
            LOGGER.info("current: " + finalPath.toString());
            if (Files.exists(tmp)) { // changes have been made
                LOGGER.info("-> conflicts are solved for this file, using " + tmp.toString());
                try {
                    Files.move(tmp, finalPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    sink.error(e);
                }
            } else {
                LOGGER.info("-> conflicts not solved or no conflicts, ignoring");
                readConflictLines(finalPath, ConflictMark.SPLIT, ConflictMark.END, ConflictMark.NONE)
                        .collect(Collectors.joining("\n"))
                        .flatMap(s -> write(finalPath.toString(), s))
                        .block();
            }
            LOGGER.info("done, adding pattern " + f);
            add.addFilepattern(f);
        });
        add.call();
        // continue rebasing
        git.rebase().setOperation(RebaseCommand.Operation.CONTINUE).call();
        // push
        git.push().setCredentialsProvider(credentials).call();
    }

    @Override
    public Flux<String> attemptSync() {
        final List<String> files = projectMetadata.listOwnedFiles();
        return getAbsolutePath(Paths.get(projectMetadata.getProjectDir()))
                .flux()
                .flatMap(projectRoot -> Flux.create((FluxSink<String> sink) -> {
                    try (Git git = makeGit(projectRoot.toFile())) {
                        if (git.status().call().getConflicting().isEmpty()) { // no conflict
                            LOGGER.info("no conflict detected");
                            handleNoConflict(git, files, sink);
                        } else {
                            LOGGER.info("handling conflicts");
                            LOGGER.info("files: ");
                            LOGGER.info(String.join(", ", files));
                            handleConflict(git, projectRoot, files, sink);
                        }
                        git.getRepository().close();
                        sink.complete();
                    } catch (GitAPIException | IOException e) {
                        sink.error(e);
                    }
                }).publishOn(Schedulers.single()));

    }

    @Override
    public void setProject(ProjectMetadata projectMetadata) {
        this.projectMetadata = projectMetadata;
    }

    @Override
    public Mono<String> cloneProject() {
        return getAbsolutePath(Paths.get(projectMetadata.getProjectDir()))
                .flatMap(rootPath -> Mono.create(sink -> {
                    try (Git git = Git.cloneRepository()
                            .setURI(projectMetadata.getOrigin())
                            .setCredentialsProvider(credentials)
                            .setDirectory(rootPath.toFile())
                            .setBranch("develop")
                            .call()) {
                        git.getRepository().close();
                        sink.success();
                    } catch (TransportException e) {
                        LOGGER.info("Local clone path: " + rootPath);
                        LOGGER.error("An error occured:", e);
                        sink.error(new CloneException());
                    } catch (GitAPIException e) {
                        LOGGER.error("An error occured:", e);
                    } catch (JGitInternalException e) {
                        LOGGER.warn("Project already cloned");
                        sink.success();
                    } catch (Exception e) {
                        LOGGER.error("Unexpected ", e);
                    }
                })
                        .publishOn(Schedulers.single())
                        .then(Mono.just(rootPath.toString())));
    }

    @Override
    public Mono<Void> removeProject() {
        return getAbsolutePath(Paths.get(projectMetadata.getProjectDir(), ".git"))
                .flatMap(path -> {
                    try {
                        FileUtils.delete(path.toFile(), FileUtils.RECURSIVE);
                        return Mono.empty();
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                });
    }

    private Flux<String> readWithFlag(Path path, ConflictFlag flag) {
        switch (flag) {
            case StrictlyOurs:
                return readConflictLines(path, ConflictMark.SPLIT);
            case Ours:
                return readConflictLines(path, ConflictMark.SPLIT, ConflictMark.END, ConflictMark.NONE);
            case StrictlyTheirs:
                return readConflictLines(path, ConflictMark.START);
            case Theirs:
                return readConflictLines(path, ConflictMark.START, ConflictMark.END, ConflictMark.NONE);
            case Resolved:
                Path tmp = tmpPathFor(path.toString());
                if (Files.exists(tmp)) {
                    return readRaw(tmp);
                } else {
                    return readConflictLines(path, ConflictMark.END, ConflictMark.NONE);
                }
            default:
                return Flux.empty();
        }
    }

    @Override
    public Flux<String> readLines(String file, ConflictFlag flag) {
        return getAbsolutePath(Paths.get(file)).flux()
                .flatMap(path -> hasConflict(path).flux()
                        .flatMap(conflict -> conflict ? readWithFlag(path, flag) : readRaw(path)));
    }

    @Override
    public Mono<Boolean> hasConflict(String path) {
        return hasConflict(Paths.get(path));
    }

    public Mono<Boolean> hasConflict(Path path) {
        return getAbsolutePath(path).map(absolute -> {
            try (Git git = makeGit(absolute.toFile())) {
                boolean res = git.status().call().getConflicting().stream()
                        .peek(s -> LOGGER.info("Conflict: " + s))
                        .anyMatch(path::endsWith);
                git.getRepository().close();
                return res;
            } catch (IOException | GitAPIException | IllegalArgumentException e) {
                LOGGER.error("Unexpected: ", e);
                return false;
            }
        });
    }

    private Path tmpPathFor(String path) {
        int index = path.lastIndexOf(".");
        return Paths.get(path.substring(0, index) + ".tmp" + path.substring(index));
    }

    @Override
    public Mono<Void> write(String f, String content) {
        return getAbsolutePath(Paths.get(f))
                .flatMap(path -> hasConflict(path).map(conflict -> conflict ? tmpPathFor(path.toString()) : path))
                .flatMap(realPath -> Mono.create(sink -> {
                    try {
                        Files.write(realPath, content.getBytes(),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);
                        sink.success();
                    } catch (IOException e) {
                        sink.error(e);
                    }
                }));
    }

    private Flux<String> readRaw(Path path) {
        return Flux.using(() -> Files.lines(path),
                Flux::fromStream,
                BaseStream::close
        );
    }

    private Flux<String> readConflictLines(Path path, ConflictMark... validMarks) {

        Flux<String> lines = readRaw(path).cache();
        Flux<ConflictMark> actualMarks = lines.map(ConflictMark::from);

        return Flux.zip(lines, actualMarks.scan(ConflictMark::scan), actualMarks)
                .filter(tuple -> !tuple.getT3().isPresent())
                .filter(tuple -> Stream.of(validMarks).anyMatch(m -> tuple.getT2().equals(m)))
                .map(Tuple3::getT1);
    }
}

enum ConflictMark {

    START("<<<<<<<"), SPLIT("======="), END(">>>>>>>"), NONE;

    private final Optional<String> value;

    @Override
    public String toString() {
        return value.orElse("(none)");
    }

    private ConflictMark(final String value) {
        this.value = Optional.of(value);
    }

    private ConflictMark() {
        this.value = Optional.empty();
    }

    public static ConflictMark from(String line) {
        return Stream.of(ConflictMark.values())
                .filter(m -> m.match(line))
                .findFirst()
                .orElse(ConflictMark.NONE);
    }

    // opérateur asymétrique sur ConflictMark
    // prev <*> 0 = prev
    // 0 <*> next = next
    // prev <*> next = next
    public static ConflictMark scan(ConflictMark prev, ConflictMark next) {
        return prev.value.isPresent() && !next.value.isPresent() ? prev : next;
    }

    public Boolean isPresent() {
        return value.isPresent();
    }

    private Boolean match(String line) {
        return value.isPresent() && line.startsWith(value.get());
    }
}