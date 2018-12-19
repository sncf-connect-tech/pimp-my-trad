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
import { connect } from 'react-redux';
import { getPrettyLanguage, KeysetLangMapping, Language } from '../../app/models/KeysetLangMapping';
import { Modal, ModalHeader, ModalFooter, ModalBody, Button, FormGroup, Input, Label } from 'reactstrap';
import { allKeysetsWithNames } from '../../app/models/Project';
import { closeDialog } from '../../app/actions/DialogAction';
import { addFiles } from '../../app/actions/ProjectAction';
import { safeGet, safeGetter } from '../../app/utils';
import { LoadingButton } from '../LoadingButton';
const mapState = state => ({
    show: state.dialogs.addLang,
    keysets: allKeysetsWithNames(state.main.projects),
    selectedProject: state.main.selected
});
const mapDispatch = dispatch => ({
    hide: () => dispatch(closeDialog()),
    addLang: (projectName, keysetId, newfile, language) => {
        let mapping = new KeysetLangMapping([newfile], [language]);
        dispatch(addFiles(projectName, keysetId, mapping))
            .then(_ => dispatch(closeDialog()));
    }
});
class _AddLangModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            projectName: '',
            keysetId: '',
            language: Language.French,
            file: ''
        };
    }
    cascadeState(key, value, props = null) {
        if (props === null) {
            props = this.props;
        }
        let orderedKeys = ['projectName', 'keysetId', 'language', 'file'];
        let scanFuns = [
            (state) => {
                return [props.keysets]
                    .map(safeGetter(state.projectName, []))
                    .map(x => x.slice().shift()) // safely access first element
                    .map(safeGetter('id', ''))
                    .pop();
            },
            (state) => this.validLanguages(state.keysetId).shift(),
            (state) => this.filenameSuggestion(state.keysetId, state.language)
        ];
        let newState = { ...this.state, [key]: value };
        let index = orderedKeys.indexOf(key);
        let finalState = orderedKeys
            .filter((k, i) => i >= index && i < orderedKeys.length - 1)
            .reduce((state, k, i) => ({
            ...state,
            [orderedKeys[i + index + 1]]: scanFuns[i + index].call(null, state)
        }), newState);
        this.setState(finalState);
    }
    componentWillReceiveProps(nextProps) {
        let projectName = nextProps.selectedProject || Object.keys(nextProps.keysets).shift() || '';
        this.cascadeState('projectName', projectName, nextProps);
    }
    projectNames() {
        return Object.keys(this.props.keysets);
    }
    addLang() {
        this.props.addLang(this.state.projectName, this.state.keysetId, this.state.file, this.state.language);
    }
    validKeysets() {
        return safeGet(this.props.keysets, this.state.projectName, []);
    }
    findKeyset(keysetId) {
        return this.validKeysets()
            .filter((k) => k.id === keysetId)
            .pop() || null;
    }
    validLanguages(keysetId) {
        let supported;
        try {
            supported = safeGet(this.findKeyset(keysetId), 'supportedLanguages', []);
        }
        catch (e) {
            supported = [];
        }
        return Object.keys(Language)
            .map(key => Language[key])
            .filter(lang => supported.indexOf(lang) === -1);
    }
    filenameSuggestion(keysetId, lang) {
        let k = safeGet(this.findKeyset(keysetId), 'name', '');
        return k.replace('*', lang.toLowerCase());
    }
    updateProjectName(e) {
        this.cascadeState('projectName', e.currentTarget.value);
    }
    updateKeysetId(e) {
        this.cascadeState('keysetId', e.currentTarget.value);
    }
    updateLanguage(e) {
        this.cascadeState('language', e.currentTarget.value);
    }
    updateFile(e) {
        this.cascadeState('file', e.currentTarget.value);
    }
    render() {
        return (<Modal fade={true} isOpen={this.props.show} toggle={() => this.props.hide()}>
                <ModalHeader>Ajouter une langue</ModalHeader>
                <ModalBody>
                    <FormGroup>
                        <Label>Projet cible</Label>
                        <Input type="select" value={this.state.projectName} onChange={e => this.updateProjectName(e)}>
                            {this.projectNames()
            .map(name => <option key={name} value={name}>{name}</option>)}
                        </Input>
                    </FormGroup>
                    <FormGroup>
                        <Label>Fichiers cibles</Label>
                        <Input type="select" value={this.state.keysetId} onChange={e => this.updateKeysetId(e)}>
                            {this.validKeysets()
            .map(keyset => <option key={keyset.id} value={keyset.id}>{keyset.name}</option>)}
                        </Input>
                    </FormGroup>
                    <FormGroup>
                        <Label>Nouvelle langue</Label>
                        <Input type="select" value={this.state.language} onChange={e => this.updateLanguage(e)}>
                            {this.validLanguages(this.state.keysetId)
            .map(lang => <option key={lang} value={lang}>{getPrettyLanguage(lang)}</option>)}
                        </Input>
                    </FormGroup>
                    <FormGroup>
                        <Label>Nouveau fichier</Label>
                        <Input type="text" placeholder="" value={this.state.file} onChange={e => this.updateFile(e)}/>
                    </FormGroup>
                </ModalBody>
                <ModalFooter>
                    <LoadingButton color="primary" onClick={() => this.addLang()}>Ajouter</LoadingButton>{' '}
                    <Button color="secondary" onClick={this.props.hide}>Annuler</Button>
                </ModalFooter>
            </Modal>);
    }
}
export const AddLangModal = connect(mapState, mapDispatch)(_AddLangModal);
