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
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class KeysetTest {

    private static final Map<String, String> enKeys = ImmutableMap.of("message.greet", "Hello");
    private static final Map<String, String> enPatchKeys = ImmutableMap.of("message.greet", "Hi");
    private static final Map<String, String> frKeys = ImmutableMap.of("message.greet", "Bonjour");

    @Test
    public void fromMapsShouldCreateValidKeyset() throws KeyNotFoundException {
        Keyset k = Keyset.fromMaps(Language.EN, enKeys, enPatchKeys);
        assertEquals(k.getSupportedLanguages(), ImmutableSet.of(Language.EN));
        assertEquals(k.getKey("message.greet").getState(), KeyState.Conflict);
    }

    @Test
    public void shouldAllowAddingNewKeysEasily() throws KeyNotFoundException {
        Keyset k = new Keyset();
        k.withKey("new.key").setTheirs(Language.FR, "Nouvelle clé");
        assertEquals(k.getKey("new.key").getState(), KeyState.Conflict);
    }

    @Test
    public void shouldMergeCorrectly() throws KeyNotFoundException {
        Keyset one = Keyset.fromMaps(Language.FR, frKeys, Collections.emptyMap());
        Keyset two = Keyset.fromMaps(Language.EN, enKeys, enPatchKeys);
        one.merge(two);
        assertEquals(one.getSupportedLanguages(), ImmutableSet.of(Language.FR, Language.EN));
        assertEquals(one.getKey("message.greet").getState(), KeyState.Conflict);
        assertEquals(one.getKey("message.greet").getTranslation(Language.FR).get(0), "Bonjour");
    }
}
