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
import {Col, Container, Row} from "reactstrap";
import {allKeysets} from "../../app/models/Project";
import {connect} from "react-redux";

const mapStateToProps = state => ({
    keysets: allKeysets(state.main.all ?
        state.main.projects : state.main.projects.filter(p => p.name === state.main.selected)),
});

class _Overview extends React.Component {

    countState(state) {
        return this.props.keysets
            .map(keyset => keyset.pairs()
                .filter(([id, key]) => key.state.toLowerCase() === state)
                .length)
            .reduce((s, n) => s + n, 0);
    }

    render() {
        return (
            <Container>
                <Row>
                    <Col xs={4} className="text-center">
                        <h1 className="display-3 text-warning font-weight-bold">{this.countState('todo')}</h1>
                        <p className="text-muted">à faire</p>
                    </Col>
                    <Col xs={4} className="text-center">
                        <h1 className="display-3 text-info font-weight-bold">{this.countState('inprogress')}</h1>
                        <p className="text-muted">en cours</p>
                    </Col>
                    <Col xs={4} className="text-center">
                        <h1 className="display-3 text-success font-weight-bold">{this.countState('done')}</h1>
                        <p className="text-muted">terminés</p>
                    </Col>
                </Row>
            </Container>

        )
    }
}

export const Overview = connect(mapStateToProps)(_Overview);