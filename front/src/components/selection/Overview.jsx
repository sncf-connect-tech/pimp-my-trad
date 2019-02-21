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
import {Button, Col, Container, Row} from "reactstrap";
import {allKeysets} from "../../app/models/Project";
import {connect} from "react-redux";
import {filterState} from "../../app/actions/ProjectAction";
import {getColorForState, KeyState} from "../../app/models/Keyset";

const mapStateToProps = state => ({
    keysets: allKeysets(state.main.all ?
        state.main.projects : state.main.projects.filter(p => p.name === state.main.selected)),
    state: state.main.state
});

const mapDispatchToProps = (dispatch) => ({
    filterState: state => dispatch(filterState(state)),
});

class _Overview extends React.Component {

    countState(state) {
        return this.props.keysets
            .map(keyset => keyset.pairs()
                .filter(([id, key]) => key.state.toLowerCase() === state)
                .length)
            .reduce((s, n) => s + n, 0);
    }

    getMapping() {
        return Object.keys(KeyState).map(key => ({
            key,
            color: getColorForState(key),
            pretty: KeyState[key],
            count: this.countState(key)
        }));
    }

    render() {
        const {state, filterState} = this.props;
        const mapping = this.getMapping();
        const col = 12 / mapping.filter(m => m.count > 0).length;
        return (
            <Container>
                <Row className="pb-3">
                    {mapping.map(({key: targetState, color, pretty, count}) => count > 0 && (
                        <Col key={targetState} xs={col} className="text-center">
                            <h1 className={`display-3 text-${color} font-weight-bold`}>{count}</h1>
                            <Button color={color} size="sm"
                                    onClick={() => state !== targetState ?
                                            filterState(targetState) :
                                            filterState(null)}
                                    outline={state !== targetState}>{pretty}</Button>
                        </Col>
                    ))}
                </Row>
            </Container>

        )
    }
}

export const Overview = connect(mapStateToProps, mapDispatchToProps)(_Overview);