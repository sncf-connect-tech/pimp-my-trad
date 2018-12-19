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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

//@Configuration
public class KeycloakConfig {

    @Bean
    @Profile("!test")
    public ServerSecurityContextRepository contextRepository() {
        return new KeycloakSecurityContext();
    }

    @Bean
    @Profile("!test")
    public SecurityWebFilterChain springSecurityFilterChain(ServerSecurityContextRepository securityContextRepo, ServerHttpSecurity http) {
        http
                .csrf().disable()
                .httpBasic().and().formLogin().disable()
                .securityContextRepository(securityContextRepo)
                .authorizeExchange().anyExchange().authenticated();
        return http.build();
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
