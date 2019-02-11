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

package sncf.oui.pmt.infrastructure;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.api.client.json.Json;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.keyset.MapEncoder;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class JsonMapEncoder implements MapEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMapEncoder.class);
    private final JsonFactory factory;

    public JsonMapEncoder() {
        factory = new JsonFactory();
    }

    @Override
    public Mono<Map<String, String>> decode(List<String> lines) {
        String str = lines.stream().map(String::trim).collect(Collectors.joining());
        Map<String, String> result = new HashMap<>();
        Stack<String> path = new Stack<>();
        String previousName = null;
        try {
            JsonParser parser = factory.createParser(str);
            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token == null) {
                    break;
                }
                switch (token) {
                    case START_OBJECT:
                        if (previousName != null) path.push(previousName);
                        break;
                    case FIELD_NAME:
                        previousName = parser.getCurrentName();
                        break;
                    case VALUE_STRING:
                        if (previousName != null) {
                            String key = Stream.concat(path.stream(), Stream.of(previousName))
                                    .collect(Collectors.joining("/"));
                            result.put(key, parser.getValueAsString());
                        }
                        break;
                    case END_OBJECT:
                        if (!path.empty()) path.pop();
                        break;
                }
            }
            return Mono.just(result);
        } catch (IOException e) {
            LOGGER.error("An error occured while parsing: ", e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<String> encode(Map<String, String> map) {
        List<String> sortedKeys = Lists.newArrayList(map.keySet());
        Collections.sort(sortedKeys);
        StringWriter writer = new StringWriter();

        try (JsonGenerator generator = factory.createGenerator(writer)) {

            List<String> prevList = Collections.emptyList();
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
            generator.writeStartObject();

            for (String cur : sortedKeys) {
                List<String> curList = Lists.newArrayList(cur.split("/"));

                final List<String> finalPrevList = prevList;
                Optional<AbstractMap.SimpleEntry<Integer, Boolean>> pair = IntStream
                        .range(0, Math.max(curList.size() - 1, prevList.size() - 1)) // if only the last key differs, there is no need to end an object
                        .mapToObj(i -> {
                            return new AbstractMap.SimpleEntry<>(i, curList.get(i) != null && curList.get(i).equals(finalPrevList.get(i)));
                        })
                        .filter(p -> p.getValue().equals(false))
                        .findFirst();

                // not the same object
                pair.ifPresent(entry -> {
                    List<String> subList = curList.subList(entry.getKey(), curList.size() - 1);
                    try {
                        for (int i = 0; i < entry.getKey(); i++) generator.writeEndObject();
                        for (String key : subList) generator.writeObjectFieldStart(key);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                generator.writeStringField(curList.get(curList.size() - 1), map.get(cur));
                prevList = curList;
            }

            generator.writeEndObject();

        } catch (Exception e) {
            LOGGER.error("An error occured while encoding: ", e);
            return Mono.error(e);
        }
        return Mono.just(writer.toString());
    }

    @Override
    public boolean accepts(String format) {
        return format.equals("json");
    }
}