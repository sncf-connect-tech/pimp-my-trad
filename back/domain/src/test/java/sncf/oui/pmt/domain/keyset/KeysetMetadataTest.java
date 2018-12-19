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

package sncf.oui.pmt.domain.keyset;

import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.FileHandler;
import sncf.oui.pmt.domain.TranslateService;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeysetMetadataTest {

    @Mock private TranslateService translateService;
    @Mock private FileHandler fileHandler;

    private static final String basePath = String.join(File.separator, "", "path", "to", "test-project");
    private static final String enPath = String.join(File.separator, basePath, "dir", "en.json");
    private static final String frPath = String.join(File.separator, basePath, "dir", "fr.json");
    private static final String itPath = String.join(File.separator, basePath, "dir", "it.json");
    private static final Map<String, String> enKeys = ImmutableMap.of("message.greet", "Hello");
    private static final Map<String, String> enPatchKeys = ImmutableMap.of("message.greet", "Hi");
    private static final Map<String, String> frKeys = ImmutableMap.of("message.greet", "Bonjour");

    public KeysetMetadataTest() {
        MockitoAnnotations.initMocks(this);
        setupFileHandler();
    }

    private void setupFileHandler() {
        when(fileHandler.writeDiff(Mockito.anyString(),
                Mockito.anyMapOf(String.class, String.class))).thenReturn(Mono.empty());
        when(fileHandler.writeFull(Mockito.anyString(),
                Mockito.anyMapOf(String.class, String.class))).thenReturn(Mono.empty());

        when(fileHandler.hasConflict(frPath)).thenReturn(Mono.just(false));
        when(fileHandler.readOurVersion(frPath)).thenReturn(Mono.just(frKeys));
        when(fileHandler.readTheirPatch(frPath)).thenReturn(Mono.just(Collections.emptyMap()));

        when(fileHandler.hasConflict(enPath)).thenReturn(Mono.just(true));
        when(fileHandler.readOurVersion(enPath)).thenReturn(Mono.just(enKeys));
        when(fileHandler.readTheirPatch(enPath)).thenReturn(Mono.just(enPatchKeys));
    }

    private KeysetMetadata keysetMeta() {
        Map<Language, String> files = new HashMap<>();
        files.put(Language.EN, "/dir/en.json");
        files.put(Language.FR, "/dir/fr.json");
        return new KeysetMetadata(null, files,"/path/to/test-project", translateService, fileHandler);
    }

    private KeysetMetadata keysetMetaSingle() {
        Map<Language, String> files = ImmutableMap.of(Language.FR, "/dir/fr.json");
        return new KeysetMetadata(null, files,"/path/to/test-project", translateService, fileHandler);
    }

    @Test
    public void shouldGuessNameReliably() {
        assertEquals(keysetMeta().nameSet(), "/dir/*.json");
        assertEquals(keysetMetaSingle().nameSet(), "/dir/fr.json");
    }

    @Test
    public void shouldComputeAbsolutePath() {
        assertEquals(keysetMeta().getFile(Language.FR), frPath);
    }

    @Test
    public void shouldReturnAllFiles() {
        assertEquals(keysetMeta().getFiles(), ImmutableMap.of(Language.FR, frPath, Language.EN, enPath));
    }

    @Test
    public void shouldInitNewFileIfOverwrite() {
        keysetMeta().addFile(Language.IT, "/dir/it.json", false).block();
        verify(fileHandler, never()).writeFull(itPath, ImmutableMap.of("message.greet", ""));
        keysetMeta().addFile(Language.IT, "/dir/it.json", true).block();
        verify(fileHandler).writeFull(itPath, ImmutableMap.of("message.greet", ""));
    }
}
