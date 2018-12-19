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

package sncf.oui.pmt.bdd;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.eclipse.jetty.server.Server;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import sncf.oui.pmt.bdd.resources.GitModificationTracker;
import sncf.oui.pmt.bdd.resources.IntlnFileStorage;
import sncf.oui.pmt.bdd.resources.MongoContextDatabase;
import sncf.oui.pmt.bdd.resources.TestServerMaster;
import sncf.oui.pmt.bdd.utils.*;

import java.util.List;

@Configuration
@PropertySource(value = "classpath:application.yml")
public class TestConfig {

    public static final String TEST_USER = "test_user";

    @Bean
    public TestContext testContext() {
        return new TestContext();
    }

    @Bean
    public MongoDatabase mongoDatabase(@Value("${mongohost:localhost}") String mongoHost) {
        return new MongoClient(mongoHost).getDatabase("pimpmytrad");
    }

    @Bean
    public ApiRequester requester(@Value("${apiHost}") String apiHost) {
        return new ApiRequester(apiHost, 8080);
    }

    @Bean
    public GenericContextResourceManager mongoUtils(MongoDatabase database, TestContext context) {
        return new MongoContextDatabase(database, context);
    }

    @Bean
    public GenericContextResourceManager storage(TestContext context) {
        return new IntlnFileStorage(context);
    }

    @Bean
    public GenericContextResourceManager serverMaster(TestContext context) {
        return new TestServerMaster(context);
    }

    @Bean
    public GenericContextResourceManager gitModTracker(TestContext context, @Value("${gitPort:8888}") int gitPort) {
        return new GitModificationTracker(context,  String.format("http://localhost:%d/reset/", gitPort));
    }

    @Bean
    public ContextResourceManager resourceManager(List<GenericContextResourceManager> managers) {
        return new ContextResourceManager(managers);
    }

    @Bean
    public Server gitServer(@Value("${gitPort:8888}") int gitPort) throws Throwable {
        return new MockGitServerBuilder()
                .addFile("fr.json", ResourcesUtils.read("data/i18n/fr.json"))
                .addFile("en.json", ResourcesUtils.read("data/i18n/en.json"))
                .commit("Initial commit")
                .build(gitPort);
    }

    @Bean
    public CredentialsProvider userProvider() {
        return new UsernamePasswordCredentialsProvider(TEST_USER, "password");
    }

}
