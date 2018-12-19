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
import { Provider } from 'react-redux';
import { createAppStore } from './app/createAppStore';
import { mainStateDefaults } from './app/reducers/mainReducer';
import { Project } from './app/models/Project';
import App from './components/App';
let store;
let saved = localStorage.getItem('pmt_main');
if (saved !== null) {
    let parsed = JSON.parse(saved);
    let main = {
        ...mainStateDefaults,
        ...parsed,
        projects: parsed.projects.map((p) => Project.from(p))
    };
    store = createAppStore({ main });
}
else {
    store = createAppStore();
}
store.subscribe(() => {
    localStorage.setItem('pmt_main', JSON.stringify(store.getState().main));
});
class Root extends React.Component {
    render() {
        return (<Provider store={store}>
                <App />
            </Provider>);
    }
}
export default Root;
