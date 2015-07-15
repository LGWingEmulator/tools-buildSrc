/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package com.android.tools.internal;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Infrastructure to allow presubmit checks.
 *
 * Rather than run all of the tests, this means only the tests that
 * could have been affected by a change to be run.
 *
 * When passed a file listing absolute paths, one per line,
 * e.g. -Pcom.android.build.presubmitChangedFiles=/path/to/file
 * creates a runPresubmitTests task that depends on buildDependents
 * of all the subprojects that contain files in that list.
 * The buildDependents task task also builds and tests all subprojects
 * that transitively depend on the modified subprojects.
 *
 * The file could be generated by something like
 * <pre>
 * git diff --name-only HEAD `git merge-base HEAD aosp/studio-1.4-dev`  | \
 *           sed 's#^#'`git rev-parse --show-toplevel`'/#' \
 *           > ../../out/presubmit_diff.txt
 * ../gradlew -Pcom.android.build.presubmitChangedFiles=../../out/presubmit_diff.txt :runPresubmitTests
 * </pre>
 */
public class PresubmitRunner implements Plugin<Project> {

    private static final String PRESUBMIT_FILES_PROPERTY = "com.android.build.presubmitChangedFiles";

    @Override
    public void apply(final Project project) {

        if (project.hasProperty(PRESUBMIT_FILES_PROPERTY)) {
            final Logger logger = project.getLogger();
            logger.info("Creating presubmit task.");

            final Task runPresubmitTestsTask = project.getTasks().create("runPresubmitTests");
            runPresubmitTestsTask.setGroup("verification");

            final Iterable<String> files;
            try {
                files = Files.readLines(new File((String) project.property(PRESUBMIT_FILES_PROPERTY)), Charsets.UTF_8);
            } catch (IOException e) {
                project.getLogger().error("Unable to read presubmit file", e);
                return;
            }

            // Find projects that contain files that have changed.
            project.subprojects(new Action<Project>() {
                @Override
                public void execute(final Project subProject) {
                    String subProjectPath = subProject.getProjectDir().toString();
                    for (String file : files) {
                        if (file.startsWith(subProjectPath)) {
                            Object subProjectTestTask = subProject.getTasks().findByName("buildDependents");
                            if (subProjectTestTask != null) {
                                runPresubmitTestsTask.dependsOn(subProjectTestTask);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

}
