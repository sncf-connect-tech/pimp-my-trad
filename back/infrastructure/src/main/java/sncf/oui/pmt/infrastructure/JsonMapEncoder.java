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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.keyset.MapEncoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JsonMapEncoder implements MapEncoder {

    private ObjectMapper mapper;

    public JsonMapEncoder() {
        mapper = new ObjectMapper();
    }

    @Override
    public Mono<Map<String, String>> decode(List<String> lines) {
        MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        String str = lines.stream().map(String::trim).collect(Collectors.joining());
        try {
            return Mono.just(mapper.readValue(str, type));
        } catch (IOException e) {
            e.printStackTrace();
            return Mono.error(e);
        }
    }

    @Override
    public Mono<String> encode(Map<String, String> map) {
        try {
            return Mono.just(mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(map));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    @Override
    public boolean accepts(String format) {
        return format.equals("json");
    }
}
