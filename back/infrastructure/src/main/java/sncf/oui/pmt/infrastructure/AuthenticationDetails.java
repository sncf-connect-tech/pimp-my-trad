package sncf.oui.pmt.infrastructure;

import reactor.core.publisher.Mono;

public interface AuthenticationDetails {
    Mono<String> getUser();
}
