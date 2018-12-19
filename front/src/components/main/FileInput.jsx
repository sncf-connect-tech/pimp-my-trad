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
import * as React from 'react';
import { Component } from 'react';
import { Input, Label } from 'reactstrap';
export class FileInput extends Component {
    constructor(props) {
        super(props);
        this.state = {
            file: null
        };
    }
    render() {
        return (<div className="custom-file">
                <Input className="custom-file-input" type="file" onChange={e => this.handleChange(e)}/>
                <Label className="custom-file-label">
                    {this.fileName()}
                </Label>
            </div>);
    }
    handleChange(e) {
        let file = e.currentTarget.files[0];
        this.setState({ file });
        this.props.onFileChanged(file);
    }
    fileName() {
        if (this.state.file === null) {
            return '(aucun fichier)';
        }
        let name = this.state.file.name;
        name = name.split(/[\/\\]/).pop() || '';
        name = name.length > 25 ? name.substr(0, 25) + '...' : name;
        return name;
    }
}