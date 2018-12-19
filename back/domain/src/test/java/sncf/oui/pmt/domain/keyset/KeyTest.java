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


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyTest {

    @Test
    public void shouldComputeStateCorrectly() {
        Key k = new Key();
        k.setOurs(Language.FR, "toto [inprogress]");
        assertEquals(k.getState(), KeyState.InProgress);
        k.setOurs(Language.EN, "");
        assertEquals(k.getState(), KeyState.Todo);
        k.setTheirs(Language.FR, "tata");
        assertEquals(k.getState(), KeyState.Conflict);
    }

    @Test
    public void shouldIgnoreConflictsDuringMerge() {
        Key one = new Key();
        one.setOurs(Language.FR, "toto");
        one.setOurs(Language.EN, "foo");
        Key two = new Key();
        two.setOurs(Language.FR, "tata [inprogress]");
        one.mergeUnsafe(two);
        assertEquals(one.getState(), KeyState.InProgress);
        assertEquals(one.getTranslation(Language.FR).get(0), "tata [inprogress]");
        assertEquals(one.getTranslation(Language.EN).get(0), "foo");
    }
}
