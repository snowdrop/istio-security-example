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

package io.openshift.booster.service;

//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.springframework.boot.actuate.health.Health;

//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.BDDMockito.given;

//@RunWith(MockitoJUnitRunner.class)
//public class NameServiceHealthIndicatorTest {

//    @Mock
//    private NameService mockNameService;
//
//    private NameServiceHealthIndicator nameServiceHealthIndicator;
//
//    @Before
//    public void before() {
//        nameServiceHealthIndicator = new NameServiceHealthIndicator(mockNameService);
//    }
//
//    @Test
//    public void shouldIndicateHealthy() {
//        given(mockNameService.getName()).willReturn("World");
//
//        Health actual = nameServiceHealthIndicator.health();
//        Health expected = Health.up()
//                .build();
//
//        assertThat(actual).isEqualTo(expected);
//    }
//
//    @Test
//    public void shouldIndicateUnhealthy() {
//        given(mockNameService.getName()).willThrow(new RuntimeException());
//
//        Health actual = nameServiceHealthIndicator.health();
//        Health expected = Health.down()
//                .build();
//
//        assertThat(actual).isEqualTo(expected);
//    }

//}