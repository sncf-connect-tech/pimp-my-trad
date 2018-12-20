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
import {applyMiddleware, combineReducers, compose, createStore} from 'redux';
import {mainReducer, mainStateDefaults} from './reducers/mainReducer';
import thunk from 'redux-thunk';
import {fileBrowserReducer, fileBrowserStateDefaults} from './reducers/fileBrowserReducer';
import {dialogReducer, dialogStateDefaults} from './reducers/dialogReducer';
import {importExportReducer, importExportStateDefaults} from './reducers/importExportReducer';
import {authReducer, authStateDefaults} from './reducers/authReducer';

export const defaultState = {
    main: mainStateDefaults,
    selection: fileBrowserStateDefaults,
    dialogs: dialogStateDefaults,
    importsExports: importExportStateDefaults,
    location: window.location,
    auth: authStateDefaults
};

export function createAppStore(bootState = {}) {
    // tslint:disable-next-line
    let composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
    let reducer = combineReducers({
        main: mainReducer,
        selection: fileBrowserReducer,
        dialogs: dialogReducer,
        importsExports: importExportReducer,
        auth: authReducer,
        location: (s, a) => ({...window.location, ...s})
    });
    return createStore(reducer, Object.assign({}, defaultState, bootState), composeEnhancers(applyMiddleware(thunk)));
}
