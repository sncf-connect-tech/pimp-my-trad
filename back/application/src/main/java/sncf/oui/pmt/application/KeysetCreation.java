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

package sncf.oui.pmt.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.DomainDrivenDesign;
import sncf.oui.pmt.domain.keyset.KeysetInput;
import sncf.oui.pmt.domain.keyset.KeysetMetadata;
import sncf.oui.pmt.domain.keyset.KeysetMetadataFactory;
import sncf.oui.pmt.domain.project.ProjectMetadataRepository;

@DomainDrivenDesign.ApplicationService
@Component
public class KeysetCreation {

    private ProjectMetadataRepository projectMetadataRepository;
    private KeysetMetadataFactory factory;

    @Autowired
    public KeysetCreation(ProjectMetadataRepository projectMetadataRepository, KeysetMetadataFactory factory) {
        this.projectMetadataRepository = projectMetadataRepository;
        this.factory = factory;
    }

    public Mono<KeysetMetadata> create(String projectName, KeysetInput input) {
        return projectMetadataRepository.findByName(projectName)
                .flatMap(projectMetadata -> {
                    KeysetMetadata keysetMetadata = factory
                            .createNew(input, projectMetadata.getProjectDir());
                    projectMetadata.addKeyset(keysetMetadata);
                    return projectMetadataRepository.update(projectMetadata)
                            .then(Mono.just(keysetMetadata));
                });
    }
}
