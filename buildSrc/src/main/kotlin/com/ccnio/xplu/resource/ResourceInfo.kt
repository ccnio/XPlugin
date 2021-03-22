package com.ccnio.xplu.resource

import java.io.File

/**
 * Created by jianfeng.li on 21-3-2.
 */
class ResourceInfo(
    name: String,
    val value: String,
    private val path: String,
    private val isFile: Boolean = false
) {
    var dir: String = File(path).parentFile.name.replace(
        "-v4", //resolve library dir: drawable-xxhdpi-v4/test.png
        ""
    )
    var id = "$dir@$name"

    override fun toString(): String {
        return if (isFile) "path = ${path.replace("-v4", "")}" else "value = $value, path = $path"
    }
}