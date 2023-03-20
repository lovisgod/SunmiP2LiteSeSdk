package com.lovisgod.sunmip2litese.domain

import android.content.Context
import android.graphics.Bitmap
import com.lovisgod.sunmip2litese.data.DataSource
import com.lovisgod.sunmip2litese.ui.uiState.PrintingState
import com.lovisgod.sunmip2litese.ui.uiState.ReadCardStates
import com.lovisgod.sunmip2litese.utils.models.TerminalInfo

class SunmiLiteRepository(val dataSource: DataSource) {
    suspend fun downloadAid() = dataSource.downloadAid()
    suspend fun dowloadCapk() = dataSource.downloadCapk()
    suspend fun writePinKey(keyIndex: Int, keyData: String) = dataSource.writePinKey(keyIndex, keyData)
    suspend fun loadMasterKey(keyData: String) = dataSource.loadMasterKey(keyData)
    suspend fun setTerminalConfig(terminalInfo: TerminalInfo) = dataSource.setEmvParameter(terminalInfo)
    suspend fun setPinKey(
        isDukpt: Boolean = true, key: String = "", ksn: String = "") = dataSource.setPinKey(isDukpt, key, ksn)
    suspend fun pay(amount: Long, readCardStates: ReadCardStates, context: Context) = dataSource.pay(amount, readCardStates, context)
    suspend fun printBitMap(bitmap: Bitmap, printingState: PrintingState) = dataSource.printBitMap(bitmap, printingState)
    suspend fun getDeviceSerial() = dataSource.getDeviceSerial()
}