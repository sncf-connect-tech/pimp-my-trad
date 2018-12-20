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
export var Error;
(function (Error) {
    Error["UNAUTHORIZED"] = "UNAUTHORIZED";
    Error["UNEXPECTED_BODY"] = "UNEXPECTED_BODY";
})(Error || (Error = {}));

export function rejectHttpErrors(res) {
    if (res.status < 400) {
        return Promise.resolve(res);
    }
    if (res.status === 401) {
        localStorage.removeItem('pmt');
        return Promise.reject(Error.UNAUTHORIZED);
    }
    else {
        return res.json()
            .then(err => Promise.reject(err))
            .catch(err => Promise.reject({error: Error.UNEXPECTED_BODY}));
    }
}

export function authHeaders() {
    return Promise.resolve({'Authorization': `Basic ${localStorage.getItem('pmt') || ''}`});
}

export const and = (p) => (result) => p.then(second => ({...result, ...second}));

export function jsonHeaders() {
    return Promise.resolve({'Content-Type': 'application/json'});
}
