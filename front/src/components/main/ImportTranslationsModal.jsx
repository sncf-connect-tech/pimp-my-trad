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
import { Fragment, Component } from 'react';
import { Modal, ModalHeader, ModalFooter, ModalBody, Button, FormGroup, Input, Label } from 'reactstrap';
import { connect } from 'react-redux';
import { closeDialog } from '../../app/actions/DialogAction';
import { getPrettyLanguage, Language } from '../../app/models/KeysetLangMapping';
import { completeImport } from '../../app/actions/ImportExportAction';
import { FileInput } from './FileInput';
import { notify } from '../../app/actions/UIAction';
const mapState = state => ({
    show: state.dialogs.importTranslations
});
const mapDispatch = dispatch => ({
    hide: () => dispatch(closeDialog()),
    importTranslations: (language, file) => dispatch(completeImport(language, file))
        .then(_ => dispatch(notify('Import effectué avec succès!')))
});
class _ImportTranslationsModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            files: []
        };
    }
    componentDidMount() {
        this.addNew();
    }
    render() {
        return (<Modal fade={true} isOpen={this.props.show} toggle={() => this.props.hide()}>
                <ModalHeader>Importer des traductions</ModalHeader>
                <ModalBody>
                    {this.state.files.map((file, n) => this.renderEntry(file, n))}
                    <div className="text-center my-3">
                        <Button color="link" onClick={() => this.addNew()}>
                            Ajouter un fichier supplémentaire
                        </Button>
                    </div>
                </ModalBody>
                <ModalFooter>
                    <Button color="primary" onClick={() => this.importAll()}>Importer</Button>{' '}
                    <Button color="secondary" onClick={this.props.hide}>Annuler</Button>
                </ModalFooter>
            </Modal>);
    }
    importAll() {
        this.state.files.reduce((promise, file) => {
            return file.uploadedFile === null ?
                Promise.resolve() :
                promise.then(_ => this.props.importTranslations(file.language, file.uploadedFile));
        }, Promise.resolve());
    }
    renderEntry(fileState, n) {
        return (<Fragment key={fileState.key}>
                {n > 0 ?
            <Button className="float-right py-0 px-1" size="sm" onClick={() => this.removeFile(n)} color="danger">&times;</Button> :
            ''}
                <FormGroup className="mt-1">
                    <Label>Langue cible {n + 1}</Label>
                    <Input type="select" onChange={e => this.setLanguage(e, n)} value={fileState.language}>
                        {this.validLanguages(fileState.language).map(lang => (<option key={lang} value={lang}>
                                {getPrettyLanguage(lang)}
                            </option>))}
                    </Input>
                </FormGroup>
                <FormGroup>
                    <Label>Fichier de traductions {n + 1}</Label>
                    <FileInput onFileChanged={file => this.setFile(file, n)}/>
                </FormGroup>
                <hr />
            </Fragment>);
    }
    validLanguages(current) {
        let alreadyUsed = this.state.files.map(f => f.language);
        return Object.keys(Language)
            .map(key => Language[key])
            .filter(lang => current === lang || alreadyUsed.indexOf(lang) === -1);
    }
    addNew() {
        let newFile = {
            language: Language.French,
            uploadedFile: null,
            key: Math.random().toString(36).substr(2, 5)
        };
        this.setState({
            files: this.state.files.concat(newFile)
        });
    }
    removeFile(n) {
        this.setState({
            files: this.state.files.filter((_, i) => i !== n)
        });
    }
    setFile(file, n) {
        this.setState({
            files: this.state.files.map((fs, i) => i === n ? { ...fs, uploadedFile: file } : fs)
        });
    }
    setLanguage(e, n) {
        this.setState({
            files: this.state.files.map((fs, i) => i === n ?
                { ...fs, language: e.currentTarget.value } : fs)
        });
    }
}
export const ImportTranslationsModal = connect(mapState, mapDispatch)(_ImportTranslationsModal);
