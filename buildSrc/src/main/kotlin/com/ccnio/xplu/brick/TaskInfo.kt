package com.ccnio.xplu.brick

import com.ccnio.xplu.utils.Logger

/**
 * Created by jianfeng.li on 21-3-16.
 */
class TaskInfo {
    var isAssemble = false
}

@Suppress("DefaultLocale")
fun getTaskInfo(tasks: List<String>): TaskInfo {
    Logger.i("getTaskInfo: tasks = $tasks")
    val info = TaskInfo()
    tasks.forEach { task ->
        if (task.contains("assemble") || task.contains("install")) {
            info.isAssemble = true
            return@forEach
        }
    }
    return info
}
