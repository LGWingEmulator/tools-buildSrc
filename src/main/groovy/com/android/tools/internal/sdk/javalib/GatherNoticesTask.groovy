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
import com.google.common.base.Charsets
import com.google.common.base.Joiner
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.io.Files
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class GatherNoticesTask extends BaseTask {

    @OutputDirectory
    File outputDir

    @InputFiles
    Collection<File> getInputFiles() {
        return project.configurations.compile.files
    }

    @InputDirectory
    File repoDir

    @TaskAction
    public void gatherNotices() {
        File outDir = getOutputDir()
        outDir.deleteDir()
        outDir.mkdirs()

        Set<ModuleVersionIdentifier> dependencyCache = Sets.newHashSet()

        File fromFile = new File(project.projectDir, "NOTICE")
        if (!fromFile.isFile()) {
            throw new GradleException("Missing NOTICE for project: " + project.name)
        }

        // copy to the noticeDir folder, adding a header
        File toFile = new File(outDir, "NOTICE_" + project.archivesBaseName + ".jar.txt")
        copyNoticeAndAddHeader(fromFile, toFile, project.name + ".jar")

        Configuration configuration = project.configurations.compile
        gatherFromConfiguration(configuration, dependencyCache, getRepoDir(), outDir)
    }

    private void gatherFromConfiguration(Configuration configuration,
                                                Set<ModuleVersionIdentifier> dependencyCache,
                                                File repoDir, File outDir) {
        File toFile, fromFile
        Set<ResolvedArtifact> artifacts = configuration.resolvedConfiguration.resolvedArtifacts

        StringBuilder sb = new StringBuilder()
        for (ResolvedArtifact artifact : artifacts) {
            sb.setLength(0)
            sb.append("${artifact.moduleVersion.id.toString()} > ")

            try {
                // check it's not an android artifact or a local artifact
                if (isAndroidArtifact(artifact.moduleVersion.id)) {
                    sb.append("SKIPPED (android)")
                } else if (isLocalArtifact(artifact.moduleVersion.id)) {
                    sb.append("SKIPPED (local)")
                } else if (!isValidArtifactType(artifact)) {
                    sb.append("SKIPPED (type = ${artifact.type})")
                } else {
                    ModuleVersionIdentifier id = artifact.moduleVersion.id
                    // manually look for the NOTICE file in the repo
                    if (!dependencyCache.contains(id)) {
                        dependencyCache.add(id)

                        fromFile = new File(repoDir,
                                id.group.replace('.', '/') +
                                        '/' + id.name + '/' + id.version + '/NOTICE')
                        if (!fromFile.isFile()) {
                            sb.append("Error: Missing NOTICE file")
                            throw new GradleException(
                                    "Missing NOTICE file: " + fromFile.absolutePath)
                        }

                        toFile = new File(outDir, "NOTICE_" + artifact.file.name + ".txt")

                        sb.append("${toFile.absolutePath}")

                        copyNoticeAndAddHeader(fromFile, toFile, artifact.file.name)
                    } else {
                        sb.append("SKIPPED (already processed)")
                    }
                }
            } finally {
                logger.info(sb.toString())
            }
        }
    }

    private static void copyNoticeAndAddHeader(File from, File to, String name) {
        List<String> lines = Files.readLines(from, Charsets.UTF_8)
        List<String> noticeLines = Lists.newArrayListWithCapacity(lines.size() + 4)
        noticeLines.addAll([
            "============================================================",
            "Notices for file(s):",
            name,
            "------------------------------------------------------------"
        ]);
        noticeLines.addAll(lines);

        Files.write(Joiner.on("\n").join(noticeLines.iterator()), to, Charsets.UTF_8)
    }
}
