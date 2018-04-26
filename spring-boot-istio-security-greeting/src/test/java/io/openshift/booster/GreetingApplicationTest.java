/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster;

import com.jayway.restassured.RestAssured;
import io.openshift.booster.service.NameServiceProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingApplicationTest {

    @MockBean
    private RestTemplate mockRestTemplate;

    @Autowired
    private NameServiceProperties nameServiceProperties;

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setup() {
        RestAssured.baseURI = String.format("http://localhost:%s/api", port);

        given(mockRestTemplate.getForObject(nameServiceProperties.getUrl(), String.class)).willReturn("World");
    }

    @Test
    public void shouldGetGreeting() {
        when().get("greeting")
                .then()
                .assertThat()
                .statusCode(is(equalTo(200)))
                .and()
                .body(is(equalTo("Hello, World!")));
    }

}