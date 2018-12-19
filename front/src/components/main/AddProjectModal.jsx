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
import { Modal, ModalHeader, ModalFooter, ModalBody, Button, FormGroup, Input } from 'reactstrap';
import { connect } from 'react-redux';
import { closeDialog } from '../../app/actions/DialogAction';
import { importProject } from '../../app/actions/ProjectAction';
import { LoadingButton } from '../LoadingButton';
const mapState = state => ({
    show: state.dialogs.addProject
});
const mapDispatch = dispatch => ({
    hide: () => dispatch(closeDialog()),
    importProject: path => dispatch(importProject(path))
});
class _AddProjectModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            repo: ''
        };
    }
    onChange(event) {
        this.setState({
            repo: event.currentTarget.value
        });
    }
    render() {
        return (<Modal fade={true} isOpen={this.props.show} toggle={() => this.props.hide()}>
                <ModalHeader>Importer un projet</ModalHeader>
                <ModalBody>
                    <FormGroup>
                        <Input type="text" placeholder="Dépôt git" onChange={(e) => this.onChange(e)}/>
                    </FormGroup>
                </ModalBody>
                <ModalFooter>
                    <LoadingButton color="primary" onClick={() => this.props.importProject(this.state.repo)}>Importer</LoadingButton>{' '}
                    <Button color="secondary" onClick={this.props.hide}>Annuler</Button>
                </ModalFooter>
            </Modal>);
    }
}
export const AddProjectModal = connect(mapState, mapDispatch)(_AddProjectModal);
