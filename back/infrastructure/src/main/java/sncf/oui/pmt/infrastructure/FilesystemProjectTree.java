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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.project.ProjectMetadata;
import sncf.oui.pmt.domain.ProjectTree;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

@Component
public class FilesystemProjectTree implements ProjectTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemProjectTree.class);

    @Value("${dataroot}")
    private String root;

    private final AuthenticationDetails auth;

    @Autowired
    public FilesystemProjectTree(AuthenticationDetails auth) {
        this.auth = auth;
    }

    private Mono<Path> getAbsolutePath(Path dir) {
        return auth.getUser()
                .map(name -> Paths.get(root, name).resolve(dir));
    }

    @Override
    public Mono<List<String>> list(String dir) {
        return getAbsolutePath(Paths.get(dir))
                .flux()
                .flatMap(rootPath -> {
                    try {
                        return Flux.fromIterable(Files.newDirectoryStream(rootPath))
                                .filter(path -> !path.getFileName().toString().startsWith("."))
                                .map(path -> rootPath.relativize(path).toString() + (path.toFile().isDirectory() ? "/" : ""));
                    } catch (IOException e) {
                        LOGGER.error("Could not open directory", e);
                        return Flux.empty();
                    }
                })
                .collectList();
    }
}
