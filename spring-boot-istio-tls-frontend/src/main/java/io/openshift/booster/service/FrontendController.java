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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FrontendController {

    private final GreetingService greetingService;

    private final NameService nameService;

    public FrontendController(GreetingService greetingService, NameService nameService) {
        this.greetingService = greetingService;
        this.nameService = nameService;
    }

    @RequestMapping("/api/greeting")
    public String getGreeting() {
        try {
            return greetingService.getGreeting();
        } catch (Throwable t) {
            return String.format("Failed to get greeting: %s", t.getMessage());
        }
    }

    @RequestMapping("/api/name")
    public String getName() {
        try {
            return nameService.getName();
        } catch (Throwable t) {
            return String.format("Failed to get name: %s", t.getMessage());
        }

    }

}
