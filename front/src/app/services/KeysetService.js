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
import {Key, Keyset} from '../models/Keyset';
import {and, authHeaders, jsonHeaders, rejectHttpErrors} from './commons';

class KeysetService {
    createKeyset(projectName, mapping) {
        return jsonHeaders()
            .then(and(authHeaders()))
            .then(headers => fetch(`/projects/${projectName}/keysets/`, {
                method: 'post',
                headers: headers,
                body: JSON.stringify(mapping.toObject())
            }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(Keyset.from);
    }

    setKey(projectName, keysetId, keyId, language, translation) {
        return jsonHeaders()
            .then(and(authHeaders()))
            .then(headers => fetch(`/projects/${projectName}/keysets/${keysetId}/keys/${encodeURIComponent(keyId)}`, {
                method: 'put',
                headers: headers,
                body: JSON.stringify({
                    language: language,
                    translation: translation
                })
            }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(Key.from);
    }

    addFiles(projectName, keysetId, mapping, overwrite = true) {
        return jsonHeaders()
            .then(and(authHeaders()))
            .then(headers => fetch(`/projects/${projectName}/keysets/${keysetId}?overwrite=${overwrite}`, {
                method: 'put',
                headers: headers,
                body: JSON.stringify(mapping.toObject())
            }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(Keyset.from);
    }

    translateNow(projectName, keysetId, keyId, language) {
        return jsonHeaders()
            .then(and(authHeaders()))
            .then(headers => fetch(`/projects/${projectName}/keysets/${keysetId}/keys/${encodeURIComponent(keyId)}/translate`, {
                method: 'post',
                headers: headers,
                body: JSON.stringify({language})
            }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(Key.from);
    }
}

export const keysetService = new KeysetService();
