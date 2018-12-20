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
import * as React from 'react';
import {Container, TabContent, TabPane, Row, Col} from 'reactstrap';
import {ProjectFileExplorer} from './ProjectFileExplorer';
import {KeysetAssistant} from './KeysetAssistant';
import {KeyTable} from './KeyTable';
import {SearchBar} from './SearchBar';
import {ExportTranslations} from './ExportTranslations';
import {ImportTranslations} from './ImportTranslations';

const mapState = (state) => ({
    selection: state.main.selected,
    all: state.main.all,
    tab: state.main.tab
});

class _Selection extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            tab: props.tab
        };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.all !== nextProps.all) {
            this.setTab(0);
        }
    }

    setTab(index) {
        this.setState({
            tab: index
        });
    }

    render() {
        if (this.props.selection === null && !this.props.all) {
            return <Container className="text-center text-muted py-3" fluid><h4>Aucune sélection</h4></Container>;
        }
        else {
            return (<div>
                <TabContent activeTab={this.props.tab}>
                    <TabPane tabId={0}>
                        <Container fluid>
                            <SearchBar/>
                            <KeyTable/>
                        </Container>
                    </TabPane>
                    <TabPane tabId={1}>
                        <Row>
                            <Col md={8}>
                                <ProjectFileExplorer/>
                            </Col>
                            <Col className="pl-4" md={4}>
                                <KeysetAssistant/>
                            </Col>
                        </Row>
                    </TabPane>
                    <TabPane tabId={2}>
                        <Row>
                            <Col md={4}>
                                <ExportTranslations/>
                            </Col>
                            <Col md={8} className="pl-4">
                                <ImportTranslations/>
                            </Col>
                        </Row>
                    </TabPane>
                </TabContent>
            </div>);
        }
    }
}

export const Selection = connect(mapState)(_Selection);
