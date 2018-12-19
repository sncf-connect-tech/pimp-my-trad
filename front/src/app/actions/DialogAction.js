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
export var DialogAction;
(function (DialogAction) {
    DialogAction["SHOW_ADD_PROJECT"] = "SHOW_ADD_PROJECT";
    DialogAction["SHOW_ADD_KEY"] = "SHOW_ADD_KEY";
    DialogAction["CLOSE_ANY"] = "CLOSE_ANY";
    DialogAction["SHOW_ADD_LANG"] = "SHOW_ADD_LANG";
    DialogAction["SHOW_SYNC_DIALOG"] = "SHOW_SYNC_DIALOG";
    DialogAction["SHOW_IMPORT_DIALOG"] = "SHOW_IMPORT_DIALOG";
})(DialogAction || (DialogAction = {}));
export function showAddProject() {
    return {
        type: DialogAction.SHOW_ADD_PROJECT
    };
}
export function showAddKey() {
    return {
        type: DialogAction.SHOW_ADD_KEY
    };
}
export function showAddLang() {
    return {
        type: DialogAction.SHOW_ADD_LANG
    };
}
export function showSyncDialog() {
    return {
        type: DialogAction.SHOW_SYNC_DIALOG
    };
}
export function showImportDialog() {
    return {
        type: DialogAction.SHOW_IMPORT_DIALOG
    };
}
export function closeDialog() {
    return {
        type: DialogAction.CLOSE_ANY
    };
}
