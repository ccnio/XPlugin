package com.ccnio.xplu

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.ccnio.xplu.resource.ResourceCheck
import com.ccnio.xplu.resource.ResourceConfig
import com.ccnio.xplu.utils.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*

/**
 * Created by jianfeng.li on 2021/2/26.
 */
private const val TAG = "ResourceConflict"
private const val TASK_GROUP = "resource"
private const val FILE_IGNORE = "resource_check_ignore.txt"
private const val OUTPUT_FILE = "resource_conflict.log"

class ResourcePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        Logger.make(project, TAG)
        project.extensions.create("resourceConfig", ResourceConfig::class.java)
        val config = project.extensions.getByType(ResourceConfig::class.java)

        val variants = when {
            project.plugins.hasPlugin(PLUGIN_APP) -> project.extensions.getByType(AppExtension::class.java).applicationVariants
            project.plugins.hasPlugin(PLUGIN_LIBRARY) -> project.extensions.getByType(
                LibraryExtension::class.java
            ).libraryVariants
            else -> null
        } ?: return

        project.afterEvaluate { _ ->
            if (!config.enable) return@afterEvaluate

            variants.forEach { variant ->
                val startMills = System.currentTimeMillis()

                val taskName = "checkResource${variant.name.capitalize()}"
                val outputFile =
                    "${project.rootProject.buildDir.absolutePath}${File.separator}$OUTPUT_FILE"
                val task = project.tasks.create(taskName, ResourceCheck::class.java) {
                    Logger.i("task action")
                    it.resourceFiles = variant.allRawAndroidResources
                    it.outputFile = File(outputFile)
                    it.ignoreFile =
                        File(("${project.rootDir.absolutePath}${File.separator}$FILE_IGNORE"))
                }.doLast {
                    Logger.i("consume ${System.currentTimeMillis() - startMills}")
                    if (it.property("conflict") as Boolean) {
                        if (config.interruptWhenConflict) {
                            throw Exception("has conflict resource, see detail in $outputFile ~")
                        } else {
                            Logger.e("has conflict resource, see detail in $outputFile ~")
                        }
                    }
                }

                task.group = TASK_GROUP
                val taskCompile =
                    project.tasks.findByName("dexBuilder${variant.name.capitalize()}")
                taskCompile?.dependsOn(task)
            }
        }
    }
}