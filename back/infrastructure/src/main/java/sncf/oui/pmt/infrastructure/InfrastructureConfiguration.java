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

package sncf.oui.pmt.infrastructure;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;
import sncf.oui.pmt.domain.TranslateService;

@Configuration
public class InfrastructureConfiguration {

    @Bean
    public MongoClient mongoClient(@Value("${mongohost}") String mongoHost) {
        return MongoClients.create("mongodb://" + mongoHost);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient client, CodecRegistry codecRegistry) {
        return client.getDatabase("pimpmytrad")
                .withCodecRegistry(codecRegistry);
    }

    @Bean
    public CodecRegistry codecRegistry(Codec<?>[] codecs) {
        return CodecRegistries.fromRegistries(
                MongoClients.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(codecs)
        );
    }

    @Bean
    public CredentialsProvider userProvider(@Value("${username}") String username, @Value("${password}") String password) {
        return new UsernamePasswordCredentialsProvider(username, password);
    }

    @Bean
    public TranslateService googleTranslate(@Value("${googleApiKey}") String apiKey) {
        return new GoogleTranslateService(apiKey);
    }

    @Bean
    @Profile("test")
    public AuthenticationDetails fakeAuthenticationDetails() {
        return () -> Mono.just("test_user");
    }

    @Bean
    @Profile("!test")
    public AuthenticationDetails authenticationDetails() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName());
    }
}
