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
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;
import sncf.oui.pmt.DomainDrivenDesign;
import sncf.oui.pmt.domain.keyset.*;
import sncf.oui.pmt.domain.project.Project;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DomainDrivenDesign.Entity
public class ExportMetadata {

    private String exportId;
    private List<KeySpec> keySpecs; // no getter so Jackson hides this from response
    private long timestamp;

    public ExportMetadata(String exportId, long timestamp, List<KeySpec> keySpecs) {
        this.exportId = exportId;
        this.timestamp = timestamp;
        this.keySpecs = keySpecs;
    }

    public ExportMetadata() {
        exportId = UUID.randomUUID().toString().substring(0, 6);
        timestamp = Instant.now().toEpochMilli();
        keySpecs = new LinkedList<>();
    }

    public Mono<String> exportProject(Project project) {
        int filter = KeyState.InProgress.flag() | KeyState.Todo.flag();
        List<Keyset> k = project.getKeysets();
        return Flux.fromIterable(k)
                .map(keyset -> Tuples.of(keyset.getId(), keyset.getKeysByState(filter)))
                .flatMap(m -> Flux.fromIterable(m.getT2().entrySet())
                        .zipWith(Flux.just(m.getT1()).repeat())) // Keep id of keyset
                .map(tuple -> exportKey(project, tuple))
                .collect(Collectors.joining("\n"));
    }

    public static String utfHeader() {
        return "\uFEFF";
    }

    private String exportKey(Project project, Tuple2<Map.Entry<String, Key>, String> tuple) {
        Map.Entry<String, Key> e = tuple.getT1();
        String keysetId = tuple.getT2();
        keySpecs.add(new KeySpec(project.getName(), keysetId, e.getKey()));
        return e.getValue().getCleanTranslation(Language.FR);
    }

    public String getExportId() {
        return exportId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Stream<KeySpec> streamSpecs() {
        return keySpecs.stream();
    }
}

