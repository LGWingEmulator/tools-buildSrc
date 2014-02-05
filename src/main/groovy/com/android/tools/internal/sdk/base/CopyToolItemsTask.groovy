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
import com.google.common.collect.Lists
import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
/**
 */
class CopyToolItemsTask extends DefaultTask {

    List<ToolItem> items;

    File outputDir

    List<File> getInputFiles() {
        if (items == null) {
            return Collections.emptyList()
        }

        List<File> files = Lists.newArrayListWithExpectedSize(items.size())
        for (ToolItem item : items) {
            files.add(item.getFile())
        }

        return files;
    }

    @TaskAction
    void copy() {
        File outDir = getOutputDir()

        if (items != null) {
            for (ToolItem item : items) {
                File fromFile = item.getFile()

                File toFolder = outDir
                if (item.getPath() != null) {
                    toFolder = new File(outDir, item.getPath())//.replace('/', File.separatorChar))
                    toFolder.mkdirs()
                }

                if (fromFile.isFile()) {
                    File toFile = copyFile(fromFile, toFolder, item)
                    if (item.getExecutable()) {
                        toFile.setExecutable(true)
                    }
                } else if (fromFile.isDirectory()) {
                    copyFolderItems(fromFile, toFolder, item.getFlatten())
                } else {
                    throw new RuntimeException("Missing sdk-files: ${fromFile}")
                }
            }
        }
    }

    protected File copyFile(File fromFile, File toFolder, ToolItem item) {
        File toFile = new File(toFolder, (item != null && item.getName() != null) ? item.getName() : fromFile.name)

        String fromPath = item != null ? item.getFromPath() : null
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
