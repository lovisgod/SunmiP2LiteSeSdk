package com.lovisgod.sunmip2litese.ui.uiState

interface PrintingState {
    fun onSuccess(code: Int = 1025)
    fun onError(error: Int)
}