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
import {projectService} from '../services/ProjectService';
import {selectProject, selectAll} from './ProjectAction';
import {loadingDone, setLoading} from './UIAction';

export var FileAction = {
    BROWSE: "BROWSE",
    SELECT: "SELECT_FILE",
    TOGGLE: "TOGGLE_FILE",
    RESET_SELECTION: "RESET_SELECTION",
    ERROR: "ERROR",
};

export function browseProjectFiles(projectName, path) {
    return dispatch => {
        dispatch(setLoading(true));
        return projectService.getProjectFiles(projectName, path)
            .then(files => dispatch({
                type: FileAction.BROWSE,
                files: files,
                workingDir: path
            }))
            .then(_ => dispatch(selectProject(projectName)))
            .catch(err => dispatch({
                type: FileAction.ERROR,
                err
            }))
            .then(loadingDone(dispatch));
    };
}

export function browseProject(projectName) {
    return (dispatch, getState) => projectName === null ?
        Promise.resolve(dispatch({
            type: FileAction.BROWSE,
            files: getState().main.projects.map(p => p.name),
            workingDir: '/',
        }))
            .then(() => dispatch(selectAll())) :
        dispatch(browseProjectFiles(projectName, '/'));
}

export function browseFiles(path) {
    return (dispatch, getState) => dispatch(browseProjectFiles(getState().main.selected || '', path));
}

export function toggleFileSelect(path) {
    return {
        type: FileAction.TOGGLE,
        file: path
    };
}

export function selectFile(path, language) {
    return {
        type: FileAction.SELECT,
        file: path,
        language: language
    };
}

export function resetSelection() {
    return {
        type: FileAction.RESET_SELECTION
    };
}
