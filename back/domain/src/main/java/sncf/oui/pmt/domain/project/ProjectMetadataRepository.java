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

@DomainDrivenDesign.Repository
public interface ProjectMetadataRepository {
    Flux<ProjectMetadata> findAll();
    Mono<Boolean> exists(String name);
    Mono<ProjectMetadata> findByName(String name);
    Mono<ProjectMetadata> findById(String id);
    Mono<ProjectMetadata> save(ProjectMetadata project);
    Mono<ProjectMetadata> update(ProjectMetadata project);
}
