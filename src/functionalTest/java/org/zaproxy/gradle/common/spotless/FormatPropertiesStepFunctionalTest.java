/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.gradle.common.spotless;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FormatPropertiesStepFunctionalTest {
    @Test
    void shouldFormatProperties(@TempDir File testProjectDir) throws Exception {
        // Given
        Path originalBuildFilePath =
                Path.of(getClass().getResource("spotlessProperties.gradle.kts").toURI());
        Path copiedBuildFilePath = testProjectDir.toPath().resolve("build.gradle.kts");
        Path originalUnformattedPropertiesPath =
                Path.of(getClass().getResource("unformatted.properties").toURI());
        Path copiedUnformattedPropertiesPath =
                testProjectDir.toPath().resolve("unformatted.properties");
        Path formattedPropertiesPath =
                Path.of(getClass().getResource("formatted.properties").toURI());
        String spotlessPropertiesApplyTaskName = ":spotlessPropertiesApply";

        Files.copy(originalUnformattedPropertiesPath, copiedUnformattedPropertiesPath);
        Files.copy(originalBuildFilePath, copiedBuildFilePath);
        // When
        Path pluginClasspath =
                Path.of(getClass().getClassLoader().getResource("pluginClasspath.txt").toURI());
        List<File> pluginClasspathFiles =
                Files.readAllLines(pluginClasspath).stream()
                        .map(File::new)
                        .collect(Collectors.toList());
        BuildResult result =
                GradleRunner.create()
                        .withProjectDir(testProjectDir)
                        .withArguments(spotlessPropertiesApplyTaskName)
                        .withPluginClasspath(pluginClasspathFiles)
                        .build();
        // Then
        BuildTask spotlessPropertiesApplyTask = result.task(spotlessPropertiesApplyTaskName);
        assertThat(spotlessPropertiesApplyTask, is(notNullValue()));
        assertThat(spotlessPropertiesApplyTask.getOutcome(), is(TaskOutcome.SUCCESS));
        assertThat(
                Files.readString(copiedUnformattedPropertiesPath, StandardCharsets.UTF_8),
                is(equalTo(Files.readString(formattedPropertiesPath, StandardCharsets.UTF_8))));

        var properties = new Properties();
        properties.load(
                new InputStreamReader(
                        Files.newInputStream(copiedUnformattedPropertiesPath),
                        StandardCharsets.UTF_8));
        assertThat(properties.size(), is(22));
        assertThat(properties.getProperty("commas"), is("value1, value2, value3"));
        assertThat(properties.getProperty("spaces.after.separator"), is("value"));
        assertThat(properties.getProperty("no.value"), is(""));
        assertThat(properties.getProperty("colon.separator"), is("value"));
        assertThat(properties.getProperty("unicode.unencoded"), is("नमस्ते"));
        assertThat(properties.getProperty("spaces in key"), is("value"));
        assertThat(properties.getProperty("quoted.args"), is("value \"{0}\""));
        assertThat(properties.getProperty("single.quotes"), is("'value'"));
        assertThat(properties.getProperty("trailing.whitespace"), is("value    "));
        assertThat(properties.getProperty("unicode.whitespace"), is(" hello world "));
        assertThat(properties.getProperty("quotes"), is("\"value\""));
        assertThat(properties.getProperty("args"), is("value {0}"));
        assertThat(properties.getProperty("single.quoted.args"), is("value ''{0}''"));
        assertThat(properties.getProperty("escape.chars"), is("\nvalue1\nvalue2\tvalue3\t"));
        assertThat(properties.getProperty("spaces.before.separator"), is("value"));
        assertThat(properties.getProperty("no.spaces.separator"), is("value"));
        assertThat(properties.getProperty("no.value.with.separator"), is(""));
        assertThat(properties.getProperty("equals"), is("value=1"));
        assertThat(properties.getProperty("unicode.encoded"), is("नमस्ते"));
        assertThat(properties.getProperty("space.separator"), is("value"));
        assertThat(properties.getProperty("multiline.value"), is("value1 value2 value3"));
        assertThat(properties.getProperty("leading.escaped.space"), is(" value"));
    }
}