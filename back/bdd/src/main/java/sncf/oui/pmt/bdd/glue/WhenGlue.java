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

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.model.Filters;
import cucumber.api.java.fr.Quand;
import org.apache.http.HttpResponse;
import org.bson.BsonInvalidOperationException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import sncf.oui.pmt.bdd.ContextAwareGlue;
import sncf.oui.pmt.bdd.resources.IntlnFile;
import sncf.oui.pmt.bdd.resources.MongoContextDocument;
import sncf.oui.pmt.bdd.utils.ApiRequester;
import sncf.oui.pmt.bdd.utils.Language;
import sncf.oui.pmt.bdd.utils.ResourcesUtils;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static sncf.oui.pmt.bdd.TestConfig.TEST_USER;

public class WhenGlue extends ContextAwareGlue {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhenGlue.class);
    private ApiRequester requester;
    private int gitPort;
    private String gitHost;
    private String dataRoot;

    public WhenGlue(ApiRequester requester, @Value("${gitPort:8888}") int gitPort, @Value("${gitHost}") String gitHost,
                    @Value("${dataroot}") String dataRoot) {
        this.requester = requester;
        this.gitPort = gitPort;
        this.gitHost = gitHost;
        this.dataRoot = Paths.get(dataRoot, TEST_USER).toString();
    }

    private void buildFiles() {
        context.tryGet(ContextConstants.INTLN_FILE_BUILDER).ifPresent(builder -> {
            builder.build().forEach(manager::notify);
            context.remove(ContextConstants.INTLN_FILE_BUILDER);
        });
    }

    private void feedContext(HttpResponse res) {
        context.put("statusCode", res.getStatusLine().getStatusCode());
        if (res.getStatusLine().getStatusCode() > 399) {
            LOGGER.error(String.format("Error %d: %s", res.getStatusLine().getStatusCode(), res.getStatusLine().getReasonPhrase()));
        }
        String resBody = ApiRequester.resBody(res);
        LOGGER.info(String.format("Result: %s", resBody));
        try {
            context.put(ContextConstants.RESPONSE_BODY, Document.parse(resBody));
        } catch (BsonInvalidOperationException e) {
            LOGGER.warn("Assuming result is a list of JSON docs!");
            context.put(ContextConstants.RESPONSE_BODY_AS_LIST, (List) Document.parse(String.format("{\"_list\": %s}", resBody)).get("_list"));
        }
    }

    @Quand("^je veux consulter le projet$")
    public void quandConsulteProjet() {
        buildFiles();
        requester.get(String.format("/projects/%s", context.get("projectName")), Collections.emptyMap(), this::feedContext);
        context.put("resType", "project");
    }

    @Quand("^je créée un projet depuis git$")
    public void creationDepuisGit() {
        buildFiles();
        final String projectName = "cloned-project";
        Map<String, Object> body = ImmutableMap.of(
                "url", String.format("http://%s:%d/git/%s", gitHost, gitPort, projectName),
                "name", projectName);
        requester.post("/projects/", Collections.EMPTY_MAP, body, this::feedContext);
        manager.notify(new IntlnFile(Paths.get(dataRoot, projectName)));
        manager.notify(new MongoContextDocument(Filters.eq("projectName", projectName), "projects"));
        context.put("resType", "project");
    }

    @Quand("^j'écris une clé \"([^\"]*)\" identifiée par \"([^\"]*)\"$")
    public void jecrisWording(String translation, String keyId) {
        buildFiles();
        jeTraduisCle(keyId, translation, "FR");
        context.put("resType", "key");
    }

    @Quand("^je récupère la clé identifiée par \"([^\"]*)\"$")
    public void jeRecupereCle(String keyId) {
        buildFiles();
        context.put("keyId", keyId);
        String uri = String.format("/projects/%s/keysets/%s/keys/%s", context.get("projectName"), context.get("keysetId"), keyId);
        requester.get(uri, Collections.EMPTY_MAP, this::feedContext);
        context.put("resType", "key");
    }

    @Quand("^j'ajoute le fichier \"([^\"]*)\" pour la langue ([a-zç]+)( à un nouveau keyset)?$")
    public void jAjouteLaLangueViaLeFichier(String file, String language, String newString) {
        buildFiles();
        boolean isNew = newString != null;
        Map<String, Object> body = ImmutableMap.of(Language.get(language), file);
        if (isNew) {
            String uri = String.format("/projects/%s/keysets", context.get("projectName"));
            requester.post(uri, Collections.emptyMap(), body, this::feedContext);
            context.put("keysetId", context.get(ContextConstants.RESPONSE_BODY).get("id"));
        } else {
            String uri = String.format("/projects/%s/keysets/%s", context.get("projectName"), context.get("keysetId"));
            requester.put(uri, Collections.EMPTY_MAP, body, this::feedContext);
        }
        context.put("resType", "keyset");
    }

    @Quand("^je traduis la clé \"([^\"]*)\" par \"([^\"]*)\" en ([a-zç]+)$")
    public void jeTraduisCle(String keyId, String translation, String language) {
        buildFiles();
        context.put("keyId", keyId);
        String uri = String.format("/projects/%s/keysets/%s/keys/%s", context.get("projectName"), context.get("keysetId"), keyId);
        Map<String, Object> body = ImmutableMap.of("language", Language.get(language), "translation", translation);
        requester.put(uri, Collections.EMPTY_MAP, body, this::feedContext);
        context.put("resType", "key");
    }

    @Quand("^j'exporte ce projet$")
    public void jExporteCeProjet() {
        buildFiles();
        String uri = String.format("/projects/%s/export", context.get("projectName"));
        requester.get(uri, Collections.EMPTY_MAP, this::feedContext);
    }

    @Quand("^j'importe des traductions pour la langue ([a-zç]+)$")
    public void jImporteTraductions(String language) {
        buildFiles();
        String uri = String.format("/projects/import/%s", context.get("exportId", String.class));
        Map<String, Object> body = ImmutableMap.of("language", Language.get(language));
        requester.postFormWithFile(uri, Collections.EMPTY_MAP, body,
                ResourcesUtils.readAsStream("data/export/to-import.csv"), "import.csv", this::feedContext);
        context.put("resType", "keysets");
    }

    @Quand("^je synchronise le projet$")
    public void jeSynchronise() {
        buildFiles();
        requester.post(String.format("/projects/%s/sync", context.get("projectName")), Collections.EMPTY_MAP, Collections.EMPTY_MAP, this::feedContext);
    }
}
