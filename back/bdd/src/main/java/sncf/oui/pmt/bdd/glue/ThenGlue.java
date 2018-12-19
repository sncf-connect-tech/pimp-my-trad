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

import com.google.common.collect.ImmutableList;
import com.mongodb.client.model.Filters;
import cucumber.api.DataTable;
import cucumber.api.java.fr.Alors;
import cucumber.api.java.fr.Et;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sncf.oui.pmt.bdd.ContextAwareGlue;
import sncf.oui.pmt.bdd.resources.MongoContextDocument;
import sncf.oui.pmt.bdd.utils.Language;

import java.util.List;

import static org.junit.Assert.*;

public class ThenGlue extends ContextAwareGlue {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThenGlue.class);

    private void jeRecoisProjet(String nouveau, List<String> fields) {
        boolean isNotNew = nouveau == null;
        assertEquals("le statut HTTP est correct", isNotNew ? 200 : 201, context.getInt("statusCode"));
        Document res = context.get(ContextConstants.RESPONSE_BODY);
        fields.forEach(field -> {
            assertTrue(String.format("le projet a la propriété %s", field), res.containsKey(field));
        });
    }

    @Alors("^je reçois un( nouveau)? projet$")
    public void jeRecoisProjet(String nouveau) {
        jeRecoisProjet(nouveau, ImmutableList.of("name", "keysets"));
    }

    @Alors("^je reçois un( nouveau)? projet avec les propriétés suivantes :$")
    public void jeRecoisProjet(String nouveau, DataTable data) {
        jeRecoisProjet(nouveau, data.asList(String.class));
    }

    @Et("^les clés ont un état$")
    public void clesOntEtat() {
        Document res = context.get(ContextConstants.RESPONSE_BODY);
        Document keyset = (Document) res.get("keysets", List.class).get(0);
        assertTrue("le keyset a des clés", keyset.containsKey("keys"));
        Document keys = keyset.get("keys", Document.class);
        String someKey = keys.keySet().iterator().next();
        Document actualKey = keys.get(someKey, Document.class);
        assertTrue("kes clés ont un état", actualKey.containsKey("state"));
    }


    private void jeRecoisCle(List<String> fields) {
        assertEquals("le statut HTTP est correct", 200, context.getInt("statusCode"));
        Document res = context.get(ContextConstants.RESPONSE_BODY);
        fields.forEach(field -> {
            assertTrue(String.format("la clé a la propriété %s", field), res.containsKey(field));
        });
    }

    @Alors("^je reçois une clé avec les propriétés suivantes :$")
    public void jeRecoisCle(DataTable data) {
        jeRecoisCle(data.asList(String.class));
    }

    @Alors("^je reçois une clé$")
    public void jeRecoisCle() {
        jeRecoisCle(ImmutableList.of("state", "translations"));
    }

    @Et("^la clé a pour traduction (?:en )?([a-zç]+) : (.*)$")
    public void valeurTraduction(String language, String translations) {
        Document res = context.get(ContextConstants.RESPONSE_BODY);
        assertArrayEquals("la clé a les bonnes traductions", translations.split(", "), res
                .get("translations", Document.class)
                .get(Language.get(language), List.class).toArray());
    }


    @Et("^la clé \"([^\"]*)\" a pour traduction (?:en )?([a-zç]+) : (.*)$")
    public void valeurTraduction(String keyId, String language, String translations) {
        Document res;
        if (context.get("resType").equals("keysets")) {
            res = (Document) context.get(ContextConstants.RESPONSE_BODY_AS_LIST).get(0);
        } else {
            res = context.get(ContextConstants.RESPONSE_BODY);
        }
        assertArrayEquals("la clé a les bonnes traductions", translations.split(", "), res
                .get("keys", Document.class)
                .get(keyId, Document.class)
                .get("translations", Document.class)
                .get(Language.get(language), List.class).toArray());
    }

    @Alors("^je n'ai pas de résultat$")
    public void jAiPasResultat() {
        assertEquals(404, context.getInt("statusCode"));
    }

    private void jeRecoisKeyset(String nouveau, List<String> fields) {
        boolean isNew = nouveau != null;
        assertEquals("le status HTTP est correct", isNew ? 201 : 200, context.getInt("statusCode"));
        Document res = context.get(ContextConstants.RESPONSE_BODY);
        fields.forEach(field -> {
            assertTrue(String.format("le key a la propriété %s", field), res.containsKey(field));
        });
    }

    @Alors("^je reçois un( nouveau)? keyset avec les propriétés suivantes :?$")
    public void jeRecoisKeyset(String nouveau, DataTable data) {
        jeRecoisKeyset(nouveau, data.asList(String.class));
    }

    @Alors("^je reçois un( nouveau)? keyset?$")
    public void jeRecoisKeyset(String nouveau) {
        jeRecoisKeyset(nouveau, ImmutableList.of("name", "keys", "supportedLanguages"));
    }

    @Et("^le keyset prend en charge la langue ([a-zç]+)$")
    public void leKeysetPrendEnChargeLaLangue(String language) {
        Document res = context.get(ContextConstants.RESPONSE_BODY);
        assertEquals("le keyset prend en charge la langue", true, res
                .get("supportedLanguages", List.class)
                .contains(Language.get(language)));
    }

    @Et("^la clé \"([^\"]*)\" n'est pas traduite en ([a-zç]+)$")
    public void cleNonTraduite(String keyId, String language) {
        Document keyset = context.get(ContextConstants.RESPONSE_BODY);
        final String[] expected = {""};
        assertArrayEquals("il n'y a pas de traductions", expected, keyset
                .get("keys", Document.class)
                .get(keyId, Document.class)
                .get("translations", Document.class)
                .get(Language.get(language), List.class)
                .toArray());
    }

    @Et("^la clé a pour état \"([^\"]*)\"$")
    public void etatCle(String state) {
        Document key = context.get(ContextConstants.RESPONSE_BODY);
        assertEquals("l'état de la clé est correct", state, key.get("state", String.class));
    }


    private void jeRecoisExport(List<String> fields) {
        assertEquals("le statut HTTP est correct",201, context.getInt("statusCode"));
        Document export = context.get(ContextConstants.RESPONSE_BODY);
        fields.forEach(field -> {
            assertTrue(String.format("l'export a la propriété %s", field), export.containsKey(field));
        });

        String exportId = export.get("metadata", Document.class).getString("exportId");
        manager.notify(new MongoContextDocument(Filters.eq("exportId", exportId), "exported"));
    }

    @Alors("^je reçois un export avec les propriétés suivantes :$")
    public void jeRecoisExport(DataTable data) {
        jeRecoisExport(data.asList(String.class));
    }

    @Alors("^je reçois un export$")
    public void jeRecoisExport() {
        jeRecoisExport(ImmutableList.of("metadata", "exported"));
    }

    @Et("^l'export CSV vaut :$")
    public void valeurExport(String exportCsv) {
        Document export = context.get(ContextConstants.RESPONSE_BODY);
        String actual = export.get("exported", String.class);
        assertEquals("l'export CSV est celui attendu", exportCsv.substring(2), actual);
    }

    @Alors("^je reçois des keysets$")
    public void jeRecoisKeysets() {
        assertEquals("le code HTTP est correct", 200, context.getInt("statusCode"));
    }

    @Alors("^il y a des conflits$")
    public void ilYADesConflits() {
        Document res = context.get(ContextConstants.RESPONSE_BODY);
        assertTrue("il y a des conflits", res.getBoolean("conflicts"));
    }
}
