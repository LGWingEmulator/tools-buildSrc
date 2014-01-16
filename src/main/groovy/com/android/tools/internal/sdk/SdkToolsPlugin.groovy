/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.tools.internal.sdk

import com.google.common.collect.Lists
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip

public class SdkToolsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        File sdkRoot = new File(project.rootProject.buildDir, "sdk-tools")

        // main task
        Task packageTask = project.tasks.create("packageSdkTools")

        Task cleanFolder = project.tasks.create("cleanSdkToolsFolder")
        cleanFolder.doFirst {
            sdkRoot.deleteDir()
            sdkRoot.mkdirs()
        }

        Task copyFiles = project.tasks.create("copySdkToolsFiles")
        copyFiles.mustRunAfter cleanFolder


        Zip zipFiles = project.tasks.create("zipSdkToolsFolder", Zip)
        zipFiles.from(sdkRoot)
        zipFiles.into(new File(project.buildDir, "sdk-tools.zip"))
        zipFiles.mustRunAfter copyFiles

        packageTask.description = "Packages the SDK Tools"
        packageTask.group = "build"
        packageTask.dependsOn cleanFolder, copyFiles, zipFiles

        // under this:
        // - build task (depends on all the sub projects build task).
        // - copy task (mustRunAfter build task, or after the sub project task?)
        // - zip task.

        project.afterEvaluate {
            List<Task> copyTasks = Lists.newArrayList()
            project.subprojects.each { p ->
                NamedDomainObjectSet<Task> matches = p.tasks.matching { t ->
                    "copySdkToolsFiles".equals(t.name)
                }
                copyTasks.addAll(matches)
            }

            copyFiles.dependsOn copyTasks
        }
    }
}
