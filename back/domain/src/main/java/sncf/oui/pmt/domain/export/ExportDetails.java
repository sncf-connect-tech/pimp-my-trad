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

package sncf.oui.pmt.domain.export;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.DomainDrivenDesign;
import sncf.oui.pmt.domain.project.Project;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@DomainDrivenDesign.Entity
public class ExportDetails {

    private ExportMetadata metadata;
    private String exported;

    private ExportDetails(ExportMetadata metadata, String exported) {
        this.metadata = metadata;
        this.exported = exported;
    }

    public static Mono<ExportDetails> forProject(Project project) {
        return forProjects(Collections.singletonList(project));
    }

    public static Mono<ExportDetails> forProjects(List<Project> projects) {
        ExportMetadata metadata = new ExportMetadata();
        return Flux.fromIterable(projects)
                .flatMap(metadata::exportProject)
                .collect(Collectors.joining("\n"))
                .map(s -> ExportMetadata.utfHeader() + s)
                .map(exported -> new ExportDetails(metadata, exported));
    }

    public ExportMetadata getMetadata() {
        return metadata;
    }

    public String getExported() {
        return exported;
    }
}
