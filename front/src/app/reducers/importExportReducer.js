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
import {ImportExportAction} from '../actions/ImportExportAction';

export const importExportStateDefaults = {
    exported: null,
    allExports: [],
    selectedExport: null
};
export const importExportReducer = (state, action) => {
    state = {...importExportStateDefaults, ...state};
    switch (action.type) {
        case ImportExportAction.EXPORT_PROJECT:
            return {
                ...state,
                exported: action.exported,
                allExports: state.allExports.concat(action.exported.metadata)
            };
        case ImportExportAction.SET_EXPORTS:
            return {
                ...state,
                allExports: action.exports
            };
        case ImportExportAction.IMPORT_TRANSLATIONS:
            return {
                ...state,
                selectedExport: action.exportId
            };
        default:
            return state;
    }
};
