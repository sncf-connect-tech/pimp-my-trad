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
import { FileAction } from '../actions/FileAction';
import { KeysetLangMapping } from '../models/KeysetLangMapping';
export const fileBrowserStateDefaults = {
    files: [],
    workingDir: '/',
    selected: KeysetLangMapping.empty()
};
export const fileBrowserReducer = (state, action) => {
    state = { ...fileBrowserStateDefaults, ...state };
    switch (action.type) {
        case FileAction.BROWSE:
            return {
                ...state,
                files: action.files,
                workingDir: action.workingDir
            };
        case FileAction.TOGGLE:
            return {
                ...state,
                selected: state.selected.toggleFile(action.file)
            };
        case FileAction.SELECT:
            return {
                ...state,
                selected: state.selected.addFile(action.file, action.language)
            };
        case FileAction.RESET_SELECTION:
            return {
                ...state,
                selected: KeysetLangMapping.empty()
            };
        default:
            return state;
    }
};
