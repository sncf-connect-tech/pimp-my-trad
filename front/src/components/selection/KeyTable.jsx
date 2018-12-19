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
import { ListGroup, Container, ListGroupItem } from 'reactstrap';
import { KeyTableEntry } from './KeyTableEntry';
import { allKeysetsWithNames } from '../../app/models/Project';
import ListGroupItemHeading from 'reactstrap/lib/ListGroupItemHeading';
import { getPrettyLanguage, Language } from '../../app/models/KeysetLangMapping';
const mapState = state => ({
    keysets: allKeysetsWithNames(state.main.all ?
        state.main.projects : state.main.projects.filter(p => p.name === state.main.selected)),
    query: state.main.search.toLowerCase(),
    state: state.main.state
});
class _KeyTable extends React.Component {
    hasKeys() {
        return Object.keys(this.props.keysets).some(name => {
            return this.props.keysets[name].some((keyset) => Object.keys(keyset.keys).length > 0);
        });
    }
    iterateKeysets() {
        return Object.keys(this.props.keysets)
            .map(projectName => this.props.keysets[projectName]
            .map(keyset => [projectName, keyset]))
            .reduce((acc, cur) => acc.concat(cur), []);
    }
    match(id, key) {
        return (this.props.query.length === 0
            || id.toLowerCase().indexOf(this.props.query) > -1
            || key.translation(Language.French).toLowerCase().indexOf(this.props.query) > -1)
            && (this.props.state == null
                || key.state.toLowerCase() === (this.props.state || '').toLowerCase());
    }
    render() {
        return (this.hasKeys() ?
            this.iterateKeysets().map(([project, keyset]) => (<ListGroup key={keyset.id} className="my-3">
                            <ListGroupItem>
                                <ListGroupItemHeading>{keyset.name}</ListGroupItemHeading>
                                {keyset.supportedLanguages.map(getPrettyLanguage).join(', ')}
                            </ListGroupItem>
                            {Object.keys(keyset.keys)
                .filter(key => this.match(key, keyset.keys[key]))
                .map(key => <KeyTableEntry languages={keyset.supportedLanguages} key={key} translationKey={keyset.keys[key]} id={key} keysetId={keyset.id} projectName={project}/>)}
                        </ListGroup>)) :
            (<Container className="text-center text-muted py-3" fluid>
                        <h4>Pas de clés. Importez les à partir de l'onglet "Fichiers".</h4>
                    </Container>));
    }
}
export const KeyTable = connect(mapState)(_KeyTable);
