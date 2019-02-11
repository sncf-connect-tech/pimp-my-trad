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

package sncf.oui.pmt.bdd.utils;

import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MockGitServerBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockGitServerBuilder.class);
    public static final String ROLE = "user";
    private Repository repo;
    private RevCommit initialState;

    public MockGitServerBuilder() {
        repo = buildRepo();
    }

    public Server build(int port) {
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(buildServlet()), "/git/*");

        ConstraintSecurityHandler securityHandler = securityHandler("/git/*");
        securityHandler.setHandler(handler);

        ServletHandler controlHandler = new ServletHandler();
        controlHandler.addServletWithMapping(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                boolean res = resetRepo();
                LOGGER.info("Repo reset successful? " + (res ? "YES" : "NO"));
                resp.setStatus(res ?
                        HttpServletResponse.SC_OK :
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println(initialState.getName());
            }
        }), "/reset/*");

        HandlerCollection collection = new HandlerCollection();
        collection.addHandler(securityHandler);
        collection.addHandler(controlHandler);

        LOGGER.info(String.format("Port for mock git server: %d", port));
        Server server = new Server(port);
        server.setHandler(collection);
        return server;
    }

    private boolean resetRepo() {
        try (Git git = new Git(repo)) {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(initialState.getName()).call();
            return true;
        } catch (GitAPIException e) {
            LOGGER.error("an error occured while resetting", e);
            return false;
        }
    }

    public MockGitServerBuilder addFile(String name, String content) throws IOException, GitAPIException {
        try (Git git = new Git(repo)) {
            Path path = Paths.get(repo.getDirectory().getParent(), name);
            Files.write(path, content.getBytes(Charset.forName("UTF-8")));
            git.add().addFilepattern(name).call();
        }
        return this;
    }

    public MockGitServerBuilder commit(String message) throws GitAPIException {
        try (Git git = new Git(repo)) {
            initialState = git.commit().setMessage(message).call();
        }
        return this;
    }

    public MockGitServerBuilder checkout(String branch) throws GitAPIException {
        try (Git git = new Git(repo)) {
            git.checkout().setCreateBranch(true).setName(branch).call();
        }
        return this;
    }

    private GitServlet buildServlet() {
        GitServlet servlet = new GitServlet();
        servlet.setRepositoryResolver((req, name) -> {
            LOGGER.info(String.format("Accessing repo %s", name));
            repo.incrementOpen();
            return repo;
        });
        return servlet;
    }

    private Repository buildRepo() {
        try {
            File localPath = File.createTempFile("test_repository", "");
            if (!localPath.delete() || !localPath.mkdirs()) {
                throw new IOException("Could not delete or create temporary file " + localPath);
            }
            Repository repo = FileRepositoryBuilder.create(new File(localPath, ".git"));
            repo.create();
            return repo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ConstraintSecurityHandler securityHandler(String pathSpec) {
        ConstraintSecurityHandler handler = new ConstraintSecurityHandler();
        handler.addConstraintMapping(constraintMapping(pathSpec));
        handler.setAuthenticator(new BasicAuthenticator());
        handler.setLoginService(new AbstractLoginService() {
            @Override
            protected String[] loadRoleInfo(UserPrincipal user) {
                return new String[] {ROLE};
            }

            @Override
            protected UserPrincipal loadUserInfo(String username) {
                return new UserPrincipal(username, new Password("password"));
            }
        });
        handler.addRole(ROLE);
        return handler;
    }

    private ConstraintMapping constraintMapping(String pathSpec) {
        Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[] {ROLE});
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec(pathSpec);
        mapping.setConstraint(constraint);
        return mapping;
    }

}