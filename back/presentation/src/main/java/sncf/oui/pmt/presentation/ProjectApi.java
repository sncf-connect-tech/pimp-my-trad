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

package sncf.oui.pmt.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.application.ImportTranslations;
import sncf.oui.pmt.application.ProjectCreation;
import sncf.oui.pmt.domain.export.ExportDetails;
import sncf.oui.pmt.domain.export.ExportMetaRepository;
import sncf.oui.pmt.domain.export.ExportMetadata;
import sncf.oui.pmt.domain.keyset.Keyset;
import sncf.oui.pmt.domain.keyset.Language;
import sncf.oui.pmt.domain.project.Project;
import sncf.oui.pmt.domain.project.ProjectInput;
import sncf.oui.pmt.domain.project.ProjectMetadata;
import sncf.oui.pmt.domain.project.ProjectMetadataRepository;

import javax.validation.Valid;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/projects")
public class ProjectApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectApi.class);
    private final ProjectMetadataRepository projectMetadataRepository;
    private final ExportMetaRepository exportMetaRepository;
    private final ProjectCreation projectCreation;
    private final ImportTranslations importTranslations;
    private final ExceptionMapper mapper;

    @Autowired
    public ProjectApi(ProjectMetadataRepository projectMetadataRepository,
                      ExportMetaRepository exportMetaRepository,
                      ProjectCreation projectCreation,
                      ImportTranslations importTranslations,
                      ExceptionMapper mapper) {
        this.projectMetadataRepository = projectMetadataRepository;
        this.exportMetaRepository = exportMetaRepository;
        this.projectCreation = projectCreation;
        this.importTranslations = importTranslations;
        this.mapper = mapper;
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(Throwable e) {
        return this.mapper.handle(e);
    }

    @GetMapping("/")
    public Mono<ResponseEntity<List<Project>>> getProjects() {
        return projectMetadataRepository.findAll()
                .flatMap(ProjectMetadata::readProject)
                .collectList()
                .map(projects -> ResponseEntity.ok().body(projects));
    }

    @GetMapping("/{name:.+}")
    public Mono<ResponseEntity<Project>> getProjectByName(@PathVariable("name") String name) {
        return projectMetadataRepository.findByName(name)
                .flatMap(ProjectMetadata::readProject)
                .map(project -> ResponseEntity.ok().body(project));
    }

    @DeleteMapping("/{name:.+}")
    public Mono<ResponseEntity<Void>> deleteProject(@PathVariable("name") String name) {
        return projectMetadataRepository.findByName(name)
                .flatMap(ProjectMetadata::delete)
                .flatMap(projectMetadataRepository::delete)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @PostMapping("/")
    public Mono<ResponseEntity<Project>> addProject(@Valid @RequestBody ProjectInput input) {
        return projectCreation.create(input)
                .flatMap(ProjectMetadata::readProject)
                .map(project -> ResponseEntity.status(HttpStatus.CREATED)
                        .header("Location", String.format("/projects/%s", project.getName()))
                        .body(project));
    }

    @GetMapping("/{name:.+}/files")
    public Mono<ResponseEntity<List<String>>> listFiles(@PathVariable("name") String name, @RequestParam("path") Optional<String> path) {
        LOGGER.info("Calling files endpoint");
        return projectMetadataRepository.findByName(name)
                .flatMap(meta -> meta.listFiles(path.orElse("")))
                .map(files -> ResponseEntity.ok().body(files));
    }

    @PostMapping("/{name:.+}/sync")
    public Mono<ResponseEntity<SyncResult>> syncProject(@PathVariable("name") String name) {
        return projectMetadataRepository.findByName(name)
                .flatMap(ProjectMetadata::attemptSync)
                .map(SyncResult::new)
                .map(res -> ResponseEntity.ok().body(res));
    }

    @PostMapping("/sync")
    public Mono<ResponseEntity<SyncResult>> syncAll() {
        return projectMetadataRepository.findAll()
                .flatMap(ProjectMetadata::attemptSync)
                .all(t -> t)
                .map(SyncResult::new)
                .map(res -> ResponseEntity.ok().body(res));
    }

    @GetMapping("/{name:.+}/export")
    public Mono<ResponseEntity<ExportDetails>> export(@PathVariable("name") String name) {
        return projectMetadataRepository.findByName(name)
                .flatMap(ProjectMetadata::readProject)
                .flatMap(ExportDetails::forProject)
                .flatMap(d -> exportMetaRepository
                        .save(d.getMetadata())
                        .then(Mono.just(d)))
                .map(exportDetails -> ResponseEntity.status(HttpStatus.CREATED).body(exportDetails));
    }

    @GetMapping("/export")
    public Mono<ResponseEntity<ExportDetails>> exportAll() {
        return projectMetadataRepository.findAll()
                .flatMap(ProjectMetadata::readProject)
                .collectList()
                .flatMap(ExportDetails::forProjects)
                .flatMap(d -> exportMetaRepository
                        .save(d.getMetadata())
                        .then(Mono.just(d)))
                .map(exportDetails -> ResponseEntity.status(HttpStatus.CREATED).body(exportDetails));
    }

    @GetMapping("/recentExports")
    public Mono<ResponseEntity<List<ExportMetadata>>> listRecentExports(@RequestParam("weekNum") Optional<Integer> weekNum) {
        return exportMetaRepository.findRecent(weekNum.orElse(0))
                .collectList()
                .map(res -> ResponseEntity.ok().body(res));
    }

    @PostMapping("/import/{id}")
    public Mono<ResponseEntity<List<Keyset>>> importTranslations(@PathVariable("id") String exportId,
                                                                 ServerWebExchange req) {
        Mono<Map<String, Part>> monoMap = req.getMultipartData()
                .map(MultiValueMap::toSingleValueMap);
        Mono<Language> lang = monoMap
                .map(map -> (FormFieldPart) map.get("language"))
                .map(FormFieldPart::value)
                .map(Language::valueOf);
        Mono<ByteBuffer> bufferMono = DataBufferUtils.join(monoMap
                .map(map -> map.get("file"))
                .flux()
                .flatMap(Part::content))
                .map(DataBuffer::asByteBuffer);
        return bufferMono.zipWith(lang)
                .flatMap(pair -> {
                    ByteBuffer buffer = pair.getT1();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes, buffer.position(), buffer.limit());
                    return importTranslations.importFromBytes(bytes, pair.getT2(), exportId)
                            .collectList()
                            .map(list -> ResponseEntity.ok().body(list));
                })
                .onErrorResume(e -> {
                    LOGGER.error("An error occured:", e);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
}