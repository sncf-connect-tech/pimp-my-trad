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

import org.eclipse.jetty.server.Server;
import sncf.oui.pmt.bdd.utils.GenericContextResourceManager;
import sncf.oui.pmt.bdd.utils.TestContext;

public class TestServerMaster implements GenericContextResourceManager<Server> {

    private static final String SERVER = "_RUNNING_SERVER";

    private TestContext context;

    public TestServerMaster(TestContext context) {
        this.context = context;
    }

    @Override
    public void notify(Server server) {
        context.put(SERVER, server);
    }

    @Override
    public Server add(Server server) {
        try {
            server.start();
            return server;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void cleanup() {
        context.tryGet(SERVER, Server.class).ifPresent(server -> {
            try {
                server.stop();
                context.remove(SERVER);
            } catch (Exception ignored) {}
        });
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Server.class;
    }
}
