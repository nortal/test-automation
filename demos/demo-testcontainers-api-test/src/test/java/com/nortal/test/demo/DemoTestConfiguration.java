/**
 * Copyright (c) 2023 Nortal AS
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nortal.test.demo;

import com.nortal.test.testcontainers.configurator.SpringBootTestContainerConfigurator;
import com.nortal.test.testcontainers.configurator.TestContainerConfigurator;
import com.nortal.test.testcontainers.images.builder.ImageFromDockerfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableFeignClients(basePackages = {"com.nortal.test.demo.api"})
public class DemoTestConfiguration {

    @Bean
    public TestContainerConfigurator testContainerConfigurator() {
        return new SpringBootTestContainerConfigurator();
    }

    @Bean
    public SpringBootTestContainerConfigurator.TestContainerCustomizer testContainerCustomizer() {
        return new SpringBootTestContainerConfigurator.TestContainerCustomizer() {

            @Override
            public void customizeDockerFileBuilder(@NotNull DockerfileBuilder builder) {
            }

            @Override
            public void customizeImageDefinition(@NotNull ImageFromDockerfile reusableImageFromDockerfile) {
            }

            @NotNull
            @Override
            public List<String> customizeCommandParts() {
                return new ArrayList<>();
            }

            @NotNull
            @Override
            public List<Integer> additionalExposedPorts() {
                return Collections.singletonList(8080);
            }
        };
    }
}
