package com.lovisgod.sunmip2litese.data

import android.os.RemoteException
import com.lovisgod.sunmip2litese.SunmiLiteSeApplication
import com.lovisgod.sunmip2litese.utils.*
import com.lovisgod.sunmip2litese.utils.AidHelpers.EmvUtil
import com.lovisgod.sunmip2litese.utils.models.ConfigInfoHelper.saveTerminalInfo
import com.lovisgod.sunmip2litese.utils.models.TerminalInfo
import com.pixplicity.easyprefs.library.Prefs
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2

class EmvDataKeyManager {

    private var isSupportPinPad = false

    fun initialize() {

    }

     fun downloadAID() {
            try {
            EmvUtil.initAid()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

     fun clearAID() {
        try {
            val ret = EmvUtil.clearAid()
            println("clear aid ::: $ret")
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun downloadCapk() {
        try {
            EmvUtil.initCapk()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun clearCapk() {
        try {
            val ret = EmvUtil.clearCapk()
            println("clear capk ::: $ret")
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun setEmvConfig(terminalInfo: TerminalInfo) {
        val terminalMap:Map<String?, String?> = mapOf(
            "countryCode" to terminalInfo.terminalCountryCode,
            "capability" to terminalInfo.terminalCapabilities,
            "currencyCode" to terminalInfo.transCurrencyCode,
            "currencyExponent" to "02"
        )
        EmvUtil.setTerminalParam(terminalMap)

        saveTerminalInfo(terminalInfo)

    }


    private fun setDukpt(key: String, ksn:String): Int {
        Prefs.putString("KSN", StringManipulator.dropLastCharacter(ksn))
        val mKeyType: Int = AidlConstants.Security.KEY_TYPE_DUPKT_IPEK
        val mKeyAlgType: Int = AidlConstants.Security.KEY_ALG_TYPE_3DES
        val ksnBytes: ByteArray = ByteUtil.hexStr2Bytes(ksn)
        val keyValue: ByteArray = ByteUtil.hexStr2Bytes(key)
        val checkValue: ByteArray = ByteUtil.hexStr2Bytes("82E13665B4624DF5")
        val keyIndex = 1
        val ret  = SunmiLiteSeApplication.securityOptV2?.saveKeyDukpt(mKeyType, keyValue, checkValue, ksnBytes, mKeyAlgType,keyIndex )
        println("dukpt:::::::: $ret")
        EmvUtil.initKey()
        return ret!!
    }


    fun writePinKey(keyIndex: Int, keyData: String): Int? {

        // save PIK
        val databyte = ByteUtil.hexStr2Bytes(keyData)
        val cvByte = ByteUtil.hexStr2Bytes("82E13665B4624DF5")
        val result = SunmiLiteSeApplication.securityOptV2?.saveCiphertextKey(
            AidlConstants.Security.KEY_TYPE_PIK,
            databyte,
            cvByte,
            11,
            AidlConstants.Security.KEY_ALG_TYPE_3DES,
            12
        )
        println("save PIK result:$result")
        return result
    }

    fun loadMasterKey(masterkey: String) {
        // Save TMK
        val databyte = ByteUtil.hexStr2Bytes(masterkey)
        val cvByte = ByteUtil.hexStr2Bytes("82E13665B4624DF5")
        val result = SunmiLiteSeApplication.securityOptV2?.saveCiphertextKey(
            AidlConstants.Security.KEY_TYPE_TMK,
            databyte,
            cvByte,
            10,
            AidlConstants.Security.KEY_ALG_TYPE_3DES,
            11
        )
        println("save TMK result:$result")
    }

    fun setPinKey(isDukpt: Boolean, key: String = "", ksn: String = ""): Int {
        if (isDukpt) return  setDukpt(key, ksn) else return IswHpCodes.NOT_SUPPORTED // implement pin key later
    }
}