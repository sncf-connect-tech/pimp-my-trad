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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sncf.oui.pmt.domain.FileHandler;
import sncf.oui.pmt.domain.TranslateService;

import java.util.Map;

@Component
public class KeysetMetadataFactory {

    private FileHandler fileHandler;
    private TranslateService translateService;

    @Autowired
    public KeysetMetadataFactory(FileHandler fileHandler, TranslateService translateService) {
        this.fileHandler = fileHandler;
        this.translateService = translateService;
    }

    public KeysetMetadata create(String id, Map<Language, String> map) {
        return new KeysetMetadata(id, map, null, translateService, fileHandler); // that's bad :(
    }

    public KeysetMetadata createNew(Map<Language, String> map, String root) {
        return new KeysetMetadata(null, map, root, translateService, fileHandler);
    }
}
