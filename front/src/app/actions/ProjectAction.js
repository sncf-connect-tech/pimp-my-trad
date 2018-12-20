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
import {keysetService} from '../services/KeysetService';
import {closeDialog} from './DialogAction';
import {loadingDone, setLoading} from './UIAction';

export var ProjectAction;
(function (ProjectAction) {
    ProjectAction["ADD_PROJECT"] = "ADD_PROJECT";
    ProjectAction["SET_PROJECTS"] = "SET_PROJECTS";
    ProjectAction["SELECT_PROJECT"] = "SELECT_PROJECT";
    ProjectAction["SET_KEYSET"] = "SET_KEYSET";
    ProjectAction["UPDATE_KEYSETS"] = "UPDATE_KEYSETS";
    ProjectAction["SET_KEY"] = "SET_KEY";
    ProjectAction["SEARCH_KEY"] = "SEARCH_KEY";
    ProjectAction["SELECT_ALL"] = "SELECT_ALL";
    ProjectAction["FILTER_STATE"] = "FILTER_STATE";
    ProjectAction["ERROR"] = "ERROR";
})(ProjectAction || (ProjectAction = {}));

export function getProjects() {
    return dispatch => {
        dispatch(setLoading(true));
        return projectService.getProjects()
            .then(projects => dispatch({
                type: ProjectAction.SET_PROJECTS,
                projects: projects
            }))
            .catch(err => dispatch({
                type: ProjectAction.ERROR,
                err: err
            }))
            .then(loadingDone(dispatch));
    };
}

export function selectProject(name) {
    return {
        type: ProjectAction.SELECT_PROJECT,
        name: name
    };
}

export function selectAll() {
    return {
        type: ProjectAction.SELECT_ALL
    };
}

export function createKeyset() {
    return (dispatch, getState) => {
        let projectName = getState().main.selected;
        if (projectName == null) {
            return Promise.resolve({
                type: ProjectAction.ERROR
            });
        }
        else {
            dispatch(setLoading(true));
            return keysetService.createKeyset(projectName, getState().selection.selected)
                .then(res => dispatch(setKeyset(projectName || '', res, true)))
                .then(loadingDone(dispatch));
        }
    };
}

export function importProject(repo) {
    return dispatch => {
        dispatch(setLoading(true));
        return projectService.importProject(repo)
            .then(project => {
                dispatch({
                    type: ProjectAction.ADD_PROJECT,
                    project: project
                });
                dispatch(setLoading(false));
                dispatch(closeDialog());
                dispatch(selectProject(project.name));
                return project;
            })
            .catch(error => {
                dispatch(setLoading(false));
                return dispatch({type: ProjectAction.ERROR, message: error.error});
            });
    };
}

export function setKey(keyId, keysetId, projectName, language, translation) {
    return dispatch => {
        dispatch(setLoading(true));
        return keysetService.setKey(projectName, keysetId, keyId, language, translation)
            .then(key => {
                dispatch({
                    type: ProjectAction.SET_KEY,
                    project: projectName,
                    keysetId: keysetId,
                    id: keyId,
                    key: key
                });
                return true;
            })
            .catch(err => false)
            .then(loadingDone(dispatch));
    };
}

export function setKeyset(project, keyset, isNew) {
    return {
        type: ProjectAction.SET_KEYSET,
        project,
        keyset,
        isNew
    };
}

export function updateKeysets(keysets) {
    return {
        type: ProjectAction.UPDATE_KEYSETS,
        keysets
    };
}

export function addFiles(projectName, keysetId, mapping, overwrite = true) {
    return dispatch => {
        dispatch(setLoading(true));
        return keysetService.addFiles(projectName, keysetId, mapping, overwrite)
            .then(keyset => dispatch(setKeyset(projectName, keyset, false)))
            .then(loadingDone(dispatch));
    };
}

export function addSelectedFiles(keysetId, overwrite = true) {
    return (dispatch, getState) => {
        let state = getState();
        if (state.main.selected === null) {
            return Promise.resolve(dispatch({type: ProjectAction.ERROR, message: 'No project selected'}));
        }
        else {
            let projectName = state.main.selected;
            dispatch(setLoading(true));
            return keysetService
                .addFiles(projectName, keysetId, state.selection.selected, overwrite)
                .then(keyset => dispatch(setKeyset(projectName, keyset, false)))
                .then(loadingDone(dispatch));
        }
    };
}

export function translateNow(projectName, keysetId, keyId, language) {
    return dispatch => {
        dispatch(setLoading(true));
        return keysetService.translateNow(projectName, keysetId, keyId, language)
            .then(key => {
                dispatch(setLoading(false));
                dispatch({
                    type: ProjectAction.SET_KEY,
                    project: projectName,
                    keysetId: keysetId,
                    id: keyId,
                    key: key
                });
                return key.translation(language);
            })
            .catch(err => {
                dispatch(setLoading(false));
                return '';
            });
    };
}

export function syncAll() {
    return dispatch => {
        dispatch(setLoading(true));
        return projectService.syncAll()
            .then(noConflicts => {
                noConflicts ?
                    dispatch(getProjects())
                        .then(_ => dispatch(closeDialog())) :
                    dispatch(getProjects())
                        .then(_ => dispatch(filterState('conflict')));
            });
    };
}

export function searchKey(query) {
    return {
        type: ProjectAction.SEARCH_KEY,
        query: query
    };
}

export function filterState(state) {
    return {
        type: ProjectAction.FILTER_STATE,
        state: state
    };
}
