package com.lovisgod.sunmip2litese.utils.models.iccData

import com.lovisgod.sunmip2litese.utils.KeysUtilx


data class EmvPinData (
    var ksn : String = KeysUtilx.getIpekKsn(false).ksn,
    var CardPinBlock: String = ""
)