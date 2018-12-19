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

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Optional;

public class MongoContextDocument {

    private final Bson filter;
    private Document doc = null;
    private final String collection;

    public MongoContextDocument(Document doc, String collection) {
        this.doc = doc;
        this.filter = Filters.eq("_id", doc.getObjectId("_id"));
        this.collection = collection;
    }

    public MongoContextDocument(Bson filter, String collection) {
        this.filter = filter;
        this.collection = collection;
    }

    public String getCollection() {
        return collection;
    }

    public Optional<Document> getDoc() {
        return Optional.ofNullable(doc);
    }

    public Bson getFilter() {
        return filter;
    }
}
