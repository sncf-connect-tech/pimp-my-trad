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
import { ListGroup, ListGroupItem, Badge } from 'reactstrap';
import { connect } from 'react-redux';
import { showAddKey, showAddLang } from '../../app/actions/DialogAction';
const mapDispatch = dispatch => ({
    showAddKey: () => dispatch(showAddKey()),
    showAddLang: () => dispatch(showAddLang())
});
class _Tools extends React.Component {
    render() {
        return (<ListGroup className="my-4">
                <ListGroupItem action onClick={() => this.props.showAddKey()}>
                    <Badge pill color="success" className="pt-0"><h5 className="m-0">+</h5></Badge> Nouvelle clé
                </ListGroupItem>
                <ListGroupItem action onClick={() => this.props.showAddLang()}>
                    <Badge pill color="success" className="pt-0"><h5 className="m-0">+</h5></Badge> Nouvelle langue
                </ListGroupItem>
            </ListGroup>);
    }
}
export const Tools = connect(null, mapDispatch)(_Tools);
