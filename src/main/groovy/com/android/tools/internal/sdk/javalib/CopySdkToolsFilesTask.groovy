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

package com.android.tools.internal.sdk.javalib

import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public class CopySdkToolsFilesTask extends DefaultTask {

    @InputFile
    File mainJar

    @InputDirectory
    File dependencyFolder

    @InputDirectory
    File noticeFolder

    @InputFiles
    Collection<File> launcherScripts

    @OutputDirectory
    File outputDir

    @TaskAction
    void copy() {
        File outDir = getOutputDir()
        File jar = getMainJar()

        File outLib = new File(outDir, "lib")
        outLib.mkdirs()

        File toMainJar = new File(outLib, jar.name);
        Files.copy(jar, toMainJar)

        File[] files = getDependencyFolder().listFiles()
        if (files != null) {
            for (File file : files) {
                File toFile = new File(outLib, file.name)
                Files.copy(file, toFile)
            }
        }

        files = getNoticeFolder().listFiles()
        if (files != null) {
            for (File file : files) {
                File toFile = new File(outLib, file.name)
                Files.copy(file, toFile)
            }
        }

        for (File launcherScripts : getLauncherScripts()) {
            File toFile = new File(outDir, launcherScripts.name)
            Files.copy(launcherScripts, toFile)
        }
    }
}
