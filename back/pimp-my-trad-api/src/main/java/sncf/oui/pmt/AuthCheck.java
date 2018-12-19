package sncf.oui.pmt;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthCheck {

    @GetMapping("/check")
    public Mono<ResponseEntity<Void>> check() {
        return Mono.just(ResponseEntity
                .ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue())
                .build());
    }
}
