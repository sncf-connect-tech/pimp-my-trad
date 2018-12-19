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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import sncf.oui.pmt.domain.export.ExportMetaRepository;
import sncf.oui.pmt.domain.export.ExportMetadata;
import sncf.oui.pmt.domain.export.KeySpec;
import sncf.oui.pmt.domain.keyset.Keyset;
import sncf.oui.pmt.domain.keyset.KeysetMetadata;
import sncf.oui.pmt.domain.keyset.KeysetMetadataRepository;
import sncf.oui.pmt.domain.keyset.Language;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ImportTranslations {

    private KeysetMetadataRepository keysetMetadataRepository;
    private ExportMetaRepository exportMetaRepository;

    @Autowired
    public ImportTranslations(KeysetMetadataRepository keysetMetadataRepository,
                              ExportMetaRepository exportMetaRepository) {
        this.keysetMetadataRepository = keysetMetadataRepository;
        this.exportMetaRepository = exportMetaRepository;
    }

    public Flux<Keyset> importFromBytes(byte[] bytes, Language language, String exportId) {
        String imported = new String(bytes, Charset.forName("UTF-8"));
        return exportMetaRepository.findByExportId(exportId)
                .flux()
                .flatMap(meta -> importForMetadata(meta, imported, language));
    }

    private Flux<Keyset> importForMetadata(ExportMetadata export, String imported, Language language) {
        return Flux.fromArray(imported.split("\\R"))
                .zipWith(Flux.fromStream(export.streamSpecs()))
                .groupBy(pair -> pair.getT2().getProjectName() + ":" + pair.getT2().getKeysetId())
                .flatMap(group -> makeMapFromGroup(group.cache())
                        .zipWith(findKeysetMeta(group.key())))
                .flatMap(pair -> pair.getT2().updateKeys(pair.getT1(), language));
    }

    private static Mono<Map<String, String>> makeMapFromGroup(Flux<Tuple2<String, KeySpec>> group) {
        return Flux.zip(group.map(t -> t.getT2().getKeyId()), group.map(Tuple2::getT1))
                .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
    }

    private Mono<KeysetMetadata> findKeysetMeta(String key) {
        String[] splitted = key.split(":");
        if (splitted.length != 2) {
            return Mono.error(new IndexOutOfBoundsException());
        } else {
            String project = splitted[0];
            String keysetId = splitted[1];
            return keysetMetadataRepository.find(project, keysetId);
        }
    }
}
