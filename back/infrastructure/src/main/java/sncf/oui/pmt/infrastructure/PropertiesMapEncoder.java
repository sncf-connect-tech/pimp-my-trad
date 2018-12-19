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

import sncf.oui.pmt.domain.keyset.MapEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PropertiesMapEncoder implements MapEncoder {

    @Override
    public Mono<Map<String, String>> decode(List<String> lines) {
        return Flux.fromIterable(lines).reduce(new HashMap<String, String>(), (acc, line) -> {
            int i = line.indexOf("=");
            if (i > 0) {
                acc.put(line.substring(0, i), line.substring(i+1));
            }
            return acc;
        });
    }

    @Override
    public Mono<String> encode(Map<String, String> map) {
        return Flux.fromIterable(map.entrySet())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean accepts(String format) {
        return format.equals("properties");
    }
}
