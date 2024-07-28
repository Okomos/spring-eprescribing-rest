/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.labs.eprescribing.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Java config for Springfox swagger documentation plugin
 *
 * @author Vitaliy Fedoriv
 */
@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI customOpenAPI() {

        return new OpenAPI().components(new Components()).info(new Info()
                .title("REST ePrescribing backend Api Documentation").version("1.0")
                .termsOfService("ePrescribing backend terms of service")
                .description(
                        "This is REST API documentation of the Spring ePrescribing backend. If authentication is enabled, when calling the APIs use admin/admin")
                .license(swaggerLicense()).contact(swaggerContact()));
    }

    private Contact swaggerContact() {
        Contact eprescribingContact = new Contact();
        eprescribingContact.setName("Vitaliy Fedoriv");
        eprescribingContact.setEmail("vitaliy.fedoriv@gmail.com");
        eprescribingContact.setUrl("https://github.com/spring-eprescribing/spring-eprescribing-rest");
        return eprescribingContact;
    }

    private License swaggerLicense() {
        License eprescribingLicense = new License();
        eprescribingLicense.setName("Apache 2.0");
        eprescribingLicense.setUrl("http://www.apache.org/licenses/LICENSE-2.0");
        eprescribingLicense.setExtensions(Collections.emptyMap());
        return eprescribingLicense;
    }

}
