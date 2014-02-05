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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

/**
 * A file going in the tools folder of the SDK.
 * Contains the original file (or relative path from the project), an optional
 * subfolder where to put it, whether it's an executable (to reset its +x flag),
 * and what is required to build it.
 */
class ToolItem {

    private final File file
    private final String fromPath
    private List<Object> builtByTasks

    private String path
    private String name
    private boolean flatten = false
    private boolean executable = false

    ToolItem(File file, String fromPath) {
        this.file = file
        this.fromPath = fromPath
    }

    ToolItem(File file) {
        this(file, null)
    }

    void into(String path) {
        this.path = path
    }

    void name(String name) {
        this.name = name
    }

    void executable(boolean b) {
        this.executable = b
    }

    void flatten(boolean b) {
        this.flatten = b
    }

    void builtBy(Object... tasks) {
        if (builtByTasks == null) {
            builtByTasks = Lists.newArrayListWithExpectedSize(tasks.length)
        }

        Collections.addAll(builtByTasks, tasks)
    }

    @InputFile
    File getFile() {
        return file
    }

    @Input @Optional
    String getPath() {
        return path
    }

    @Input @Optional
    String getName() {
        return name
    }

    @Input @Optional
    boolean getFlatten() {
        return flatten
    }

    @Input @Optional
    boolean getExecutable() {
        return executable
    }

    List<Object> getBuiltByTasks() {
        if (builtByTasks == null) {
            return Collections.emptyList()
        }
        return builtByTasks
    }

    String getFromPath() {
        return fromPath
    }
}
