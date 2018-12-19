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

package sncf.oui.pmt.infrastructure.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.export.ExportMetaRepository;
import sncf.oui.pmt.domain.export.ExportMetadata;
import sncf.oui.pmt.infrastructure.mongo.MongoRepository;

import java.time.Instant;

@Component
public class MongoExportMetaRepository extends MongoRepository<ExportMetadata> implements ExportMetaRepository {

    @Autowired
    public MongoExportMetaRepository(MongoDatabase database) {
        super(database);
    }

    @Override
    public Flux<ExportMetadata> findRecent(int weekNum) {
        long week = 7*24*60*60;
        Instant after = Instant.now().minusSeconds(week*(weekNum+1));
        Instant before = Instant.now().minusSeconds(week*weekNum);
        Bson filter = Filters.and(
                Filters.gte("timestamp", after.toEpochMilli()),
                Filters.lte("timestamp", before.toEpochMilli()));
        return find(filter);
    }

    @Override
    public Mono<ExportMetadata> findByExportId(String id) {
        return find(Filters.eq("exportId", id))
                .take(1)
                .single();
    }

    @Override
    public Mono<Void> deleteById(String exportId) {
        return delete(Filters.eq("exportId", exportId))
                .then(Mono.empty());
    }

    @Override
    public Mono<ExportMetadata> save(ExportMetadata exportMetadata) {
        return insert(exportMetadata);
    }

    @Override
    protected String collectionName() {
        return "exported";
    }

    @Override
    protected Class<ExportMetadata> collectionClass() {
        return ExportMetadata.class;
    }
}
