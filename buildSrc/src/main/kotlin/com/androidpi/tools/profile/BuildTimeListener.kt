package com.androidpi.tools.profile

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

/**
 * Created by jastrelax on 2017/5/24.
 */
internal class BuildTimeListener(project: Project) : TaskExecutionListener, BuildListener {

    var profileExtension: ProfileExtension = project.extensions.getByName("profile") as ProfileExtension
    var profiles: MutableList<BuildProfile> = mutableListOf()
    var currentTimeMs: Long = 0


    override fun beforeExecute(task: Task) {
        currentTimeMs = System.currentTimeMillis()
    }

    override fun afterExecute(task: Task, taskState: TaskState) {
        val elapsedMs = System.currentTimeMillis() - currentTimeMs
        profiles.add(BuildProfile(task.path, elapsedMs))
    }

    override fun buildStarted(gradle: Gradle) {

    }

    override fun settingsEvaluated(settings: Settings) {

    }

    override fun projectsLoaded(gradle: Gradle) {

    }

    override fun projectsEvaluated(gradle: Gradle) {

    }

    override fun buildFinished(buildResult: BuildResult) {
        println("Build profile:")
        for ((task, time) in profiles) {
            if (time > profileExtension.threshold) {
                print("%7sms %s\n".format(time, task))
            }
        }
    }
}
