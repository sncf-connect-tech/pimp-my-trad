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
import { NavItem, NavLink, Button, ButtonGroup, Row, Col } from 'reactstrap';
import { Language } from '../../app/models/KeysetLangMapping';
export class FileEntry extends React.Component {
    render() {
        let active = (lang) => this.props.language === Language[lang];
        let disabled = (lang) => this.props.blacklist.indexOf(Language[lang]) > -1 && !active(lang);
        return (<NavItem className="my-1">
                {this.props.selected ?
            <NavLink tag="a" href="#" active>
                        <Row>
                            <Col className="py-1" md={8} onClick={() => this.props.select()}>{this.props.label}</Col>
                            <Col md={4} className="py-0">
                                <ButtonGroup className="float-right">
                                    {Object.keys(Language).map(lang => (<Button color="light" size="sm" key={lang} onClick={() => {
                if (!disabled(lang)) {
                    this.props.setLanguage(Language[lang]);
                }
            }} active={active(lang)}>
                                            {Language[lang]}
                                        </Button>))}
                                    <Button color="light" size="sm" key="more">...</Button>
                                </ButtonGroup>
                            </Col>
                        </Row>
                    </NavLink> :
            <NavLink tag="a" href="#" onClick={() => this.props.select()}>{this.props.label}</NavLink>}
            </NavItem>);
    }
}
