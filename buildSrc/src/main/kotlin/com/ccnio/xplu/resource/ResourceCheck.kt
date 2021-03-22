package com.ccnio.xplu.resource

import com.ccnio.xplu.utils.ATTRIBUTE_NAME
import com.ccnio.xplu.utils.DIR_RES_VALUES
import com.ccnio.xplu.utils.Logger
import com.ccnio.xplu.utils.getFileMD5
import groovy.util.Node
import groovy.util.XmlParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * Created by jianfeng.li on 21-3-4.
 */
@CacheableTask
open class ResourceCheck : DefaultTask() {
    //    @Internal
    private val ignores = HashMap<String, HashSet<String>>()

    @Internal
    var conflict = false

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    @get:SkipWhenEmpty
    var ignoreFile: File? = null

    @get:OutputFile
    var outputFile: File? = null

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    var resourceFiles: FileCollection? = null

    @TaskAction
    fun doAction() {
        conflict = false
        readIgnoreList(ignoreFile)
        resourceFiles?.files?.filter { it.exists() && !it.absolutePath.contains("transforms") }
            ?.forEach { fileOrDir ->
                fileOrDir.walk().filter { it.exists() && it.isFile }.forEach {
                    if (isValuesRes(it)) readValues(it)
                    else readFile(it)
                }
            }

        printConflict(outputFile)
    }


    private fun isValuesRes(file: File) = file.parentFile.name.contains(DIR_RES_VALUES)
    private val resourceTypeMap = HashMap<String, HashMap<String, ResList>>()

    /**
     * type: string/color
     * ---- name: str1
     */
    private fun readValues(file: File) {
        val resources = try {
            XmlParser().parse(file)
        } catch (e: Exception) {
            Logger.e("parse file $file error!")
            null
        } ?: return

        resources.children().forEach { node ->
            node as Node
            val type = node.name().toString()
            val name = node.attribute(ATTRIBUTE_NAME)?.toString()
            val value = node.text()
            if (!name.isNullOrEmpty() && !value.isNullOrEmpty()) {
                var resourceMap = resourceTypeMap[type]
                if (resourceMap == null) {
                    resourceMap = HashMap()
                    resourceTypeMap[type] = resourceMap
                }

                val res = ResourceInfo(name, value, file.path)
                addRes(resourceMap, res)
            }
        }
    }

    private fun readFile(file: File) {
        val name = file.name
//        Logger.w("readFile: ${file.name}")
        val value = getFileMD5(file) ?: return
        val res = ResourceInfo(name, value, file.path, true)//name value path

        var resourceMap = resourceTypeMap[res.dir]
        if (resourceMap == null) {
            resourceMap = HashMap()
            resourceTypeMap[res.dir] = resourceMap
        }

        addRes(resourceMap, res)
    }

    private fun addRes(
        resourceMap: HashMap<String, ResList>,
        res: ResourceInfo
    ) {
        var values = resourceMap[res.id]
        if (values == null) {
            values = ResList()//HashSet()
            resourceMap[res.id] = values //"$dir@$name"
        }
        val resSet = values.resSet
        if (!values.conflict && !values.containsRes(res.value) && resSet.isNotEmpty()) {
            values.conflict = true
        }

        resSet.add(res)
    }

    private fun printConflict(file: File?) {
        file ?: return

        val dir = file.parentFile
        if (!dir.exists()) dir.mkdirs()
        if (!file.exists()) file.createNewFile()
        val printWriter = file.printWriter()
        printWriter.write("update at ${Date().toLocaleString()}\n\n")
        resourceTypeMap.forEach { (type, map) ->
            var typeDivider = false  //resourceTypeMap = HashMap<String, HashMap<String, ResList>>()
            map.filter { it.value.conflict }.forEach { (key, valuesSet) ->
                if (!typeDivider) {
                    printWriter.write("**************** $type ****************\n")
                    typeDivider = true
                }

                var keyDivider = false
                valuesSet.resSet.forEach {
                    if (!keyDivider) {
                        printWriter.write("<<<<<<<< $key >>>>>>>\n")
                        keyDivider = true
                    }
                    val ignoreValues = ignores[type]
                    val out =
                        if (ignoreValues != null && ignoreValues.contains(it.id)) "ignored::$it"
                        else {
                            if (!conflict) conflict = true
                            it.toString()
                        }
                    printWriter.write("$out\n")
                }
                printWriter.write("\n")
            }

        }
        printWriter.flush()
        printWriter.close()
    }

    private fun readIgnoreList(file: File?) {
        file ?: return
        if (!file.exists()) return

        ignores.clear()
        file.readLines().forEach {
            val split = it.split("#")
            if (split.size == 2) {
                val key = split[0]
                var set = ignores[key]
                if (set == null) set = HashSet()
                set.add(split[1])

                ignores[key] = set
            }
        }
    }
}