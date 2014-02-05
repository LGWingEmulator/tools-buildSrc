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

package com.android.tools.internal.sdk.base

import org.gradle.api.Action
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator;

/**
 * Base extension for project publishing to the SDK Tools.
 */
public class BaseExtension {

    private PlatformConfig linuxConfig
    private PlatformConfig macConfig
    private PlatformConfig winConfig

    public BaseExtension(Instantiator instantiator, FileResolver fileResolver) {
        linuxConfig = instantiator.newInstance(PlatformConfig.class, "linux", fileResolver)
        macConfig = instantiator.newInstance(PlatformConfig.class, "mac", fileResolver)
        winConfig = instantiator.newInstance(PlatformConfig.class, "win", fileResolver)
    }

    void linux(Action<PlatformConfig> action) {
        action.execute(linuxConfig)
    }

    void mac(Action<PlatformConfig> action) {
        action.execute(macConfig)
    }

    void windows(Action<PlatformConfig> action) {
        action.execute(winConfig)
    }

    void common(Action<PlatformConfig> action) {
        action.execute(linuxConfig)
        action.execute(macConfig)
        action.execute(winConfig)
    }

    PlatformConfig getLinux() {
        return linuxConfig
    }

    PlatformConfig getMac() {
        return macConfig
    }

    PlatformConfig getWin() {
        return winConfig
    }

    List<PlatformConfig> getPlatforms() {
        return [linuxConfig, macConfig, winConfig]
    }
}
