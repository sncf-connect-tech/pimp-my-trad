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

import sncf.oui.pmt.DomainDrivenDesign;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@DomainDrivenDesign.ValueObject
public class Keyset {

    private final Map<String, Key> keys;
    private final Set<Language> supportedLanguages;
    private String name;
    private String id;

    public Keyset() {
        this.keys = new LinkedHashMap<>();
        this.supportedLanguages = new HashSet<>();
    }

    public static Keyset fromMaps(Language lang, Map<String, String> ours, Map<String, String> theirPatch) {
        Keyset set = new Keyset();
        set.supportedLanguages.add(lang);
        ours.forEach((key, value) -> {
            if (value == null) {
                set.withoutKey(key);
            } else {
                set.withKey(key).setOurs(lang, value);
            }
        });
        if (theirPatch != null) {
            theirPatch.forEach((key, value) -> set.withKey(key).setTheirs(lang, value));
        }
        return set;
    }

    private void withoutKey(String keyId) {
        keys.remove(keyId);
    }

    public Map<String, Key> getKeys() {
        return keys;
    }

    public Map<String, Key> getKeysByState(int stateFlag) {
        return keys.entrySet().stream()
                .filter(e -> (e.getValue().getState().flag() & stateFlag) > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Set<Language> getSupportedLanguages() {
        return supportedLanguages;
    }

    public Keyset setSupportedLanguages(Set<Language> supportedLanguages) {
        this.supportedLanguages.addAll(supportedLanguages);
        return this;
    }

    public Key withKey(String keyId) {
        if (!hasKey(keyId)) {
            keys.put(keyId, new Key());
        }
        return keys.get(keyId);
    }

    public Keyset merge(Keyset other) {
        this.supportedLanguages.addAll(other.supportedLanguages);
        other.keys.forEach((k, v) -> keys.merge(k, v, (value1, value2) -> {
            value1.mergeUnsafe(value2);
            return value1;
        }));
        return this;
    }

    public boolean hasKey(String keyId) {
        return keys.containsKey(keyId);
    }

    public Key getKey(String keyId) throws KeyNotFoundException {
        return Optional.ofNullable(keys.get(keyId)).orElseThrow(KeyNotFoundException::new);
    }

    public String getName() {
        return name;
    }

    public Keyset setName(String name) {
        this.name = name;
        return this;
    }

    public Keyset setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
