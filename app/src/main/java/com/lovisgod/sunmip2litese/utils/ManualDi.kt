package com.lovisgod.sunmip2litese.utils

import com.lovisgod.sunmip2litese.data.EmvDataKeyManager
import com.lovisgod.sunmip2litese.data.DataSource
import com.lovisgod.sunmip2litese.data.EmvPaymentHandler
import com.lovisgod.sunmip2litese.domain.SunmiLiteRepository
import com.lovisgod.sunmip2litese.domain.use_cases.*


class SunmiLiteContainer {

    val emvDataKeyManager = EmvDataKeyManager()
    val emvPaymentHandler = EmvPaymentHandler()
    private val dataSource = DataSource(emvDataKeyManager, emvPaymentHandler)
    private val repository = SunmiLiteRepository(dataSource)


    fun getUseCases(): AllUseCases {
        println("this got called")
         return AllUseCases(
             downloadAid = DownloadAidUseCase(repository),
             downloadCapkUseCase = DownloadCapkUseCase(repository),
             setTerminalConfigUseCase = SetTerminalConfigUseCase(repository),
             setPinKeyUseCase = SetPinKeyUseCase(repository),
             emvPayUseCase = EmvPayUseCase(repository),
             printBitMapUseCase = PrintBitMapUseCase(repository),
             writePinkeyUseCase = WritePinkeyUseCase(repository),
             loadMasterKeyUseCase = LoadMasterKeyUseCase(repository)
         )
    }

//    fun initializeEmvDataManager()

}

