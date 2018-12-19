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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ContextResourceManager implements GenericContextResourceManager<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextResourceManager.class);
    private List<GenericContextResourceManager> managers;

    public ContextResourceManager(List<GenericContextResourceManager> managers) {
        managers.sort((m1, m2) -> m2.priority() - m1.priority());
        this.managers = managers;
    }

    private Optional<GenericContextResourceManager> suitableManager(Class<?> clazz) {
        return managers.stream()
                .filter(manager -> manager.supports(clazz))
                .findFirst();
    }

    @Override
    public void notify(Object resource) {
        suitableManager(resource.getClass()).ifPresent(manager -> manager.notify(resource));
    }

    @Override
    public Object add(Object resource) {
        suitableManager(resource.getClass()).ifPresent(manager -> manager.add(resource));
        return resource;
    }

    @Override
    public void cleanup() {
        managers.forEach(manager -> {
            LOGGER.info(String.format("Cleaning up resources for %s", manager.getClass().getSimpleName()));
            manager.cleanup();
        });
    }

    public <T> void cleanup(Class<T> clazz) {
        suitableManager(clazz).ifPresent(GenericContextResourceManager::cleanup);
    }

    @Override
    public boolean supports(Class<?> ignored) {
        return true;
    }

    @Override
    public Object save(Object resource) {
        suitableManager(resource.getClass()).ifPresent(manager -> manager.save(resource));
        return resource;
    }
}
