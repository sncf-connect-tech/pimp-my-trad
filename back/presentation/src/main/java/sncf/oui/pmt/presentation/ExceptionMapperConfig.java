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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import sncf.oui.pmt.domain.CloneException;
import sncf.oui.pmt.domain.keyset.KeysetNotFoundException;
import sncf.oui.pmt.domain.project.ProjectAlreadyExistsException;
import sncf.oui.pmt.domain.project.ProjectNotFoundException;
import sncf.oui.pmt.domain.keyset.KeyNotFoundException;

import java.util.stream.Collectors;

@Configuration
public class ExceptionMapperConfig {

    @Bean
    public ExceptionMapper handler() {
        return e -> {
            Class<? extends Throwable> theClass = e.getClass();
            if (theClass.equals(ProjectNotFoundException.class)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage("Project not found"));
            } else if (theClass.equals(KeysetNotFoundException.class)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage("Keyset not found"));
            } else if (theClass.equals(KeyNotFoundException.class)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage("Key not found"));
            } else if (theClass.equals(CloneException.class)) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorMessage("Not authorized to clone this project"));
            } else if (theClass.equals(ProjectAlreadyExistsException.class)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessage("A project with that name already exists"));
            } else if (theClass.equals(MethodArgumentNotValidException.class)) {
                String m = ((MethodArgumentNotValidException) e).getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(","));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(m));
            } else {
                e.printStackTrace(System.out);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        };
    }
}

