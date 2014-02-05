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
import org.gradle.api.Action
import org.gradle.api.internal.file.FileResolver

/**
 * A Config to build the SDK tools for a given platform.
 *
 * This is mostly a list of files to copy.
 */
class PlatformConfig {

    private final String name
    private final FileResolver fileResolver
    private final List<ToolItem> items = Lists.newArrayList()

    PlatformConfig(String name, FileResolver fileResolver) {
        this.name = name
        this.fileResolver = fileResolver
    }

    String getName() {
        return name
    }

    void path(String path) {
        getToolItem(path)
    }

    void file(File file) {
        getToolItem(file, null)
    }

    void path(String path, Action<ToolItem> config) {
        ToolItem item = getToolItem(path)
        config.execute(item)
    }

    void file(File file, Action<ToolItem> config) {
        ToolItem item = getToolItem(file, null)
        config.execute(item)
    }

    private getToolItem(String path) {
        return getToolItem(fileResolver.resolve(path), path)
    }

    private getToolItem(File file, String path) {
        ToolItem item = new ToolItem(file, path)
        items.add(item)
        return item
    }

    List<ToolItem> getItems() {
        return items
    }

    List<Object> getBuiltBy() {
        List<Object> objects = Lists.newArrayListWithExpectedSize(items.size())
        for (ToolItem item : items) {
            objects.addAll(item.getBuiltByTasks())
        }

        return objects
    }
}
