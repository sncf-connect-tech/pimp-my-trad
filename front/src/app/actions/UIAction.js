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
import {timeout} from '../utils';

export var UIAction =  {
    SET_TAB: "SET_TAB",
    SET_NOTIFICATION: "SET_NOTIFICATION",
    SET_LOADING: "SET_LOADING",
};

export function setTab(tab) {
    return {
        type: UIAction.SET_TAB,
        tab
    };
}

export function setLoading(loading) {
    return dispatch => {
        dispatch({
            type: UIAction.SET_LOADING,
            loading
        });
        if (loading) window.setTimeout(() => dispatch({
            type: UIAction.SET_LOADING,
            loading: false
        }), 2000);
    }
}

export function notify(message) {
    return dispatch => Promise.resolve(dispatch({
        type: UIAction.SET_NOTIFICATION,
        notification: message
    }))
        .then(_ => timeout(3000))
        .then(_ => dispatch({
            type: UIAction.SET_NOTIFICATION,
            notification: null
        }));
}

export function loadingDone(dispatch) {
    return (res) => {
        dispatch({
            type: UIAction.SET_LOADING,
            loading: false
        });
        return res;
    };
}
