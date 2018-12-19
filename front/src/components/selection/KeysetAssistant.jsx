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
import { connect } from 'react-redux';
import * as React from 'react';
import { Card, CardBody, CardTitle, Badge, CardText, UncontrolledDropdown } from 'reactstrap';
import { addSelectedFiles, createKeyset } from '../../app/actions/ProjectAction';
import { resetSelection } from '../../app/actions/FileAction';
import DropdownToggle from 'reactstrap/lib/DropdownToggle';
import DropdownMenu from 'reactstrap/lib/DropdownMenu';
import DropdownItem from 'reactstrap/lib/DropdownItem';
import { notify } from '../../app/actions/UIAction';
const mapState = state => ({
    selected: state.selection.selected,
    keysets: state.main.projects
        .filter(p => p.name === state.main.selected)
        .map(p => p.keysets)
        .shift() || []
});
const mapDispatch = dispatch => ({
    createKeyset: () => {
        dispatch(createKeyset()).then(_ => dispatch(resetSelection()));
    },
    addFiles: keysetId => dispatch(addSelectedFiles(keysetId, false))
        .then(_ => dispatch(resetSelection()))
        .then(_ => dispatch(notify('Clés ajoutées avec succès!')))
});
class _KeysetAssistant extends React.Component {
    selectCount() {
        return this.props.selected.fileCount();
    }
    render() {
        return (<Card>
                <CardBody>
                    <CardTitle>Sélection <Badge pill>{this.selectCount()}</Badge></CardTitle>
                    <CardText>Sélectionner des fichiers à grouper.</CardText>
                    <UncontrolledDropdown className="d-inline-block">
                        <DropdownToggle disabled={this.selectCount() === 0} caret>Ajouter à...</DropdownToggle>
                        <DropdownMenu>
                            <DropdownItem onClick={() => this.props.createKeyset()}>
                                Nouveau jeu de clés
                            </DropdownItem>
                            {this.props.keysets.length > 0 ? <DropdownItem divider/> : ''}
                            {this.props.keysets.map((k, i) => <DropdownItem key={i} onClick={() => this.props.addFiles(k.id)}>
                                    {k.name}
                                </DropdownItem>)}
                        </DropdownMenu>
                    </UncontrolledDropdown>
                </CardBody>
            </Card>);
    }
}
export const KeysetAssistant = connect(mapState, mapDispatch)(_KeysetAssistant);
