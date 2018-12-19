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
import { showRecentExports, startImport } from '../../app/actions/ImportExportAction';
import { ListGroup, ListGroupItem, ListGroupItemHeading, ListGroupItemText, Row, Col, Button } from 'reactstrap';
const mapState = state => ({
    recent: state.importsExports.allExports,
    auth: state.auth.auth
});
const mapDispatch = dispatch => ({
    loadRecent: () => dispatch(showRecentExports()),
    showImportDialog: id => dispatch(startImport(id))
});
class _ImportTranslations extends React.Component {
    componentDidMount() {
        if (this.props.auth) {
            this.props.loadRecent();
        }
    }
    componentWillReceiveProps(props) {
        if (!this.props.auth && props.auth) {
            this.props.loadRecent();
        }
    }
    render() {
        return (<ListGroup>
                {this.props.recent.reverse().map(meta => (<ListGroupItem key={meta.exportId}>
                        <Row className="align-items-center">
                            <Col md={10}>
                                <ListGroupItemHeading>
                                    {meta.exportId.toUpperCase()}
                                </ListGroupItemHeading>
                                <ListGroupItemText>
                                    {new Date(meta.timestamp).toLocaleDateString('fr-FR', {
            weekday: 'long', month: 'long', day: 'numeric'
        })}
                                </ListGroupItemText>
                            </Col>
                            <Col md={2}>
                                <Button onClick={() => this.props.showImportDialog(meta.exportId)}>Importer</Button>
                            </Col>
                        </Row>
                    </ListGroupItem>))}
            </ListGroup>);
    }
}
export const ImportTranslations = connect(mapState, mapDispatch)(_ImportTranslations);
