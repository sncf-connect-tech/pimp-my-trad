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

package sncf.oui.pmt.bdd.glue;

import org.bson.Document;
import sncf.oui.pmt.bdd.resources.IntlnFileBuilder;
import sncf.oui.pmt.bdd.utils.TypedContextItem;

import java.util.List;

public class ContextConstants {
    static final TypedContextItem<IntlnFileBuilder> INTLN_FILE_BUILDER = new TypedContextItem<>("INTLN_FILE_BUILDER", IntlnFileBuilder.class);
    static final TypedContextItem<Document> RESPONSE_BODY = new TypedContextItem<>("RESPONSE_BODY", Document.class);
    static final TypedContextItem<List> RESPONSE_BODY_AS_LIST = new TypedContextItem<>("RESPONSE_BODY_AS_LIST", List.class);
}