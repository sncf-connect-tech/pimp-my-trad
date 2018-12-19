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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IntlnFile {

    private Path path;

    private void createDir() {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createFile(String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public IntlnFile(String path) {
        this.path = Paths.get(path);
        createDir();
    }

    public IntlnFile(Path path) {
        this.path = path;
        createDir();
    }

    public IntlnFile(String path, String content) {
        this.path = Paths.get(path);
        createFile(content);
    }

    public IntlnFile(Path path, String content) {
        this.path = path;
        createFile(content);
    }

    public String getPath() {
        return path.toString();
    }
}
