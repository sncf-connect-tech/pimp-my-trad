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
import { timeout } from '../utils';
export var UIAction;
(function (UIAction) {
    UIAction["SET_TAB"] = "SET_TAB";
    UIAction["SET_NOTIFICATION"] = "SET_NOTIFICATION";
    UIAction["SET_LOADING"] = "SET_LOADING";
})(UIAction || (UIAction = {}));
export function setTab(tab) {
    return {
        type: UIAction.SET_TAB,
        tab
    };
}
export function setLoading(loading) {
    return {
        type: UIAction.SET_LOADING,
        loading
    };
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
        dispatch(setLoading(false));
        return res;
    };
}
