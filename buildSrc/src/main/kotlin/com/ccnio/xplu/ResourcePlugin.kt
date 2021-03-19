package com.ccnio.xplu

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.core.InternalBaseVariant
import com.android.build.gradle.internal.res.GenerateLibraryRFileTask
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.ccnio.xplu.resource.ResourceCheck
import com.ccnio.xplu.resource.ResourceConfig
import com.ccnio.xplu.utils.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by jianfeng.li on 2021/2/26.
 */
const val TAG = "X-Plu"
private const val TASK_GROUP = "resource"
private const val FILE_IGNORE = "res_conflict_ignore.txt"
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

        project.afterEvaluate {
            variants.forEach { variant ->
                checkConflict(variant, project, config)
                genRefactorTask(config, project, variant)
            }
        }
    }

    private fun checkConflict(
        variant: InternalBaseVariant,
        project: Project,
        config: ResourceConfig
    ) {
        if (!config.checkConflict) return
        var startMills = 0L
        val taskName = "checkResource${variant.name.capitalize()}"
        val outputFile =
            "${project.rootProject.buildDir.absolutePath}${File.separator}$OUTPUT_FILE"
        val task = project.tasks.create(taskName, ResourceCheck::class.java) {
            //                    Logger.i("task action")
            it.resourceFiles = variant.allRawAndroidResources
            it.outputFile = File(outputFile)
            it.ignoreFile =
                File(("${project.rootDir.absolutePath}${File.separator}$FILE_IGNORE"))
        }.doFirst {
            startMills = System.currentTimeMillis()
            //                    Logger.i("doFirst")
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

    private fun genRefactorTask(
        config: ResourceConfig,
        project: Project,
        variant: InternalBaseVariant
    ) {
        val resSrc = config.srcModule
        val resDest = config.destModule
        if (resSrc.isEmpty() || resDest.isEmpty()) {
            Logger.w("srcModule or destModule should not be empty")
            return
        }

        val create =
            project.tasks.create("checkRefactorRes${variant.name.capitalize()}", Exec::class.java) {
//                it.commandLine = listOf("python", "Demo.py")
                it.workingDir = File(project.rootDir.absolutePath)
            }.doFirst { task ->
                task as Exec
                val once = AtomicBoolean()
                var rFile: File? = null
                variant.outputs.all { output ->
                    if (once.compareAndSet(false, true)) {
                        val processResources = output.processResourcesProvider.get()
                        rFile = project.files(
                            when (processResources) {
                                is GenerateLibraryRFileTask -> processResources.getTextSymbolOutputFile()
                                is LinkApplicationAndroidResourcesTask -> processResources.getTextSymbolOutputFile()
                                else -> throw RuntimeException("error")
                            }
                        ).builtBy(processResources).singleFile
                    }
                }
                if (rFile == null || !rFile!!.exists()) {
                    Logger.e("R file not found")
                    return@doFirst
                }
                val args =
                    arrayListOf("python", "ResRefactor.py", resSrc, resDest, rFile!!.absolutePath)
                for (pro in project.rootProject.allprojects) {
                    if (resSrc == pro.name) continue
                    for (conf in pro.configurations) {
                        val find = conf.allDependencies.find { it.name.equals(resSrc) }
                        if (find != null) {
                            args.add(pro.name)
                            break
                        }
                    }
                }
                task.commandLine = args
            }
        create.group = TASK_GROUP
    }

}