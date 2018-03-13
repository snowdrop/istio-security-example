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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class FrontendControllerTest {

    @Mock
    private GreetingService mockGreetingService;

    @Mock
    private NameService mockNameService;

    private FrontendController frontendController;

    @Before
    public void before() {
        frontendController = new FrontendController(mockGreetingService, mockNameService);
    }

    @Test
    public void shouldGetGreeting() {
        given(mockGreetingService.getGreeting()).willReturn("test-greeting");

        String greeting = frontendController.getGreeting();

        assertThat(greeting).isEqualTo("test-greeting");
    }

    @Test
    public void shouldFailToGetGreeting() {
        given(mockGreetingService.getGreeting()).willThrow(new RuntimeException("test-exception"));

        String greeting = frontendController.getGreeting();

        assertThat(greeting).isEqualTo("Failed to get greeting: test-exception");
    }

    @Test
    public void shouldGetName() {
        given(mockNameService.getName()).willReturn("test-name");

        String name = frontendController.getName();

        assertThat(name).isEqualTo("test-name");
    }

    @Test
    public void shouldFailToGetName() {
        given(mockNameService.getName()).willThrow(new RuntimeException("test-exception"));

        String name = frontendController.getName();

        assertThat(name).isEqualTo("Failed to get name: test-exception");
    }

}