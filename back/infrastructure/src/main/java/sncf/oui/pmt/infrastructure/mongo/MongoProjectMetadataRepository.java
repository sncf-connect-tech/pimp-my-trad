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
import sncf.oui.pmt.domain.project.ProjectMetadataRepository;
import sncf.oui.pmt.domain.project.ProjectMetadata;
import sncf.oui.pmt.domain.project.ProjectNotFoundException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.infrastructure.AuthenticationDetails;

@Component
public class MongoProjectMetadataRepository extends MongoRepository<ProjectMetadata> implements ProjectMetadataRepository {

    private final AuthenticationDetails details;

    protected String collectionName() {
        return "projects";
    }

    protected Flux<ProjectMetadata> actualFindByName(String name) {
        return details.getUser()
                .map(user -> Filters.and(
                        Filters.eq("projectName", name),
                        Filters.eq("_user", user)
                ))
                .flux()
                .flatMap(this::find);
    }

    protected Class<ProjectMetadata> collectionClass() {
        return ProjectMetadata.class;
    }

    @Autowired
    public MongoProjectMetadataRepository(MongoDatabase database, AuthenticationDetails details) {
        super(database);
        this.details = details;
    }

    @Override
    public Flux<ProjectMetadata> findAll() {
        return find();
    }

    @Override
    public Mono<Boolean> exists(String name) {
        return actualFindByName(name)
                .map(any -> true)
                .switchIfEmpty(Mono.just(false))
                .next();
    }

    @Override
    public Mono<ProjectMetadata> findByName(String name) {
        return actualFindByName(name)
                .switchIfEmpty(Mono.error(new ProjectNotFoundException()))
                .next();
    }

    @Override
    public Mono<ProjectMetadata> findById(String id) {
        return find(Filters.eq("_id", new ObjectId(id)))
                .switchIfEmpty(Mono.error(new ProjectNotFoundException()))
                .next();
    }

    @Override
    public Mono<ProjectMetadata> save(ProjectMetadata project) {
        return insert(project);
    }

    @Override
    public Mono<ProjectMetadata> update(ProjectMetadata project) {
        return project.getId().isPresent()
                ? replace(Filters.eq("_id", new ObjectId(project.getId().get())), project)
                : Mono.error(new ProjectNotFoundException());
    }
}

