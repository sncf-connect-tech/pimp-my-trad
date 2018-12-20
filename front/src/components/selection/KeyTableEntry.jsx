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
import {
    UncontrolledDropdown,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    ListGroupItem,
    Row,
    Col,
    Input,
    Button
} from 'reactstrap';
import {getPrettyLanguage, Language} from '../../app/models/KeysetLangMapping';
import {connect} from 'react-redux';
import {filterState, setKey, translateNow} from '../../app/actions/ProjectAction';

const mapDispatchToProps = (dispatch) => ({
    modifyKey: (id, keysetId, projectName, lang, translation) => dispatch(setKey(id, keysetId, projectName, lang, translation)),
    filterState: state => dispatch(filterState(state)),
    translateNow: (projectName, keysetId, id, lang) => dispatch(translateNow(projectName, keysetId, id, lang))
});

class _KeyTableEntry extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            focused: false,
            language: Language.French,
            text: props.translationKey.translation(Language.French),
            conflict: props.translationKey.conflicting(Language.French)
        };
    }

    focus() {
        this.setState({
            focused: true
        });
    }

    blur() {
        this.setState({
            focused: false
        });
    }

    componentWillReceiveProps(nextProps) {
        this.switchLang(this.state.language, nextProps);
    }

    switchLang(lang, props = null) {
        if (props === null) {
            props = this.props;
        }
        this.setState({
            language: lang,
            text: props.translationKey.translation(lang) || '',
            conflict: props.translationKey.conflicting(lang)
        });
    }

    modifyKey() {
        this.props.modifyKey(this.props.id, this.props.keysetId, this.props.projectName, this.state.language, this.state.text);
    }

    translateNow() {
        this.props.translateNow(this.props.projectName, this.props.keysetId, this.props.id, this.state.language)
            .then(translated => this.setState({text: translated}));
    }

    updateText(e) {
        this.setState({
            text: e.currentTarget.value
        });
    }

    renderFocused() {
        return (<div>
            {this.state.conflict === null ? '' :
                <div className="alert alert-danger mb-1">
                    Une autre personne a suggéré le texte suivant :
                    <p className="font-italic">{this.state.conflict}</p>
                </div>}
            <Input type="textarea" onChange={e => this.updateText(e)} onBlur={e => this.modifyKey()}
                   value={this.state.text}/>
            <div className="mt-1">
                <UncontrolledDropdown className="d-inline-block">
                    <DropdownToggle caret>{getPrettyLanguage(this.state.language)}</DropdownToggle>
                    <DropdownMenu>
                        {this.props.languages.map(lang => <DropdownItem key={lang}
                                                                        disabled={lang === this.state.language}
                                                                        onClick={() => this.switchLang(lang)}>{getPrettyLanguage(lang)}</DropdownItem>)}
                    </DropdownMenu>
                </UncontrolledDropdown>
                {this.state.language !== Language.French ?
                    <Button className="mx-1" onClick={() => this.translateNow()}>Traduire</Button> : null}
                <Button color="primary" className="float-right" onClick={() => this.blur()}>Terminer</Button>
            </div>
        </div>);
    }

    getColor() {
        switch (this.props.translationKey.state.toLowerCase()) {
            case 'done':
                return 'success';
            case 'inprogress':
                return 'info';
            case 'todo':
                return 'warning';
            case 'conflict':
                return 'danger';
            default:
                return 'secondary';
        }
    }

    filterState(event) {
        this.props.filterState(this.props.translationKey.state);
        event.stopPropagation();
    }

    defaultText() {
        let len = 80;
        let str = this.props.translationKey.translation(Language.French) || '';
        return str.substr(0, len) + (str.length > len ? '...' : '');
    }

    render() {
        return (<ListGroupItem tag="li" action={!this.state.focused}
                               onClick={() => this.state.focused ? null : this.focus()}>
            <Row>
                <Col xs={3}><span className="text-muted">{this.props.id}</span></Col>
                {this.state.focused ?
                    <Col xs={7}>{this.renderFocused()}</Col> :
                    <Col xs={7}><span className="">{this.defaultText()} </span></Col>}
                <Col xs={2} className="text-right">
                    <Button className="text-uppercase" size="sm" color={this.getColor()}
                            onClick={e => this.filterState(e)}>
                        {this.props.translationKey.state}
                    </Button>
                </Col>
            </Row>
        </ListGroupItem>);
    }
}

export const KeyTableEntry = connect(null, mapDispatchToProps)(_KeyTableEntry);
