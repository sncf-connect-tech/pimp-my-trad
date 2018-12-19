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
import { Col, Progress, Row } from 'reactstrap';
import { Header } from './main/Header';
import { ProjectNavigation } from './main/ProjectNavigation';
import { Tools } from './selection/Tools';
import { Selection } from './selection/Selection';
import { AddProjectModal } from './main/AddProjectModal';
import { AddKeyModal } from './main/AddKeyModal';
import { AddLangModal } from './main/AddLangModal';
import { SyncModal } from './main/SyncModal';
import { ImportTranslationsModal } from './main/ImportTranslationsModal';
import { connect } from 'react-redux';
import { getProjects } from '../app/actions/ProjectAction';
import { Login } from './Login';
import { checkAuth } from '../app/actions/AuthAction';
const mapStateToProps = state => ({
    auth: state.auth.auth,
    loading: state.main.loading > 0
});
const mapDispatchToProps = dispatch => ({
    getProjects: () => dispatch(getProjects()),
    login: (user, password) => dispatch(checkAuth(user, password))
});
class _App extends React.Component {
    componentDidMount() {
        if (this.props.auth) {
            this.props.getProjects();
        }
    }
    componentWillReceiveProps(props) {
        if (!this.props.auth && props.auth) {
            this.props.getProjects();
        }
    }
    render() {
        return (<div>
                <Header />
                <Progress className="loading" value={this.props.loading ? 0 : 100}/>
                <Row className="flex-grow">
                    <Col md={2} className="sidebar">
                        <div className="sticky-top">
                            <ProjectNavigation />
                            <Tools />
                        </div>
                    </Col>
                    <Col md={10} className="p-4 scroll-y">
                        <Selection tab={0}/>
                    </Col>
                </Row>
                <Login auth={this.props.auth} login={this.props.login}/>
                <AddProjectModal />
                <AddKeyModal />
                <AddLangModal />
                <SyncModal />
                <ImportTranslationsModal />
            </div>);
    }
}
const App = connect(mapStateToProps, mapDispatchToProps)(_App);
export default App;
