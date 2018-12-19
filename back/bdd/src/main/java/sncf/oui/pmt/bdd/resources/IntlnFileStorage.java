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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sncf.oui.pmt.bdd.utils.GenericContextResourceManager;
import sncf.oui.pmt.bdd.utils.TestContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class IntlnFileStorage implements GenericContextResourceManager<IntlnFile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntlnFileStorage.class);
    private static final String FILES = "_COLLECTED_FILES";

    private TestContext context;

    public IntlnFileStorage(TestContext context) {
        this.context = context;
    }

    private boolean removeDir(String path) {
        Path dir = Paths.get(path);
        boolean res;
        try {
            res = Files.walk(dir, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .allMatch(File::delete);
        } catch (IOException e) {
            res = false;
        }
        LOGGER.info(String.format("Attempt to delete %s : %s", dir.toString(), res ? "success" : "failure"));
        return res;
    }

    private Set<String> collectedFiles() {
        return context.tryGet(FILES, Set.class).orElse(new HashSet<>());
    }

    @Override
    public void notify(IntlnFile file) {
        Set<String> files = collectedFiles();
        files.add(file.getPath());
        LOGGER.info(String.format("Scheduling %s for deletion", file.getPath()));
        context.put(FILES, files);
    }

    @Override
    public IntlnFile add(IntlnFile file) {
        return file;
    }

    @Override
    public void cleanup() {
        collectedFiles().forEach(this::removeDir);
        context.remove(FILES);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == IntlnFile.class;
    }
}
