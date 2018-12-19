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

package sncf.oui.pmt.bdd.resources;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sncf.oui.pmt.bdd.utils.GenericContextResourceManager;
import sncf.oui.pmt.bdd.utils.TestContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class MongoContextDatabase implements GenericContextResourceManager<MongoContextDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoContextDatabase.class);
    private static final String DOCS = "_COLLECTED_DOCS";
    private static final String COLLECTIONS = "_COLLECTED_COLLECTIONS";

    private MongoDatabase database;
    private TestContext context;

    public MongoContextDatabase(MongoDatabase database, TestContext context) {
        this.database = database;
        this.context = context;
    }

    private Set<ObjectId> collectedDocs() {
        return context
                .tryGet(DOCS, Set.class)
                .orElse(new HashSet<>());
    }

    private Set<String> collectedCollections() {
        return context
                .tryGet(COLLECTIONS, Set.class)
                .orElse(new HashSet<>());
    }

    private Document getActualDoc(MongoContextDocument doc) {
        return doc.getDoc().orElseGet(() -> database
                .getCollection(doc.getCollection())
                .find(doc.getFilter())
                .first());
    }

    @Override
    public void notify(MongoContextDocument doc) {
        Set<ObjectId> docs = collectedDocs();
        Set<String> collections = collectedCollections();
        docs.add(getActualDoc(doc).getObjectId("_id"));
        collections.add(doc.getCollection());
        context.put(DOCS, docs);
        context.put(COLLECTIONS, collections);
    }

    @Override
    public MongoContextDocument add(MongoContextDocument doc) {
        Document bsonDoc = getActualDoc(doc);
        if (!bsonDoc.containsKey("_id")) {
            bsonDoc.put("_id", new ObjectId());
        }
        database.getCollection(doc.getCollection()).insertOne(bsonDoc);
        return doc;
    }

    @Override
    public void cleanup() {
        Set<ObjectId> docs = collectedDocs();
        Set<String> collections = collectedCollections();
        if (docs.size() > 0) {
            collections.forEach(collection -> {
                MongoCollection<Document> mongoCollection = database.getCollection(collection);
                mongoCollection.find(Filters.in("_id", docs)).forEach((Consumer<Document>) document -> {
                    DeleteResult res = mongoCollection.deleteOne(Filters.eq("_id", document.getObjectId("_id")));
                    LOGGER.info(String.format("Deleting %s: %s", document.toString(),
                            res.wasAcknowledged() ? "success" : "failure"));
                });
            });
        }
        context.remove(DOCS);
        context.remove(COLLECTIONS);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == MongoContextDocument.class;
    }
}
