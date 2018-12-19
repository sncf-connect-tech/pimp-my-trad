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

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;

public class Language {
    private static final Map<String, String> langMapping = ImmutableMap.of(
            "fran(ç|c)ais(e)?", "FR",
            "anglais(e)?", "EN",
            "italien(ne)?", "IT");
    public static String get(String readableValue) {
        return langMapping.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(readableValue) || readableValue.matches(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("no such language: %s", readableValue)));
    }
}
