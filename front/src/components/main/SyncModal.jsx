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
import {connect} from 'react-redux';
import {syncAll} from '../../app/actions/ProjectAction';
import {closeDialog} from '../../app/actions/DialogAction';
import {Modal, ModalHeader, ModalFooter, ModalBody, Button} from 'reactstrap';
import * as React from 'react';
import {allKeysets} from '../../app/models/Project';

const mapState = state => ({
    show: state.dialogs.sync,
    conflict: allKeysets(state.main.projects)
        .map(k => k.pairs())
        .reduce((acc, cur) => acc.concat(cur), [])
        .filter(([id, key]) => key.state.toLowerCase() === 'conflict')
        .map(([id, key]) => id)
});
const mapDispatch = dispatch => ({
    syncAll: () => dispatch(syncAll()),
    hide: () => dispatch(closeDialog())
});

class _SyncModal extends React.Component {
    render() {
        return (<Modal fade={true} isOpen={this.props.show} toggle={() => this.props.hide()}>
            <ModalHeader>Synchroniser les changements</ModalHeader>
            <ModalBody>
                {this.props.conflict.length <= 0 ?
                    <div>Les modifications seront poussées.</div> :
                    <div>
                        Les clés suivantes sont en conflit avec une autre contribution :
                        <ul>{this.props.conflict.map(id => <li key={id}>{id}</li>)}</ul>
                        Annulez pour corriger les clés.<br/>
                        <b>Si vous synchronisez à nouveau, votre version sera utilisée.</b>
                    </div>}
            </ModalBody>
            <ModalFooter>
                <Button color="primary" onClick={() => this.props.syncAll()}>Synchroniser</Button>{' '}
                <Button color="secondary" onClick={this.props.hide}>Annuler</Button>
            </ModalFooter>
        </Modal>);
    }
}

export const SyncModal = connect(mapState, mapDispatch)(_SyncModal);
