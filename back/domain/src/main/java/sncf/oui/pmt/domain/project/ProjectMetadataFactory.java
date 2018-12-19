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

package sncf.oui.pmt.domain.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sncf.oui.pmt.domain.CloneService;
import sncf.oui.pmt.domain.ProjectTree;
import sncf.oui.pmt.domain.SyncService;

@Component
public class ProjectMetadataFactory {

    private String dataRoot;
    private CloneService cloneService;
    private ProjectTree projectTree;
    private SyncService syncService;

    @Autowired
    public ProjectMetadataFactory(@Value("${dataroot}") String dataRoot,
                                  CloneService cloneService,
                                  ProjectTree projectTree,
                                  SyncService syncService) {
        this.dataRoot = dataRoot;
        this.cloneService = cloneService;
        this.projectTree = projectTree;
        this.syncService = syncService;
    }

    public ProjectMetadata create(String id, String origin, String name) {
        ProjectMetadata projectMetadata = new ProjectMetadata(id, origin, name, dataRoot, cloneService, projectTree, syncService);
        cloneService.setProject(projectMetadata);
        syncService.setProject(projectMetadata);
        return projectMetadata;
    }
}
