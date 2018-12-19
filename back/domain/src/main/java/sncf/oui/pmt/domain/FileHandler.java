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

package sncf.oui.pmt.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.keyset.FormatNotSupportedException;
import sncf.oui.pmt.domain.keyset.MapEncoder;

import java.util.*;

@Component
public class FileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
    private ConflictingFileHandle handle;
    private List<MapEncoder> encoders;

    @Autowired
    public FileHandler(ConflictingFileHandle handle, List<MapEncoder> encoders) {
        this.handle = handle;
        this.encoders = encoders;
    }

    public Mono<Boolean> hasConflict(String path) {
        return handle.hasConflict(path);
    }

    public Mono<Map<String, String>> readOurVersion(String path) {
        Mono<Map<String, String>> mono = hasConflict(path).flatMap(conflict -> conflict ?
                read(path, ConflictFlag.Ours)
                        .flatMap(ours -> read(path, ConflictFlag.Resolved)
                                .map(resolved -> {
                                    ours.putAll(resolved);
                                    return ours;
                                })) :
                read(path, null));
        return mono.onErrorResume(e -> {
            LOGGER.error("An error occurred while reading: " + e.getMessage());
            return Mono.just(Collections.emptyMap());
        });
    }

    private Mono<Map<String, String>> readResolvedVersion(String path) {
        return read(path, ConflictFlag.Resolved)
                .onErrorResume(e -> Mono.just(Collections.emptyMap()));
    }

    public Mono<Map<String, String>> readTheirPatch(String path) {
        return hasConflict(path).flatMap(conflict -> conflict ?
                read(path, ConflictFlag.Theirs)
                        .flatMap(theirs -> read(path, ConflictFlag.Resolved)
                                .map(resolved -> {
                                    theirs.keySet().removeAll(resolved.keySet());
                                    return theirs;
                                }))
                        .onErrorResume(e -> Mono.just(Collections.emptyMap())) :
                Mono.just(Collections.emptyMap()));
    }

    public Mono<Map<String, String>> writeFull(String path, Map<String, String> full) {
        String format = path.substring(path.lastIndexOf(".") + 1);
        return findSuitableEncoder(format)
                .flatMap(e -> e.encode(full))
                .flatMap(s -> handle.write(path, s))
                .then(Mono.just(full));
    }

    public Mono<Map<String, String>> writeDiff(String path, Map<String, String> diff) {
        String format = path.substring(path.lastIndexOf(".") + 1);
        Mono<Map<String, String>> mapMono = hasConflict(path).flatMap(conflict -> conflict ?
                readResolvedVersion(path) : readOurVersion(path));
        return findSuitableEncoder(format).zipWith(mapMono)
                .flatMap(pair -> {
                    Map<String, String> full = new HashMap<>();
                    full.putAll(pair.getT2());
                    full.putAll(diff);
                    return pair.getT1().encode(full)
                            .flatMap(s -> handle.write(path, s))
                            .then(Mono.just(full));
                });
    }

    private Mono<Map<String, String>> read(String path, ConflictFlag flag) {
        String format = path.substring(path.lastIndexOf(".") + 1);
        return findSuitableEncoder(format)
                .zipWith(handle.readLines(path, flag).collectList())
                .flatMap(pair -> pair.getT1().decode(pair.getT2()));
    }

    private Mono<MapEncoder> findSuitableEncoder(String format) {
        Optional<MapEncoder> suitableEncoder = encoders.stream()
                .filter(encoder -> encoder.accepts(format))
                .limit(1)
                .findFirst();
        return suitableEncoder.map(Mono::just).orElse(Mono.error(new FormatNotSupportedException()));
    }
}
