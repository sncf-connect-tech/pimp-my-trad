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

public class TypedContextItem<T> {
    private final String key;
    private final Class<T> clazz;

    public TypedContextItem(String key, Class<T> clazz) {
        this.key = key;
        this.clazz = clazz;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getKey() {
        return key;
    }
}
