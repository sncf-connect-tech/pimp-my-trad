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

package sncf.oui.pmt.domain.project;

import sncf.oui.pmt.DomainDrivenDesign;
import sncf.oui.pmt.domain.keyset.Keyset;

import java.util.LinkedList;
import java.util.List;

@DomainDrivenDesign.ValueObject
public class Project {

    private final List<Keyset> keysets;
    private final String name;

    public Project(String name) {
        this.name = name;
        this.keysets = new LinkedList<>();
    }

    public List<Keyset> getKeysets() {
        return keysets;
    }

    public String getName() {
        return name;
    }

    public Project addKeyset(Keyset keyset) {
        keysets.add(keyset);
        return this;
    }
}
