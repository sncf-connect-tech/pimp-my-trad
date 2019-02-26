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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.DomainDrivenDesign;
import sncf.oui.pmt.domain.FileHandler;
import sncf.oui.pmt.domain.TranslateService;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@DomainDrivenDesign.Entity
public class KeysetMetadata {

    private String id;
    private String projectRoot;
    private Map<Language, String> files;
    private TranslateService translateService;
    private FileHandler fileHandler;

    public KeysetMetadata(String id, Map<Language, String> files, String projectRoot, TranslateService translateService, FileHandler fileHandler) {
        this.id = Optional.ofNullable(id).orElse(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        this.files = files;
        this.projectRoot = projectRoot;
        this.translateService = translateService;
        this.fileHandler = fileHandler;
    }

    public Mono<KeysetMetadata> addFile(Language lang, String path) {
        return addFile(lang, path, true);
    }

    public Mono<KeysetMetadata> addFile(Language lang, String path, boolean overwrite) {
        files.put(lang, path);
        return overwrite ? readSingleFile(Language.FR, getFile(Language.FR))
                .map(keyset -> keyset.getKeys().keySet().stream().collect(Collectors.toMap(v -> v, v -> "")))
                .flatMap(map -> fileHandler.writeFull(getFile(lang), map))
                .then(Mono.just(this))
                : Mono.just(this);
    }

    public String getFile(Language lang) {
        return Paths.get(projectRoot, files.get(lang)).toString();
    }

    public Set<Language> getSupportedLanguage() {
        return files.keySet();
    }

    public Map<Language, String> getRelativeFiles() {
        return files;
    }

    public Map<Language, String> getFiles() {
        return getFiles(Collections.emptyList());
    }

    public Map<Language, String> getFiles(List<Language> blacklist) {
        return files.entrySet().stream()
                .filter(entry -> !blacklist.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Paths.get(projectRoot, e.getValue()).toString()));
    }

    public Mono<Keyset> readKeyset(List<Language> blacklist) {
        Flux<Keyset> readKeysets = Flux.fromIterable(getFiles(blacklist).entrySet())
                .flatMap(entry -> readSingleFile(entry.getKey(), entry.getValue()));
        return readKeysets
                .reduce(Keyset::merge)
                .doOnNext(keyset -> keyset.setSupportedLanguages(getSupportedLanguage()));
    }

    public Mono<Keyset> readKeyset() {
        return readKeyset(Collections.emptyList());
    }

    private Mono<Keyset> readSingleFile(Language lang, String path) {
        Mono<Map<String, String>> firstMap = fileHandler.readOurVersion(path);
        Mono<Map<String, String>> secondMap = fileHandler.readTheirPatch(path);
        return firstMap.zipWith(secondMap)
                .map(pair -> Keyset.fromMaps(lang, pair.getT1(), pair.getT2()))
                .map(keyset -> keyset.setName(nameSet()).setId(id));
    }

    public Mono<Keyset> updateKeys(Map<String, String> idsToTranslated, Language lang) {
        List<Language> blacklist = Collections.singletonList(lang);
        return fileHandler.writeDiff(getFile(lang), idsToTranslated)
                .map(curLangMap -> Keyset.fromMaps(lang, curLangMap, null))
                .map(keyset -> keyset.setName(nameSet()).setId(id))
                .mergeWith(readKeyset(blacklist))
                .reduce(Keyset::merge);
    }

    public Mono<Keyset> updateKey(String keyId, Language lang, String translated) {
        return updateKeys(Collections.singletonMap(keyId, translated), lang);
    }

    public Mono<Keyset> deleteKey(String keyId) {
        return Flux.fromIterable(getSupportedLanguage())
                .flatMap(language -> updateKey(keyId, language, null))
                .last();
    }

    public Mono<Boolean> hasKey(String keyId) {
        return readSingleFile(Language.FR, getFile(Language.FR))
                .filter(keyset -> keyset.hasKey(keyId))
                .map(keyset -> true)
                .switchIfEmpty(Mono.just(false));
    }

    public Boolean supportsLanguage(Language lang) {
        return files.containsKey(lang);
    }

    public String nameSet() {
        if (files.entrySet().size() == 1) {
            return files.entrySet().iterator().next().getValue();
        }
        String common = files.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce((acc, curr) -> {
                    int offset = IntStream
                            .range(0, Math.min(acc.length(), curr.length()))
                            .reduce(-1, (result, i) -> result == i - 1
                                    && acc.charAt(i) == curr.charAt(i) ?
                                    i : result);
                    return curr.substring(0, offset + 1);
                })
                .orElse("");
        String firstPath = files.entrySet().iterator().next().getValue();
        String ext = firstPath.substring(firstPath.lastIndexOf(".") + 1);
        return String.format("%s*.%s", common, ext);
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public Mono<Keyset> translateKey(String keyId, Language language) {
        return readSingleFile(Language.FR, getFile(Language.FR))
                .flatMap(keyset -> {
                    try {
                        return Mono.just(keyset.getKey(keyId));
                    } catch (KeyNotFoundException e) {
                        return Mono.error(e);
                    }
                })
                .map(key -> key.getTranslation(Language.FR))
                .flatMap(fr -> translateService
                        .translate(Collections.singletonList(fr.get(0)), language.toString())
                        .single())
                .flatMap(result -> updateKey(keyId, language, result + KeyState.Todo.asTag()));
    }

    public KeysetMetadata setProjectRoot(String root) {
        projectRoot = root;
        return this;
    }

    public String getId() {
        return id;
    }
}
