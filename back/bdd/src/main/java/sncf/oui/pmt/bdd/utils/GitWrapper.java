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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitWrapper {

    private String uri;
    private CredentialsProvider creds;
    private Git git;

    public GitWrapper(String uri, CredentialsProvider creds) {
        this.uri = uri;
        this.creds = creds;
        this.git = tempClone();
    }

    public void close() {
        this.git.close();
    }

    public Git tempClone() {
        try {
            File localPath = File.createTempFile(String.format("tmp_clone_%d", System.currentTimeMillis()), "");
            if (!localPath.delete() || !localPath.mkdirs()) {
                throw new RuntimeException("Could not delete or create temporary file " + localPath);
            }
            return Git.cloneRepository()
                    .setURI(uri)
                    .setDirectory(localPath)
                    .setCredentialsProvider(creds)
                    .call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void setIdentity(String name, String email) {
        git.getRepository().getConfig().setString("user", null, "name", name);
        git.getRepository().getConfig().setString("user", null, "email", email);
    }

    public void addFile(String name, String content) {
        try {
            Path path = Paths.get(git.getRepository().getDirectory().getParent(), name);
            Files.write(path, content.getBytes(Charset.forName("UTF-8")));
            git.add().addFilepattern(name).call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkout(String branch) {
        try {
            git.checkout().setName(branch).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public String commitAndPush(String message) {
        try {
            git.commit().setMessage(message).call();
            Iterable<PushResult> res = git.push().setCredentialsProvider(creds).call();
            return res.iterator().next().getMessages();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
