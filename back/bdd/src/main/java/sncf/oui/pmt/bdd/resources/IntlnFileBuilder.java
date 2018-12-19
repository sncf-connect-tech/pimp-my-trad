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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntlnFileBuilder {

    private static final String FR = "FR";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MapType type = TypeFactory.defaultInstance()
            .constructMapType(HashMap.class, String.class, String.class);
    private Map<String, Map<String, String>> intln;
    private String destination = "";

    public IntlnFileBuilder() {
        intln = new HashMap<>();
    }

    private Map<String, String> getLang(String lang) {
        if (!intln.containsKey(lang)) {
            intln.put(lang, new HashMap<>());
        }
        return intln.get(lang);

    }

    public IntlnFileBuilder setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public IntlnFileBuilder removeLang(String lang) {
        intln.remove(lang);
        return this;
    }

    public IntlnFileBuilder set(String key, String value) {
        translate(FR, key, value);
        return this;
    }

    public IntlnFileBuilder translate(String lang, String key, String value) {
        getLang(lang).put(key, value);
        return this;
    }

    public IntlnFileBuilder putFrom(String lang, String json) {
        try {
            Map<String, String> parsed = mapper.readValue(json, type);
            getLang(lang).putAll(parsed);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<IntlnFile> build(Function<String, String> langMapper) {
        List<IntlnFile> l = intln.entrySet().stream().map((Map.Entry<String, Map<String, String>> e) -> {
            try {
                String fileName = langMapper.apply(e.getKey());
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e.getValue());
                return new IntlnFile(Paths.get(destination, fileName), json);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
        l.add(new IntlnFile(destination));
        return l;
    }

    public List<IntlnFile> build() {
        return build(lang -> String.format("%s.json", lang.toLowerCase()));
    }
}
