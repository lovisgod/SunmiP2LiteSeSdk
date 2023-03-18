package com.lovisgod.sunmip2litese.domain.use_cases

import android.graphics.Bitmap
import com.lovisgod.sunmip2litese.domain.SunmiLiteRepository
import com.lovisgod.sunmip2litese.ui.uiState.PrintingState

class PrintBitMapUseCase(private val repository: SunmiLiteRepository) {

    suspend operator fun invoke(bitmap: Bitmap, printingState: PrintingState){
        return repository.printBitMap(bitmap, printingState)
    }
}
