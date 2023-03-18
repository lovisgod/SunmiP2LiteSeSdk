package com.lovisgod.sunmip2litese.data

import android.content.Context
import android.graphics.Bitmap
import com.lovisgod.sunmip2litese.ui.uiState.PrintingState
import com.lovisgod.sunmip2litese.ui.uiState.ReadCardStates
import com.lovisgod.sunmip2litese.utils.IswHpCodes
import com.lovisgod.sunmip2litese.utils.models.TerminalInfo

class DataSource(val emvDataKeyManager: EmvDataKeyManager, val emvPaymentHandler: EmvPaymentHandler) {

    suspend fun downloadAid(): Int {
        return try {
            emvDataKeyManager.clearAID()
            emvDataKeyManager.downloadAID()
            IswHpCodes.SUCCESS
        } catch (e: Exception) {
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }

    suspend fun downloadCapk(): Int {
        return try {
            emvDataKeyManager.clearCapk()
            emvDataKeyManager.downloadCapk()
            IswHpCodes.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }

    suspend fun setEmvParameter(terminalInfo: TerminalInfo): Int {
        return try {
            emvDataKeyManager.setEmvConfig(terminalInfo)
            IswHpCodes.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }

    suspend fun setPinKey(isDukpt: Boolean = true, key: String = "", ksn: String = ""): Int {
        println("ipek::: $key::::: ksn::::$ksn")
        return try {
            emvDataKeyManager.setPinKey(isDukpt, key, ksn)
        } catch (e:Exception) {
            e.printStackTrace()
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }

    suspend fun writePinKey(keyIndex: Int, keyData: String) : Int? {
        return try {
            emvDataKeyManager.writePinKey(keyIndex, keyData)
        } catch (e: Exception) {
            e.printStackTrace()
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }

    suspend fun loadMasterKey(keyData: String) : Int? {
        return try {
            emvDataKeyManager.loadMasterKey(keyData)
            return  IswHpCodes.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }

    suspend fun pay(amount: Long, readCardStates: ReadCardStates, context: Context) {
        try {
            emvPaymentHandler.pay(amount, readCardStates, context)
        } catch (e: Exception) {
            e.printStackTrace()
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }

    suspend fun printBitMap(bitmap: Bitmap, printingState: PrintingState) {
        try {
//            emvPaymentHandler.handlePrinting(bitmap, printingState)
        } catch (e: Exception) {
            e.printStackTrace()
            IswHpCodes.GENERAL_EMV_EXCEPTION
        }
    }
}