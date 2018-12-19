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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sncf.oui.pmt.bdd.utils.GenericContextResourceManager;
import sncf.oui.pmt.bdd.utils.GitWrapper;
import sncf.oui.pmt.bdd.utils.TestContext;

import java.io.IOException;

public class GitModificationTracker implements GenericContextResourceManager<GitWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitModificationTracker.class);
    private static final String GIT = "_GIT_INSTANCE";

    private TestContext context;
    private String gitMockReset;

    public GitModificationTracker(TestContext context, String gitMockReset) {
        this.context = context;
        this.gitMockReset = gitMockReset;
    }

    @Override
    public void notify(GitWrapper resource) {
        context.put(GIT, resource);
    }

    @Override
    public GitWrapper add(GitWrapper resource) {
        return resource;
    }

    @Override
    public void cleanup() {
        context.tryGet(GIT, GitWrapper.class).ifPresent(git -> {
            git.close();
            try {
                resetMock();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            } finally {
                context.remove(GIT);
            }
        });
    }

    private void resetMock() throws IOException {
        HttpGet get = new HttpGet(gitMockReset);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse res = client.execute(get)) {
                LOGGER.info(res.getStatusLine().toString());
            }
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return GitWrapper.class == clazz;
    }

    @Override
    public int priority() {
        return 999;
    }
}
