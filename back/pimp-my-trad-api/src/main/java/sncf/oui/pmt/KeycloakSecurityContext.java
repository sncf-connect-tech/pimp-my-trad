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

package sncf.oui.pmt;

import com.google.common.collect.ImmutableSet;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class KeycloakSecurityContext implements ServerSecurityContextRepository {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.auth-server}")
    private String authServer;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.realm-public-key}")
    private String realmPublicKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakSecurityContext.class);
    private static final Authentication NOOP_AUTH = noopAuth();

    private PublicKey publicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(realmPublicKey));
        return kf.generatePublic(spec);
    }

    private static Authentication noopAuth() {
        return new AbstractAuthenticationToken(AuthorityUtils.NO_AUTHORITIES) {
            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }
        };
    }

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        return null;
    }


    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        Authentication auth;
        List<String> header = serverWebExchange.getRequest().getHeaders().get("Authorization");
        assert header != null;
        try {
            String tokenString = header.get(0);
            if (tokenString != null && tokenString.matches("^Bearer (.*)$")) {
                tokenString = tokenString.substring("Bearer".length()).trim();
                Jws<Claims> jws = Jwts.parser().setSigningKey(publicKey())
                        .requireIssuer(String.format("%s/auth/realms/%s", authServer, realm))
                        .requireAudience(clientId)
                        .parseClaimsJws(tokenString);
                Claims claims = jws.getBody();
                OidcIdToken idToken = new OidcIdToken(tokenString, claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant(), claims);
                OAuth2UserAuthority authority = new OAuth2UserAuthority(jws.getBody());
                OidcUser user = new DefaultOidcUser(ImmutableSet.of(authority), idToken);
                auth = new OAuth2AuthenticationToken(user, ImmutableSet.of(authority), clientId);
            } else {
                auth = NOOP_AUTH;
            }
        } catch (NullPointerException | SignatureException | InvalidKeySpecException | NoSuchAlgorithmException | IncorrectClaimException e) {
            LOGGER.error("auth failed:", e);
            auth = NOOP_AUTH;
        }
        return Mono.just(new SecurityContextImpl(auth));

    }
}
