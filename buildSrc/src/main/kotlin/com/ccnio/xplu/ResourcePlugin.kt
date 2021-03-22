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
private const val OUTPUT_FILE = "res_conflict.log"

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
        if (!config.scanConflict) return
        var startMills = 0L
        val taskName = "scanConflictRes${variant.name.capitalize()}"
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
            project.tasks.findByName("process${variant.name.capitalize()}JavaRes")
        taskCompile?.dependsOn(task)
    }

    private fun genRefactorTask(
        config: ResourceConfig,
        project: Project,
        variant: InternalBaseVariant
    ) {
        val resSrc = config.migrateSrc
        val resDest = config.migrateDest
        if (resSrc.isEmpty() || resDest.isEmpty()) return


        val create =
            project.tasks.create("migrateRes${variant.name.capitalize()}", Exec::class.java) {
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
                    arrayListOf(
                        "python",
                        "ResRefactor.py",
                        rFile!!.absolutePath.replace("\\", "/"),
                    )
                project.rootProject.allprojects.filter { resSrc == it.name || resDest == it.name }
                    .forEach { args.add(getResPath(it.projectDir)) }

                for (pro in project.rootProject.allprojects) {
                    if (resSrc == pro.name || resDest == pro.name) continue

                    for (conf in pro.configurations) {
                        val find = conf.allDependencies.find { it.name.equals(resSrc) }
                        if (find != null) {
                            args.add(getResPath(pro.projectDir))
                            break
                        }
                    }
                }
                task.commandLine = args
            }
        create.group = TASK_GROUP
    }

    private fun getResPath(file: File): String {
        Logger.i("file = $file")
        return (file.absolutePath + "/src/main").replace("\\", "/")
    }

}