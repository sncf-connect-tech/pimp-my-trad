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
import {connect} from 'react-redux';
import {Modal, ModalHeader, ModalFooter, ModalBody, Button, FormGroup, Input, Label, FormFeedback} from 'reactstrap';
import {allKeysetsWithNames} from '../../app/models/Project';
import {closeDialog} from '../../app/actions/DialogAction';
import {setKey} from '../../app/actions/ProjectAction';
import {Language} from '../../app/models/KeysetLangMapping';
import {flattener, objectValues, safeGetter} from '../../app/utils';
import {LoadingButton} from '../LoadingButton';

const mapState = state => ({
    show: state.dialogs.addKey,
    keysets: allKeysetsWithNames(state.main.projects),
    selectedProject: state.main.selected
});
const mapDispatch = dispatch => ({
    addKey: (projectName, keysetId, keyId, text) => dispatch(setKey(keyId, keysetId, projectName, Language.French, text))
        .then(success => success && dispatch(closeDialog())),
    hide: () => dispatch(closeDialog())
});

class _AddKeyModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            keyId: '',
            keysetId: '',
            defaultText: '',
            projectName: this.projectNames()[0],
            valid: true
        };
    }

    componentWillReceiveProps(nextProps) {
        let keysets = [this.props.keysets]
            .map(x => objectValues(x))
            .map(flattener());
        this.keyIds = keysets
            .map(ks => ks.map(k => k.keyIds()))
            .map(flattener())
            .pop();
        this.setState({
            projectName: nextProps.selectedProject || Object.keys(nextProps.keysets).shift() || '',
            keysetId: keysets
                .map(ks => ks.shift() || null)
                .map(safeGetter('id', ''))
                .pop()
        });
    }

    updateStateValue(target) {
        return e => {
            let partial = {valid: true};
            partial[target] = e.currentTarget.value;
            partial.valid = target !== 'keyId' || this.checkId(partial[target]);
            this.setState(partial);
        };
    }

    addKey() {
        this.props.addKey(this.state.projectName, this.state.keysetId, this.state.keyId, this.state.defaultText);
    }

    keysets() {
        let projectName = Object.keys(this.props.keysets)
            .filter(aName => aName === this.state.projectName)
            .shift() || null;
        return projectName == null ? [] : this.props.keysets[projectName];
    }

    checkId(keyId) {
        return !this.keyIds
            .some(id => id === keyId);
    }

    projectNames() {
        return Object.keys(this.props.keysets);
    }

    render() {
        return (<Modal fade={true} isOpen={this.props.show} toggle={() => this.props.hide()}>
            <ModalHeader>Ajouter une clé</ModalHeader>
            <ModalBody>
                <FormGroup>
                    <Label>Identifiant technique</Label>
                    <Input type="text" placeholder="ex: myapp.new.key" value={this.state.keyId} valid={this.state.valid}
                           invalid={!this.state.valid} onChange={this.updateStateValue('keyId')}/>
                    <FormFeedback>Cette clé existe déjà!</FormFeedback>
                </FormGroup>
                <FormGroup>
                    <Label>Texte français</Label>
                    <Input type="text" placeholder="ex: 'Nouvelle clé'" value={this.state.defaultText}
                           onChange={this.updateStateValue('defaultText')}/>
                </FormGroup>
                <FormGroup>
                    <Label>Projet cible</Label>
                    <Input type="select" value={this.state.projectName} onChange={this.updateStateValue('projectName')}>
                        {this.projectNames()
                            .map(name => <option key={name} value={name}>{name}</option>)}
                    </Input>
                </FormGroup>
                <FormGroup>
                    <Label>Fichiers cibles</Label>
                    <Input type="select" value={this.state.keysetId} onChange={this.updateStateValue('keysetId')}>
                        {this.keysets()
                            .map(keyset => <option key={keyset.id} value={keyset.id}>{keyset.name}</option>)}
                    </Input>
                </FormGroup>
            </ModalBody>
            <ModalFooter>
                <LoadingButton color="primary" onClick={() => this.addKey()}
                               disabled={!this.state.valid}>Ajouter</LoadingButton>{' '}
                <Button color="secondary" onClick={this.props.hide}>Annuler</Button>
            </ModalFooter>
        </Modal>);
    }
}

export const AddKeyModal = connect(mapState, mapDispatch)(_AddKeyModal);
