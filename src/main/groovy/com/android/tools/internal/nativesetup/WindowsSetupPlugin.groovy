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

package com.android.tools.internal.nativesetup
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.nativebinaries.platform.Platform
import org.gradle.nativebinaries.toolchain.Gcc
import org.gradle.nativebinaries.toolchain.TargetPlatformConfiguration
/**
 */
class WindowsSetupPlugin implements Plugin<Project> {

    class MingwOnLinuxConfiguration implements TargetPlatformConfiguration {

        List<String> customLinkerArgs = ['-m32']

        boolean supportsPlatform(Platform element) {
            return element.getOperatingSystem().name == "windows"
        }

        List<String> getCppCompilerArgs() {
            ['-DUSE_MINGW', '-D__STDC_FORMAT_MACROS', '-D__STDC_CONSTANT_MACROS', '-D__USE_MINGW_ANSI_STDIO', '-m32']
        }

        List<String> getCCompilerArgs() {
            ['-DUSE_MINGW', '-D__STDC_FORMAT_MACROS', '-D__STDC_CONSTANT_MACROS', '-D__USE_MINGW_ANSI_STDIO', '-m32']
        }

        List<String> getObjectiveCCompilerArgs() {
            []
        }

        List<String> getObjectiveCppCompilerArgs() {
            []
        }

        List<String> getAssemblerArgs() {
            []
        }

        List<String> getLinkerArgs() {
            customLinkerArgs
        }

        List<String> getStaticLibraryArchiverArgs() {
            []
        }
    }

    @Override
    void apply(Project project) {

        project.ext.mingw = new MingwOnLinuxConfiguration()

        project.model {
            platforms {
                windows {
                    architecture "i386"
                    operatingSystem "windows"
                }
            }

            toolChains {
                mingw(Gcc) {
                    path "$project.rootDir/../prebuilts/gcc/linux-x86/host/x86_64-w64-mingw32-4.8/bin"

                    addPlatformConfiguration(project.ext.mingw)

                    getCCompiler().executable =         'x86_64-w64-mingw32-gcc'
                    getCppCompiler().executable =       'x86_64-w64-mingw32-g++'
                    getLinker().executable =            'x86_64-w64-mingw32-g++'
                    getAssembler().executable =         'x86_64-w64-mingw32-as'
                    getStaticLibArchiver().executable = 'x86_64-w64-mingw32-ar'
                }
            }
        }

        project.extensions.create("windows", WindowsExtension, project)
    }

    public static class WindowsExtension {
        Project project

        public WindowsExtension(Project project) {
            this.project = project
        }

        public WindResTask createTask(String taskName,
                String rcPath,
                String imageFolderPath,
                String objName) {

            WindResTask task = project.tasks.create(taskName, WindResTask)

            task.winResExe = project.file("$project.rootDir/../prebuilts/gcc/linux-x86/host/x86_64-w64-mingw32-4.8/bin/x86_64-w64-mingw32-windres")
            task.rcFile = project.file(rcPath)
            task.imageFolder = project.file(imageFolderPath)
            task.objFile = project.file("$project.buildDir/windres/$objName")

            return task
        }
    }
}
