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
export class KeysetLangMapping {
    constructor(files, languages) {
        this.files = [];
        this.languages = [];
        this.files = files;
        this.languages = languages;
    }
    static empty() {
        return new KeysetLangMapping([], []);
    }
    addFile(path, lang) {
        let index = this.fileIndex(path);
        if (index > -1) {
            return new KeysetLangMapping(this.files.slice(), this.languages.map((l, i) => i === index ? lang : l));
        }
        else {
            return new KeysetLangMapping(this.files.concat(path), this.languages.concat(lang));
        }
    }
    removeFile(path) {
        let index = this.fileIndex(path);
        if (index > -1) {
            return new KeysetLangMapping(this.files.filter((f, i) => i !== index), this.languages.filter((f, i) => i !== index));
        }
        return this;
    }
    toggleFile(path) {
        let index = this.fileIndex(path);
        return index > -1 ? this.removeFile(path) : this.addFile(path, null);
    }
    fileCount() {
        return this.files.length;
    }
    isPresent(path) {
        return this.fileIndex(path) > -1;
    }
    getLanguage(path) {
        let index = this.files.indexOf(path);
        return index > -1 ? this.languages[index] : null;
    }
    toObject() {
        return this.languages.reduce((acc, lang, index) => {
            if (lang != null) {
                acc[lang] = this.files[index];
            }
            return acc;
        }, {});
    }
    fileIndex(path) {
        return this.files.indexOf(path);
    }
}
export var Language;
(function (Language) {
    Language["French"] = "FR";
    Language["English"] = "EN";
    Language["Spanish"] = "ES";
    Language["German"] = "DE";
    Language["Italian"] = "IT";
})(Language || (Language = {}));
export function getPrettyLanguage(lang) {
    switch (lang) {
        case Language.French:
            return 'Français';
        case Language.English:
            return 'Anglais';
        case Language.Spanish:
            return 'Espagnol';
        case Language.German:
            return 'Allemand';
        case Language.Italian:
            return 'Italien';
        default:
            return '';
    }
}
