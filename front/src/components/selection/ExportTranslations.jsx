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
import { blobFrom } from '../../app/models/ExportDetails';
import { exportSelectedProject } from '../../app/actions/ImportExportAction';
import { Card, CardBody, CardTitle, CardText, Button } from 'reactstrap';
const mapState = state => ({
    exported: state.importsExports.exported,
    projectName: state.main.selected
});
const mapDispatch = dispatch => ({
    exportSelected: () => dispatch(exportSelectedProject())
});
class _ExportTranslations extends React.Component {
    exported() {
        return this.props.exported !== null;
    }
    exportButton() {
        return (<Button color="secondary" size="lg" onClick={() => this.props.exportSelected()}>
                Exporter {this.props.projectName === null ? 'tout' : `"${this.props.projectName}"`}
            </Button>);
    }
    downloadButton() {
        if (this.props.exported === null) {
            return '';
        }
        let id = this.props.exported.metadata.exportId.toUpperCase();
        let href = blobFrom(this.props.exported);
        return (<Button color="primary" size="lg" href={href} download={`export_${id}.csv`}>
                {`Export ${id}`}
            </Button>);
    }
    render() {
        return (<Card>
                <CardBody>
                    <CardTitle>Exporter</CardTitle>
                    <CardText>
                        Les clés à traduire seront exportées dans un fichier CSV.<br />
                        <b>N'oubliez pas de noter le numéro d'export!</b>
                    </CardText>
                    <p className="text-center">
                        {this.exported() ? this.downloadButton() : this.exportButton()}
                    </p>
                </CardBody>
            </Card>);
    }
}
export const ExportTranslations = connect(mapState, mapDispatch)(_ExportTranslations);
