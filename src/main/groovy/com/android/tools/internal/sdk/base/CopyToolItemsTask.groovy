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

package com.android.tools.internal.sdk.base

import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
/**
 */
class CopyToolItemsTask extends DefaultTask {

    List<ToolItem> items;

    File outputDir

    @TaskAction
    void copy() {
        File outDir = getOutputDir()

        Project p = getProject()

        if (items != null) {
            for (ToolItem item : items) {
                File sourceFile = item.getSourceFile(p)

                File toFolder = outDir
                if (item.getDestinationPath() != null) {
                    toFolder = new File(outDir, item.getDestinationPath())//.replace('/', File.separatorChar))
                    toFolder.mkdirs()
                }

                if (sourceFile.isFile()) {
                    File toFile = copyFile(sourceFile, toFolder, item)
                    if (item.getExecutable()) {
                        toFile.setExecutable(true)
                    }
                } else if (sourceFile.isDirectory()) {
                    copyFolderItems(sourceFile, toFolder, item.getFlatten())
                } else {
                    throw new RuntimeException("Missing sdk-files: ${sourceFile}")
                }
            }
        }
    }

    protected File copyFile(File fromFile, File toFolder, ToolItem item) {
        File toFile = new File(toFolder, (item != null && item.getName() != null) ? item.getName() : fromFile.name)

        String fromPath = item != null ? item.getSourcePath() : null
        if (fromPath != null) {
            logger.info("$fromPath -> $toFile")
        } else {
            logger.info("$fromFile -> $toFile")
        }
        Files.copy(fromFile, toFile)

        return toFile
    }

    private void copyFolderItems(File folder, File destFolder, boolean flatten) {
        File[] files = folder.listFiles()
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    copyFile(file, destFolder, null)
                } else if (file.isDirectory()) {
                    File newToFolder = destFolder
                    if (!flatten) {
                        newToFolder = new File(destFolder, file.name)
                        newToFolder.mkdirs()
                    }

                    copyFolderItems(file, newToFolder, flatten)
                }
            }
        }
    }
}
