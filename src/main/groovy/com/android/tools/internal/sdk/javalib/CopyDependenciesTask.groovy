/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.tools.internal.sdk.javalib
import com.android.tools.internal.BaseTask
import com.google.common.io.Files
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CopyDependenciesTask extends BaseTask {

    @OutputDirectory
    File outputDir

    @InputFiles
    Collection<File> getInputFiles() {
        return project.configurations.compile.files
    }

    @TaskAction
    public void copyDependencies() {
        File outDir = getOutputDir()
        outDir.deleteDir()
        outDir.mkdirs()

        Configuration configuration = project.configurations.compile
        Set<ResolvedArtifact> artifacts = configuration.resolvedConfiguration.resolvedArtifacts

        StringBuilder sb = new StringBuilder()
        for (ResolvedArtifact artifact : artifacts) {
            sb.setLength(0)
            sb.append("${artifact.moduleVersion.id.toString()} > ")
            // check it's not an android artifact or a local artifact
            if (isAndroidArtifact(artifact.moduleVersion.id)) {
                sb.append("SKIPPED (android)")
            } else if (isLocalArtifact(artifact.moduleVersion.id)) {
                sb.append("SKIPPED (local)")
            } else if (!isValidArtifactType(artifact)) {
                sb.append("SKIPPED (type = ${artifact.type})")
            } else {
                File dest = new File(outDir, artifact.file.name)
                sb.append("${dest.absolutePath}")
                Files.copy(artifact.file, dest)
            }

            logger.info(sb.toString())
        }
    }
}
