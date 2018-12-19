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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestContext {

    private Map<String, Object> store;

    public TestContext() {
        store = new HashMap<>();
    }

    public String get(String key) {
        return (String) store.get(key);
    }

    public <T> Optional<T> tryGet(String key, Class<T> clazz) {
        return Optional.ofNullable((T) store.get(key));
    }

    public <T> Optional<T> tryGet(TypedContextItem<T> item) {
        return tryGet(item.getKey(), item.getClazz());
    }

    public <T> T get(String key, Class<T> clazz) {
        final Object data = store.get(key);
        return (T) data;
    }

    public int getInt(String key) {
        return (int) store.get(key);
    }

    public void put(String key, Object value) {
        store.put(key, value);
    }

    public <T> void put(TypedContextItem<T> item, T value) {
        store.put(item.getKey(), value);
    }

    public <T> T get(TypedContextItem<T> item) {
        return get(item.getKey(), item.getClazz());
    }

    public boolean has(String key) {
        return store.containsKey(key);
    }

    public void remove(String key) {
        store.remove(key);
    }

    public <T> void remove(TypedContextItem<T> item) {
        store.remove(item.getKey());
    }

    public <T> T pop(String key, Class<T> clazz) {
        T item = get(key, clazz);
        remove(key);
        return item;
    }

    public <T> T pop(TypedContextItem<T> item) {
        T actual = get(item);
        remove(item);
        return actual;
    }
}
