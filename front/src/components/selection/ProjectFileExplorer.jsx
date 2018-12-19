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
import { Container, Breadcrumb, BreadcrumbItem, Nav, NavItem, NavLink } from 'reactstrap';
import { browseFiles, browseProject, selectFile, toggleFileSelect } from '../../app/actions/FileAction';
import { FileEntry } from './FileEntry';
const mapState = state => ({
    files: state.selection.files,
    selected: state.selection.selected,
    workingDir: state.selection.workingDir,
    projectName: state.main.selected
});
const mapDispatch = dispatch => ({
    browse: path => dispatch(browseFiles(path)),
    browseProject: projectName => dispatch(browseProject(projectName)),
    toggle: path => dispatch(toggleFileSelect(path)),
    setLang: (path, lang) => dispatch(selectFile(path, lang))
});
class _ProjectFileExplorer extends React.Component {
    isSelected(path) {
        return this.props.selected.isPresent(path);
    }
    getLanguage(path) {
        return this.props.selected.getLanguage(path);
    }
    renderFile(path) {
        let full = this.props.workingDir + path;
        let blacklist = this.props.selected.languages.filter(v => v != null);
        return (<FileEntry selected={this.isSelected(full)} language={this.getLanguage(full)} blacklist={blacklist} label={path} key={path} select={() => this.props.toggle(full)} setLanguage={(lang) => this.props.setLang(full, lang)}/>);
    }
    renderDir(path) {
        let full = this.props.workingDir + path;
        return (<NavItem key={path} className="my-1">
                <NavLink href="#" title={full} onClick={() => this.props.browse(full)}>
                    {path}
                </NavLink>
            </NavItem>);
    }
    renderProjectPath() {
        let list = this.props.workingDir.substring(1).split('/');
        list.pop();
        return (<React.Fragment>
                <BreadcrumbItem tag="a" href="#" onClick={() => this.props.browse('/')}>{this.props.projectName}</BreadcrumbItem>
                {list.map((fragment, index) => {
            let path = '/' + list.slice(0, index + 1).join('/') + '/';
            return (<BreadcrumbItem tag="a" href="#" title={path} key={path} onClick={() => this.props.browse(path)}>
                            {fragment}
                        </BreadcrumbItem>);
        })}
            </React.Fragment>);
    }
    renderProject(projectName) {
        return (<NavItem key={projectName} className="my-1">
                <NavLink href="#" title={projectName} onClick={() => this.props.browseProject(projectName)}>
                    {projectName}
                </NavLink>
            </NavItem>);
    }
    render() {
        let isProject = this.props.projectName === null;
        return (<Container fluid>
                <Breadcrumb tag="nav">
                    <BreadcrumbItem tag="a" href="#" onClick={() => this.props.browseProject(null)}>Projets</BreadcrumbItem>
                    {this.props.projectName !== null ? this.renderProjectPath() : ''}
                </Breadcrumb>
                <Nav vertical pills>
                    {this.props.files.map(file => {
            let isDir = file.endsWith('/');
            return isDir ?
                this.renderDir(file) :
                isProject ? this.renderProject(file) : this.renderFile(file);
        })}
                </Nav>
            </Container>);
    }
}
export const ProjectFileExplorer = connect(mapState, mapDispatch)(_ProjectFileExplorer);
