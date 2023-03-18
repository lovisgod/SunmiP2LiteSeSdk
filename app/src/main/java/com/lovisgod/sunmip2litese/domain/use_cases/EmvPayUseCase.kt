package com.lovisgod.sunmip2litese.domain.use_cases

import android.content.Context
import com.lovisgod.sunmip2litese.domain.SunmiLiteRepository
import com.lovisgod.sunmip2litese.ui.uiState.ReadCardStates
import com.lovisgod.sunmip2litese.utils.SunmiLiteException

class EmvPayUseCase (private val repository: SunmiLiteRepository) {

    @Throws(SunmiLiteException::class)
    suspend operator fun invoke(amount:Long, readCardStates: ReadCardStates, context: Context){
        return repository.pay(amount, readCardStates, context)
    }
}