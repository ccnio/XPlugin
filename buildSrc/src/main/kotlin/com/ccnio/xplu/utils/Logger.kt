package com.ccnio.xplu.utils

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.text.SimpleDateFormat

/**
 * Created by jianfeng.li on 2021/2/26.
 */
@Suppress("SimpleDateFormat")
object Logger {
    private lateinit var project: Project
    private lateinit var tag: String
    private const val enableLogLevel = true
    private const val enableLogConsole = true
    private val format by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS") }

    fun make(project: Project, tag: String) {
        this.project = project
        this.tag = tag
    }

    fun i(msg: String) {
        val message = format.format(System.currentTimeMillis()) + " $tag: $msg"
        if (enableLogLevel) {
            project.logger.log(LogLevel.INFO, message)
        }
        if (enableLogConsole) {
            println(message)
        }
    }

    fun e(msg: String) {
        val message = format.format(System.currentTimeMillis()) + " $tag: $msg"
        if (enableLogLevel) {
            project.logger.log(LogLevel.ERROR, message)
        }
        if (enableLogConsole) {
            println(message)
        }
    }
}