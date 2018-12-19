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
import { ListGroup, ListGroupItem, Badge } from 'reactstrap';
import { selectAll, selectProject } from '../../app/actions/ProjectAction';
import { browseProject, browseProjectFiles, resetSelection } from '../../app/actions/FileAction';
import { showAddProject } from '../../app/actions/DialogAction';
const mapState = state => ({
    projects: state.main.projects,
    selection: state.main.selected,
    all: state.main.all
});
const mapDispatch = dispatch => ({
    select: (name) => {
        Promise.resolve(dispatch(selectProject(name)))
            .then(_ => dispatch(resetSelection()))
            .then(_ => dispatch(browseProjectFiles(name, '/')));
    },
    addProject: () => dispatch(showAddProject()),
    toggleAll: () => Promise.resolve(dispatch(selectAll()))
        .then(_ => dispatch(browseProject(null)))
});
class _ProjectNavigation extends React.Component {
    render() {
        return (<ListGroup>
                {this.props.projects.map((p, i) => (<ListGroupItem action tag="a" href="#" key={p.name} className={i === 0 ? 'border-top-0' : ''} active={p.name === this.props.selection && !this.props.all} onClick={() => this.props.select(p.name)}>
                        {p.name}
                    </ListGroupItem>))}
                <ListGroupItem action tag="a" href="#" onClick={() => this.props.toggleAll()} active={this.props.all}>
                    <Badge pill color={this.props.all ? 'light' : 'primary'} className="pt-0">
                        <h5 className="m-0">...</h5>
                    </Badge>
                    <span className="ml-2">Tous les projets</span>
                </ListGroupItem>
                <ListGroupItem className="" action tag="a" href="#" onClick={() => this.props.addProject()}>
                    <Badge pill color="primary" className="pt-0">
                        <h5 className="m-0">+</h5>
                    </Badge>
                    <span className="ml-2">Importer un projet</span>
                </ListGroupItem>
            </ListGroup>);
    }
}
export const ProjectNavigation = connect(mapState, mapDispatch)(_ProjectNavigation);
