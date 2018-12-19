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
import { Project } from '../models/Project';
import { and, authHeaders, jsonHeaders, rejectHttpErrors } from './commons';
import { Keyset } from '../models/Keyset';
class ProjectService {
    getProjects() {
        return authHeaders()
            .then(headers => fetch('/projects/', { headers }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(res => res.map(Project.from));
    }
    getProjectFiles(projectName, path) {
        return authHeaders()
            .then(headers => fetch(`/projects/${projectName}/files?path=${path}`, { headers }))
            .then(rejectHttpErrors)
            .then(res => res.json());
    }
    importProject(repo) {
        return jsonHeaders()
            .then(and(authHeaders()))
            .then(headers => fetch('/projects/', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ url: repo })
        }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(Project.from);
    }
    syncAll() {
        return authHeaders()
            .then(headers => fetch('/projects/sync', {
            method: 'POST',
            headers
        }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(res => !res.conflicts);
    }
    exportProject(projectName) {
        return authHeaders()
            .then(headers => fetch(`/projects/${projectName}/export`, {
            method: 'GET',
            headers
        }))
            .then(rejectHttpErrors)
            .then(res => res.json());
    }
    exportAll() {
        return authHeaders()
            .then(headers => fetch(`/projects/export`, {
            method: 'GET',
            headers
        }))
            .then(rejectHttpErrors)
            .then(res => res.json());
    }
    recentExports(weekNum = 0) {
        return authHeaders()
            .then(headers => fetch(`/projects/recentExports?weekNum=${weekNum}`, { headers }))
            .then(rejectHttpErrors)
            .then(res => res.json());
    }
    importTranslations(id, language, file) {
        let formData = new FormData();
        formData.append('file', file, file.name);
        formData.append('language', language.toString());
        return authHeaders()
            .then(headers => fetch(`/projects/import/${id}`, {
            method: 'POST',
            body: formData,
            headers
        }))
            .then(rejectHttpErrors)
            .then(res => res.json())
            .then(res => res.map(Keyset.from));
    }
}
export const projectService = new ProjectService();
