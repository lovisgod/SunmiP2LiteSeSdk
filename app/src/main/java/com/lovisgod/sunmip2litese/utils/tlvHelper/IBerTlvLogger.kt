package com.lovisgod.sunmip2litese.utils.tlvHelper

interface IBerTlvLogger {
    val isDebugEnabled: Boolean
    fun debug(aFormat: String?, vararg args: Any?)
}