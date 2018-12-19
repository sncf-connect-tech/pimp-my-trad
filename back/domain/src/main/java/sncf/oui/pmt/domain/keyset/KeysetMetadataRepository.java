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

package sncf.oui.pmt.domain.keyset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.LanguageNotSupportedException;
import sncf.oui.pmt.domain.project.ProjectMetadata;
import sncf.oui.pmt.domain.project.ProjectMetadataRepository;

import java.util.function.Function;

@Component
public class KeysetMetadataRepository {

    private ProjectMetadataRepository projectMetadataRepository;

    @Autowired
    public KeysetMetadataRepository(ProjectMetadataRepository projectMetadataRepository) {
        this.projectMetadataRepository = projectMetadataRepository;
    }

    public Mono<KeysetMetadata> find(String projectName, String id) {
        return projectMetadataRepository.findByName(projectName)
                .flatMap(ProjectMetadata::cloneOrContinue)
                .flatMap(projectMetadata -> {
                    try {
                        return Mono.just(projectMetadata.getKeyset(id));
                    } catch (KeysetNotFoundException e) {
                        return Mono.error(e);
                    }
                });
    }

    public Mono<KeysetMetadata> findSupportsLanguage(String projectName, String id, Language lang) {
        return find(projectName, id)
                .filter(keysetMetadata -> keysetMetadata.supportsLanguage(lang))
                .switchIfEmpty(Mono.error(new LanguageNotSupportedException()));
    }

    public Mono<KeysetMetadata> modifyAndSave(String projectName, String id, Function<KeysetMetadata, Mono<Void>> operation) {
        return projectMetadataRepository.findByName(projectName)
                .flatMap(ProjectMetadata::cloneOrContinue)
                .flatMap(projectMetadata -> {
                    try {
                        KeysetMetadata meta =projectMetadata.getKeyset(id);
                        return operation.apply(meta)
                                .then(projectMetadataRepository.update(projectMetadata))
                                .then(Mono.just(meta));
                    } catch (KeysetNotFoundException e) {
                        return Mono.error(e);
                    }
                });
    }
}
