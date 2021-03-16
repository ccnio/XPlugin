package com.ccnio.xplu.resource

import java.io.File
import java.util.*

/**
 * Created by jianfeng.li on 21-3-2.
 */
class ResourceInfo(
    val name: String,
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
        return if (isFile) "path = $path" else "value = $value, path = $path"
    }

    override fun equals(other: Any?): Boolean {
        return other is ResourceInfo && other.value == value
    }

    override fun hashCode() = Objects.hash(value)
}