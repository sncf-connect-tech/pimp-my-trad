package sncf.oui.pmt.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.DomainDrivenDesign;
import sncf.oui.pmt.domain.project.*;

@DomainDrivenDesign.ApplicationService
@Component
public class ProjectCreation {

    private ProjectMetadataRepository projectMetadataRepository;
    private ProjectMetadataFactory factory;

    @Value("${gitlab}")
    private String gitlab;

    @Autowired
    public ProjectCreation(ProjectMetadataRepository projectMetadataRepository, ProjectMetadataFactory factory) {
        this.projectMetadataRepository = projectMetadataRepository;
        this.factory = factory;
    }

    public Mono<ProjectMetadata> create(ProjectInput input) {
        ProjectMetadata projectMetadata = factory.create(null, input.getFixedUrl(gitlab), input.getSuggestedName());
        return projectMetadataRepository.exists(projectMetadata.getProjectName())
                .flatMap(exists -> exists ? Mono.error(new ProjectAlreadyExistsException()) : Mono.just(projectMetadata))
                .flatMap(projectMetadataRepository::save);
    }
}