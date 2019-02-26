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
import {Keyset} from './Keyset';

export function allKeysetsWithNames(projects) {
    return projects.reduce((res, project) => ({
        ...res,
        [project.name]: project.keysets.slice()
    }), {});
}

export function allKeysets(projects) {
    return projects.reduce((res, project) => res.concat(project.keysets), []);
}

export class Project {
    static from(object) {
        return Object.assign(new Project(), {
            name: object.name,
            keysets: object.keysets != null ? object.keysets.map(Keyset.from) : []
        });
    }

    setKey(keysetId, targetId, newKey) {
        return Project.from({
            name: this.name,
            keysets: this.keysets.map(k => k.id === keysetId ? k.setKey(targetId, newKey) : k)
        });
    }

    deleteKey(keysetId, targetId) {
        return Project.from({
            name: this.name,
            keysets: this.keysets.map(k => k.id === keysetId ? k.deleteKey(targetId) : k)
        });
    }

    setKeyset(keyset, isNew) {
        return Project.from({
            name: this.name,
            keysets: isNew === true ?
                this.keysets.concat(keyset) :
                this.keysets.map(k => k.id === keyset.id ? keyset : k)
        });
    }
}
