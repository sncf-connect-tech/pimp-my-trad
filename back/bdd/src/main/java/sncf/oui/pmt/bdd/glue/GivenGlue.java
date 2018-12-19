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

import cucumber.api.java.fr.Et;
import cucumber.api.java.fr.Soit;
import org.bson.Document;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import sncf.oui.pmt.bdd.ContextAwareGlue;
import sncf.oui.pmt.bdd.resources.IntlnFile;
import sncf.oui.pmt.bdd.resources.IntlnFileBuilder;
import sncf.oui.pmt.bdd.resources.MongoContextDocument;
import sncf.oui.pmt.bdd.utils.GitWrapper;
import sncf.oui.pmt.bdd.utils.Language;
import sncf.oui.pmt.bdd.utils.ResourcesUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static sncf.oui.pmt.bdd.TestConfig.TEST_USER;

public class GivenGlue extends ContextAwareGlue {

    private static final Logger LOGGER = LoggerFactory.getLogger(GivenGlue.class);
    private CredentialsProvider credentials;

    private int gitPort;
    private String dataRoot;
    private String gitHost;

    public GivenGlue(CredentialsProvider credentials, @Value("${gitPort:8888}") int gitPort,
                     @Value("${dataroot}") String dataRoot, @Value("${gitHost}") String gitHost) {
        this.credentials = credentials;
        this.gitPort = gitPort;
        this.dataRoot = Paths.get(dataRoot, TEST_USER).toString();
        this.gitHost = gitHost;
    }

    @Soit("^le projet paramétré \"([^\"]*)\"$")
    public void soitUnProjet(String projectName) {
        Document doc = Document.parse(ResourcesUtils.read(String.format("data/mongo/%s.json", projectName)));
        String uri = String.format("http://%s:%d/git/%s", gitHost, gitPort, projectName);
        String uriLocal = String.format("http://localhost:%d/git/%s", gitPort, projectName);
        doc.put("origin", uri);
        Path localPath = Paths.get(dataRoot, projectName);

        context.put("projectName", projectName);
        context.put("keysetId", ((Document) doc.get("keysets", List.class).get(0)).get("id"));
        manager.save(new MongoContextDocument(doc, "projects"));

        try {
            try (Git git = Git.cloneRepository()
                    .setURI(uriLocal)
                    .setDirectory(localPath.toFile())
                    .setCredentialsProvider(credentials)
                    .call()) {
                StoredConfig config = git.getRepository().getConfig();
                config.setString("remote", "origin", "url", uri);
                config.save();
                manager.notify(new IntlnFile(localPath));
            }
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        } catch (JGitInternalException e) {
            LOGGER.info("Project already cloned");
        }
        LOGGER.info("Cloned repo successfully");

        newBuilder(localPath);
    }

    private IntlnFileBuilder newBuilder(Path localPath) {
        IntlnFileBuilder builder = new IntlnFileBuilder();
        builder.setDestination(localPath.toString())
                .putFrom("FR", ResourcesUtils.read("data/i18n/fr.json"))
                .putFrom("EN", ResourcesUtils.read("data/i18n/en.json"));
        context.put(ContextConstants.INTLN_FILE_BUILDER, builder);
        return builder;
    }

    @Et("^il comporte la clé \"([^\"]*)\" identifiée par \"([^\"]*)\"")
    public void ajoutWording(String text, String keyId) {
        context.put("keyId", keyId);
        context.get(ContextConstants.INTLN_FILE_BUILDER)
                .set(keyId, text);
    }

    @Et("^la clé est traduite par \"([^\"]*)\" en ([a-zç]+)")
    public void ajoutTrad(String translation, String lang) {
        String langCode = Language.get(lang);
        context.get(ContextConstants.INTLN_FILE_BUILDER)
                .translate(langCode, context.get("keyId"), translation);
    }

    @Soit("^le projet non paramétré \"([^\"]*)\"$")
    public void soitProjetVide(String projectName) {
        Document doc = Document.parse(ResourcesUtils.read(String.format("data/mongo/%s.json", projectName)));
        doc.put("origin", String.format("http://%s:%d/git/%s", gitHost, gitPort, projectName));
        context.put("projectName", projectName);
        manager.save(new MongoContextDocument(doc, "projects"));

        newBuilder(Paths.get(dataRoot, projectName));
    }

    @Et("^un export du projet(?: \"([^\"]*)\")?$")
    public void unExport(String projectName) {
        if (projectName == null) {
            projectName = context.get("projectName");
        }
        Document doc = Document.parse(ResourcesUtils.read(String.format("data/mongo/export_%s.json", projectName)));
        context.put("exportId", doc.get("exportId"));
        manager.save(new MongoContextDocument(doc, "exported"));
    }

    @Et("^quelqu'un d'autre a modifié les traductions$")
    public void genererConflit() {
        final String uri = String.format("http://localhost:%d/git/%s", gitPort, context.get("projectName"));
        GitWrapper git = new GitWrapper(uri, credentials);
        git.setIdentity("Test User", "test.user@yopmail.com");
        git.addFile("fr.json", ResourcesUtils.read("data/i18n/fr_alt.json"));
        git.commitAndPush("Modifed fr.json (wording.hello + new key)");
        manager.save(git);
    }

    @Et("^le projet est en conflit$")
    public void projetEnConflit() {
        LOGGER.info("Building i18n files IMMEDIATELY");
        context.pop(ContextConstants.INTLN_FILE_BUILDER)
                .set("wording.hello", "Salutations")
                .build()
                .forEach(manager::notify);

        Path localPath = Paths.get(dataRoot, context.get("projectName"));
        try (Repository repo = new FileRepositoryBuilder()
                .findGitDir(localPath.toFile())
                .build()) {
            try (Git git = Git.wrap(repo)) {
                git.add().addFilepattern(".").call();
                git.commit().setAll(true).setAllowEmpty(false).setAmend(false).setMessage("Modify wording.hello").call();
                git.pull().setCredentialsProvider(credentials).setRebase(true).call();
                LOGGER.info("Git status:");
                Status st = git.status().call();
                LOGGER.info(String.join(", ", st.getConflicting()));
            }
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Et("^je corrige \"([^\"]*)\" par \"([^\"]*)\" en ([a-zç]+)$")
    public void jeCorrige(String keyId, String text, String lang) {
        IntlnFileBuilder builder = new IntlnFileBuilder();
        lang = Language.get(lang);
        Path localPath = Paths.get(dataRoot, context.get("projectName"));
        builder.setDestination(localPath.toString())
                .translate(lang, keyId, text)
                .build(l -> String.format("%s.tmp.json", l.toLowerCase()))
                .forEach(manager::notify);
    }
}
