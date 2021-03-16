package com.ccnio.xplu

import com.ccnio.xplu.brick.getTaskInfo
import com.ccnio.xplu.utils.IMPLEMENTATION
import com.ccnio.xplu.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by jianfeng.li on 2021/3/16.
 */
private const val TAG = "ToyBrick"
private const val DEPENDENCY_NAME = "brick"

class BrickPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        Logger.make(project, TAG)
        project.configurations.create(DEPENDENCY_NAME)
        val assembleTask = getTaskInfo(project.gradle.startParameter.taskNames)

        project.afterEvaluate { _ ->
            if (assembleTask.isAssemble) {
                val con = project.configurations.getByName(DEPENDENCY_NAME)
                con.allDependencies.forEach {
                    Logger.i("$it")
                    project.dependencies.add(IMPLEMENTATION, it)
                }
            }
        }
    }
}