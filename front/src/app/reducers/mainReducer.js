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
import {ProjectAction} from '../actions/ProjectAction';
import {UIAction} from '../actions/UIAction';

export const mainStateDefaults = {
    projects: [],
    selected: null,
    all: true,
    search: '',
    state: null,
    tab: 0,
    notification: null,
    loading: 0
};
export const mainReducer = (state, action) => {
    state = {...mainStateDefaults, ...state};
    switch (action.type) {
        case ProjectAction.ADD_PROJECT:
            return {
                ...state,
                projects: state.projects.concat([action.project])
            };
        case ProjectAction.SET_PROJECTS:
            return {
                ...state,
                projects: action.projects
            };
        case ProjectAction.SELECT_PROJECT:
            return {
                ...state,
                selected: action.name,
                all: false
            };
        case ProjectAction.SELECT_ALL:
            return {
                ...state,
                selected: null,
                all: true
            };
        case ProjectAction.SET_KEYSET:
            return {
                ...state,
                projects: state.projects.map(p => p.name === action.project ? p.setKeyset(action.keyset, action.isNew) : p)
            };
        case ProjectAction.UPDATE_KEYSETS:
            return {
                ...state,
                projects: state.projects
                    .map(p => action.keysets
                        .reduce((finalProject, keyset) => p.setKeyset(keyset, false), p))
            };
        case ProjectAction.SET_KEY:
            return {
                ...state,
                projects: state.projects.map(p => p.name === action.project ? p.setKey(action.keysetId, action.id, action.key) : p)
            };
        case ProjectAction.SEARCH_KEY:
            return {
                ...state,
                search: action.query
            };
        case ProjectAction.FILTER_STATE:
            return {
                ...state,
                state: action.state
            };
        case UIAction.SET_TAB:
            return {
                ...state,
                tab: action.tab
            };
        case UIAction.SET_NOTIFICATION:
            return {
                ...state,
                notification: action.notification
            };
        case UIAction.SET_LOADING:
            return {
                ...state,
                loading: Math.max(action.loading ? state.loading + 1 : state.loading - 1, 0)
            };
        default:
            return state;
    }
};
