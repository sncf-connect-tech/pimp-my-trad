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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@DomainDrivenDesign.ValueObject
public class Key {

    private KeyState state;
    private Map<Language, List<String>> translations;

    public KeyState getState() {
        return state;
    }

    public Map<Language, List<String>> getTranslations() {
        return translations;
    }

    public Set<Language> languageSet() {
        return translations.keySet();
    }

    public Key() {
        state = KeyState.Done;
        translations = new HashMap<>();
    }

    private void beforeSet(Language lang, String translation) {
        if (!translations.containsKey(lang)) {
            translations.put(lang, new LinkedList<>());
        }
        if (translation != null) {
            KeyState computed = parseStateTag(translation);
            this.state = computed.compareTo(state) > 0 ? state : computed;
        }
    }

    private void safeSet(Language lang, int index, String translated) {
        List<String> l = translations.get(lang);
        int diff = l.size() - index - 1;
        while (diff < 0) {
            l.add("");
            diff = l.size() - index - 1;
        }
        if (translated == null) {
            l.set(index, "");
        } else {
            l.set(index, translated);
        }
    }

    public Key setOurs(Language lang, String translation) {
        beforeSet(lang, translation);
        safeSet(lang, 0, translation);
        return this;
    }

    public Key setTheirs(Language lang, String translation) {
        beforeSet(lang, translation);
        safeSet(lang, 1, translation);
        safeSet(lang, 0, getTranslation(lang).get(0) + KeyState.Conflict.asTag());
        state = KeyState.Conflict;
        return this;
    }

    public Key mergeUnsafe(Key key) {
        KeyState computed = key.computedState();
        state = computed.compareTo(state) > 0 ? state : computed;
        translations.putAll(key.translations);
        return this;
    }

    private KeyState computedState() {
        return translations.entrySet().stream()
                .map((entry) -> entry.getValue().size() > 1 ? KeyState.Conflict : parseStateTag(entry.getValue().get(0)))
                .min(Enum::compareTo)
                .get();
    }

    private static Pattern tagPattern() {
        StringBuilder rawPat = new StringBuilder("\\[(");
        String possible = Arrays.stream(KeyState.values())
                .map(Enum::toString)
                .collect(Collectors.joining("|"));
        rawPat.append(possible)
                .append(")\\]");
        return Pattern.compile(rawPat.toString(), Pattern.CASE_INSENSITIVE);
    }

    private static KeyState parseStateTag(String translation) {
        if (translation.trim().length() == 0) {
            return KeyState.Todo;
        }
        Pattern pat = tagPattern();
        Matcher matcher = pat.matcher(translation);
        KeyState state = KeyState.Done;
        while (matcher.find()) {
            String match = matcher.group(1).toLowerCase();
            KeyState parsed = Arrays.stream(KeyState.values())
                    .filter(val -> val.toString().toLowerCase().equals(match))
                    .findFirst()
                    .get();
            state = parsed.compareTo(state) > 0 ? state : parsed;
        }
        return state;
    }

    public List<String> getTranslation(Language lang) {
        return translations.get(lang);
    }

    public String getCleanTranslation(Language lang) {
        return tagPattern().matcher(translations.get(lang).get(0)).replaceAll("");
    }
}

