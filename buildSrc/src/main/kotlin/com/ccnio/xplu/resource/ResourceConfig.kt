package com.ccnio.xplu.resource

/**
 * Created by jianfeng.li on 21-3-3.
 */
open class ResourceConfig {
    var scanConflict: Boolean = false
    var interruptWhenConflict = false
    var refactorSrcModule: String = ""
    var refactorDestModule: String = ""

}