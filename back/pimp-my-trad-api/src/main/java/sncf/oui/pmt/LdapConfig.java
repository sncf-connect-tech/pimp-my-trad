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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ServerHttpBasicAuthenticationConverter;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Configuration
public class LdapConfig {

    @Value("${ldap.url}") private String ldapUrl;
    @Value("${ldap.domain}") private String ldapDomain;

    @Bean
    @Profile("!test")
    public SecurityWebFilterChain springSecurityFilterChain(ReactiveAuthenticationManager authenticationManager,
                                                            ServerAuthenticationEntryPoint entryPoint,
                                                            WebFilter webFilter,
                                                            ServerHttpSecurity http) {
        http
                .csrf().disable()
                .addFilterAt(webFilter, SecurityWebFiltersOrder.HTTP_BASIC)
                .exceptionHandling().authenticationEntryPoint(entryPoint).and()
                .authenticationManager(authenticationManager)
                .authorizeExchange().anyExchange().authenticated();
        return http.build();
    }

    @Bean
    public WebFilter webFilter(ReactiveAuthenticationManager authManager, ServerAuthenticationEntryPoint entryPoint) {
        // copied from source because there is no way to override the entrypoint defined by .httpBasic()
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authManager);
        filter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        filter.setAuthenticationConverter(new ServerHttpBasicAuthenticationConverter());
        filter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(entryPoint));
        return filter;
    }

    @Bean
    public ReactiveAuthenticationManager authManager() {
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(ldapDomain, ldapUrl);
        return new ReactiveAuthenticationManagerAdapter(new ProviderManager(Collections.singletonList(provider)));
    }

    @Bean
    public ServerAuthenticationEntryPoint basicAuthEntryPoint() {
        return (exchange, e) -> Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        });
    }

    @Bean
    @Profile("test")
    public SecurityWebFilterChain springSecurityTestFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable()
                .httpBasic().and().formLogin().disable()
                .authorizeExchange().anyExchange().permitAll();
        return http.build();
    }

}