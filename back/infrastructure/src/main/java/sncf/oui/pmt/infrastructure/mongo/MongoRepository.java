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

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class MongoRepository<T> {

    private MongoDatabase database;

    public MongoRepository(MongoDatabase database) {
        this.database = database;
    }

    protected abstract String collectionName();
    protected abstract Class<T> collectionClass();

    private MongoCollection<T> collection() {
        return database.getCollection(collectionName(), collectionClass());
    }

    protected Flux<T> find(Bson criteria) {
        return Flux.from(collection().find(criteria));
    }

    protected Flux<T> find() {
        return Flux.from(collection().find());
    }

    protected Mono<T> insert(T object) {
        return Mono.from(collection().insertOne(object))
                .then(Mono.just(object));
    }

    protected Mono<T> replace(Bson query, T object) {
        return Mono.from(collection().replaceOne(query, object))
                .then(Mono.just(object));
    }

    protected Mono<Boolean> delete(Bson query) {
        return Mono.from(collection().deleteOne(query))
                .map(DeleteResult::wasAcknowledged);
    }
}
