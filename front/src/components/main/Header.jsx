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
import { Navbar, NavbarBrand, NavItem, Fade, Nav, NavLink } from 'reactstrap';
import { connect } from 'react-redux';
import { showSyncDialog } from '../../app/actions/DialogAction';
import { setTab } from '../../app/actions/UIAction';
const mapState = state => ({
    tab: state.main.tab,
    notification: state.main.notification
});
const mapDispatch = dispatch => ({
    showSync: () => dispatch(showSyncDialog()),
    selectTab: (tab) => dispatch(setTab(tab))
});
const SetTabItem = (props) => {
    return (<NavItem className="mx-1">
            <NavLink className="px-3" onClick={() => props.select(props.tab)} href="#" active={props.current === props.tab}>{props.children}</NavLink>
        </NavItem>);
};
class _Header extends React.Component {
    render() {
        return (<Navbar color="dark" dark expand="xs" className="p-3 flex-no-shrink">
                {this.props.notification === null ?
            <Fade key={0}><NavbarBrand href="#" className="lead">pimp my trad.</NavbarBrand></Fade> :
            <Fade key={1}>
                            <p className="text-success my-2">{`\u2713 ${this.props.notification}`}</p>
                        </Fade>}
                <Nav className="ml-auto" navbar pills>
                    <SetTabItem select={this.props.selectTab} current={this.props.tab} tab={0}>Parcourir les clés</SetTabItem>
                    <SetTabItem select={this.props.selectTab} current={this.props.tab} tab={1}>Fichiers</SetTabItem>
                    <SetTabItem select={this.props.selectTab} current={this.props.tab} tab={2}>Export et import</SetTabItem>
                    <NavItem className="mx-1">
                        <NavLink className="px-3" href="#" onClick={() => this.props.showSync()}>Synchroniser</NavLink>
                    </NavItem>
                </Nav>
            </Navbar>);
    }
}
export const Header = connect(mapState, mapDispatch)(_Header);
