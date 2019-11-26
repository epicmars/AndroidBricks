package com.androidpi.tools.profile

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProfilePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("profile", ProfileExtension::class.java)
        project.gradle.addListener(BuildTimeListener(project))
    }
}