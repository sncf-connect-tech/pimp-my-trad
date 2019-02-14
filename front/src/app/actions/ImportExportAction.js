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
import {closeDialog, showImportDialog} from './DialogAction';
import {updateKeysets} from './ProjectAction';
import {loadingDone, setLoading} from './UIAction';

export var ImportExportAction = {
    ERROR: "ERROR",
    EXPORT_PROJECT: "EXPORT_PROJECT",
    SET_EXPORTS: "SET_EXPORTS",
    IMPORT_TRANSLATIONS: "IMPORT_TRANSLATIONS",
};

export function exportSelectedProject() {
    return (dispatch, getState) => {
        let projectName = getState().main.selected;
        dispatch(setLoading(true));
        let promise = projectName === null ? projectService.exportAll() : projectService.exportProject(projectName);
        return promise
            .then(exported => dispatch({
                type: ImportExportAction.EXPORT_PROJECT,
                exported
            }))
            .then(loadingDone(dispatch));
    };
}

export function showRecentExports() {
    return dispatch => {
        dispatch(setLoading(true));
        return projectService.recentExports()
            .then(res => dispatch({
                type: ImportExportAction.SET_EXPORTS,
                exports: res
            }))
            .then(loadingDone(dispatch));
    };
}

export function startImport(exportId) {
    return dispatch => {
        dispatch({
            type: ImportExportAction.IMPORT_TRANSLATIONS,
            exportId: exportId
        });
        dispatch(showImportDialog());
        return Promise.resolve();
    };
}

export function completeImport(language, file) {
    return (dispatch, getState) => {
        dispatch(setLoading(true));
        let id = getState().importsExports.selectedExport;
        return id === null ?
            Promise.resolve(dispatch({type: ImportExportAction.ERROR})) :
            projectService.importTranslations(id, language, file)
                .then(res => {
                    dispatch(updateKeysets(res));
                    return dispatch(closeDialog());
                })
                .then(loadingDone(dispatch));
    };
}
