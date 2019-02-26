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

package sncf.oui.pmt.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.application.KeysetCreation;
import sncf.oui.pmt.domain.keyset.Key;
import sncf.oui.pmt.domain.keyset.KeyNotFoundException;
import sncf.oui.pmt.domain.keyset.Keyset;
import sncf.oui.pmt.domain.keyset.KeysetInput;
import sncf.oui.pmt.domain.keyset.KeysetMetadata;
import sncf.oui.pmt.domain.keyset.KeysetMetadataRepository;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/projects/{name:.+}")
public class KeysetApi {

    private KeysetMetadataRepository repository;
    private KeysetCreation keysetCreation;
    private ExceptionMapper mapper;

    @Autowired
    public KeysetApi(KeysetMetadataRepository repository,
                     KeysetCreation keysetCreation,
                     ExceptionMapper mapper) {
        this.repository = repository;
        this.keysetCreation = keysetCreation;
        this.mapper = mapper;
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(Throwable e) {
        return this.mapper.handle(e);
    }

    @PostMapping("/keysets")
    public Mono<ResponseEntity<Keyset>> addKeyset(@PathVariable("name") String name,
                                                  @Valid @RequestBody KeysetInput input) {
        return keysetCreation.create(name, input)
                .flatMap(KeysetMetadata::readKeyset)
                .map(keyset -> ResponseEntity.status(HttpStatus.CREATED).body(keyset));
    }

    @GetMapping("/keysets/{id}/keys/{keyId}")
    public Mono<ResponseEntity<Key>> viewKeyInSet(@PathVariable("name") String name,
                                                  @PathVariable("id") String id,
                                                  @PathVariable("keyId") String keyId) {
        return repository.find(name, id)
                .flatMap(KeysetMetadata::readKeyset)
                .flatMap(keyset -> {
                    try {
                        return Mono.just(keyset.getKey(keyId));
                    } catch (KeyNotFoundException e) {
                        return Mono.error(e);
                    }
                })
                .map(key -> ResponseEntity.ok().body(key));
    }

    @PostMapping("/keysets/{id}/keys/{keyId}/translate")
    public Mono<ResponseEntity<Key>> translateKey(@PathVariable("name") String name,
                                                  @PathVariable("id") String id,
                                                  @PathVariable("keyId") String keyId,
                                                  @Valid @RequestBody LanguageInput input) {
        return repository.findSupportsLanguage(name, id, input.getLanguage())
                .flatMap(keysetMetadata -> keysetMetadata.translateKey(keyId, input.getLanguage()))
                .flatMap(keyset -> {
                    try {
                        return Mono.just(keyset.getKey(keyId));
                    } catch (KeyNotFoundException e) {
                        return Mono.error(e);
                    }
                })
                .map(key -> ResponseEntity.ok().body(key));
    }

    @PutMapping("/keysets/{id}/keys/{keyId}")
    public Mono<ResponseEntity<Key>> setKeyInSet(@PathVariable("name") String name,
                                                 @PathVariable("id") String id,
                                                 @PathVariable("keyId") String keyId,
                                                 @Valid @RequestBody KeyModificationInput input) {
        return repository.findSupportsLanguage(name, id, input.getLanguage())
                .flatMap(keysetMetadata -> keysetMetadata.updateKey(keyId, input.getLanguage(), input.getTranslation()))
                .flatMap(keyset -> {
                    try {
                        return Mono.just(keyset.getKey(keyId));
                    } catch (KeyNotFoundException e) {
                        return Mono.error(e);
                    }
                })
                .map(key -> ResponseEntity.ok().body(key));
    }

    @DeleteMapping("/keysets/{id}/keys/{keyId}")
    public Mono<ResponseEntity<Void>> deleteKeyInSet(@PathVariable("name") String name,
                                                    @PathVariable("id") String id,
                                                    @PathVariable("keyId") String keyId) {
        return repository.find(name, id).single()
                .flatMap(meta -> meta.deleteKey(keyId))
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @PutMapping("/keysets/{id}")
    public Mono<ResponseEntity<Keyset>> addLanguage(@PathVariable("name") String name,
                                                    @PathVariable("id") String id,
                                                    @Valid @RequestBody KeysetInput input,
                                                    @RequestParam("overwrite") Optional<Boolean> overwrite) {
        return repository.modifyAndSave(name, id, keysetMetadata -> Flux.fromIterable(input.entrySet())
                .flatMap(e -> keysetMetadata.addFile(e.getKey(), e.getValue(), overwrite.orElse(true)))
                .then(Mono.empty()))
                .flatMap(KeysetMetadata::readKeyset)
                .map(keyset -> ResponseEntity.status(HttpStatus.OK).body(keyset));
    }
}
