package com.ccnio.xplu.utils

import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.regex.Pattern
import kotlin.experimental.and

/**
 * Created by jianfeng.li on 2021/2/28.
 */
//todo
fun getCurrentVariant(project: Project): String {
    val gradle = project.gradle
    val tskReqStr = gradle.startParameter.taskRequests.toString()

    val pattern =
        if (tskReqStr.contains("assemble")) Pattern.compile("assemble(\\w+)(Release|Debug)") //"assemble(\\w*)(Release|Debug)"
        else Pattern.compile("generate(\\w+)(Release|Debug)")

    val matcher = pattern.matcher(tskReqStr)
    return if (matcher.find()) {
        println(matcher.group())
//        println(matcher.group(0))
        println(matcher.group(1))
        println(matcher.group(2))
        matcher.group(1).toLowerCase()
    } else {
        println("not matched")
        ""
    }
}

//todo
fun getCurrentFlavor(project: Project): String {
    val gradle = project.gradle
    val tskReqStr = gradle.startParameter.taskRequests.toString()

    val pattern =
        if (tskReqStr.contains("assemble")) Pattern.compile("assemble(\\w+)(Release|Debug)")
        else Pattern.compile("generate(\\w+)(Release|Debug)")

    val matcher = pattern.matcher(tskReqStr)
    return if (matcher.find())
        matcher.group(1).toLowerCase()
    else {
        ""
    }
}


private fun bytesToHexString(bytes: ByteArray?): String? {
    val sb = StringBuilder()
    if (bytes == null || bytes.isEmpty()) {
        return null
    }
    for (b in bytes) {
        val hex = Integer.toHexString((b and 0xFF.toByte()).toInt())
        if (hex.length < 2) {
            sb.append(0)
        }
        sb.append(hex)
    }
    return sb.toString()
}

fun getFileMD5(file: File): String? {
    return bytesToHexString(getFileMD5Bytes(file))
}

fun getFileMD5Bytes(file: File): ByteArray? {
    if (!file.isFile) {
        return null
    }
    val digest: MessageDigest
    val `in`: FileInputStream
    val buffer = ByteArray(1024)
    var len: Int
    try {
        digest = MessageDigest.getInstance("MD5")
        `in` = FileInputStream(file)
        while (`in`.read(buffer, 0, 1024).also { len = it } != -1) {
            digest.update(buffer, 0, len)
        }
        `in`.close()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return digest.digest()
}