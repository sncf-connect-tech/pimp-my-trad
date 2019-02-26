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

package sncf.oui.pmt.domain.project;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.DomainDrivenDesign;
import sncf.oui.pmt.domain.CloneService;
import sncf.oui.pmt.domain.ProjectTree;
import sncf.oui.pmt.domain.SyncService;
import sncf.oui.pmt.domain.keyset.KeysetMetadata;
import sncf.oui.pmt.domain.keyset.KeysetNotFoundException;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DomainDrivenDesign.Entity
@DomainDrivenDesign.AggregateRoot
public class ProjectMetadata {

    private Optional<String> id;
    private List<KeysetMetadata> keysets;
    private String projectName;
    private Optional<String> projectOwner;
    private String dataRoot;

    private String projectDir;
    private String origin;
    private CloneService cloneService;
    private ProjectTree projectTree;
    private SyncService syncService;

    public ProjectMetadata(String id, String origin, String name, String dataRoot, CloneService cloneService, ProjectTree projectTree, SyncService syncService) {
        this.id = Optional.ofNullable(id);
        this.projectName = name;
        this.setupOrigin(origin);
        this.syncService = syncService;
        this.dataRoot = dataRoot;
        this.keysets = new LinkedList<>();
        this.cloneService = cloneService;
        this.projectTree = projectTree;
    }

    public String getProjectName() {
        return projectName;
    }

    public Mono<Project> readProject() {
        Project project = new Project(projectName);
        return cloneOrContinue().flux()
                .flatMap(any -> Flux.fromIterable(keysets))
                .flatMap(KeysetMetadata::readKeyset)
                .reduce(project, Project::addKeyset);
    }

    public Mono<ProjectMetadata> cloneOrContinue() {
        return cloneService.cloneProject()
                .then(Mono.just(this));
    }

    public Mono<ProjectMetadata> delete() {
        return cloneService.removeProject()
                .then(Mono.just(this));
    }

    public Mono<Boolean> attemptSync() {
        return syncService.attemptSync()
                .collectList()
                .map(List::isEmpty)
                .map(b -> !b)
                .onErrorReturn(false);
    }

    public List<String> listOwnedFiles() {
        return getKeysets().stream()
                .flatMap(keysetMetadata -> keysetMetadata.getRelativeFiles().values().stream())
                .map(file -> file.startsWith("/") ? file.substring(1) : file)
                .collect(Collectors.toList());
    }

    public Mono<List<String>> listFiles(String dir) {
        return cloneOrContinue()
                .flatMap(p -> projectTree.list(Paths.get(getProjectDir(), dir).toString()));
    }

    public List<KeysetMetadata> getKeysets() {
        return keysets;
    }

    public KeysetMetadata getKeyset(String id) throws KeysetNotFoundException {
        return keysets.stream()
                .filter(k -> k.getId().equals(id))
                .findFirst()
                .orElseThrow(KeysetNotFoundException::new);
    }

    public ProjectMetadata addKeyset(KeysetMetadata keysetMetadata) {
        keysets.add(keysetMetadata);
        return this;
    }

    public Optional<String> getId() {
        return id;
    }

    public ProjectMetadata setId(String id) {
        this.id = Optional.ofNullable(id);
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public ProjectMetadata setupOrigin(String origin) {
        this.origin = origin;
        this.setProjectDir(this.projectName);
        return this;
    }


    public ProjectMetadata setKeysets(List<KeysetMetadata> keysets) {
        this.keysets = keysets;
        return this;
    }

    public ProjectMetadata setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ProjectMetadata setProjectDir(String projectDir) {
        this.projectDir = projectDir;
        return this;
    }

    public String getProjectDir() {
        return projectDir;
    }

    public Optional<String> getProjectOwner() {
        return projectOwner;
    }

    public ProjectMetadata setProjectOwner(String projectOwner) {
        this.projectOwner = Optional.ofNullable(projectOwner);
        return this;
    }
}
