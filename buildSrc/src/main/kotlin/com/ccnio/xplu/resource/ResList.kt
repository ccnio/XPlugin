package com.ccnio.xplu.resource

/**
 * Created by jianfeng.li on 21-3-22.
 */
class ResList {
    //    fun containsRes(value: String): Boolean {
//        for (i in resSet) {
//            if (i.value == value) return true
//        }
//        return false
//    }
//
//    var conflict = false
    fun isConflict() = resSet.size > 1
    val resSet = mutableSetOf<ResourceInfo>()
}