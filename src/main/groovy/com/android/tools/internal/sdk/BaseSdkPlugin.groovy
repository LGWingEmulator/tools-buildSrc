package com.android.tools.internal.sdk

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by xav on 2/4/14.
 */
class BaseSdkPlugin implements Plugin<Project> {
    protected Project project

    Task copySdkToolsFiles

    @Override
    void apply(Project project) {
        this.project = project

        // create a bunch of base tasks
        copySdkToolsFiles = project.tasks.create("copySdkToolsFiles")

        // create base tasks
        /*
        We need a master task.
        We then need 3 tasks in that order.
        - delete folder
        - copy to folder (depends on subproject if root project)
        - zip project
         */
    }

    protected File getSdkRoot() {
        return new File(project.rootProject.buildDir, "tools")
    }
}
