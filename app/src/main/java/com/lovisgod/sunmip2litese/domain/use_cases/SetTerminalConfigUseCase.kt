package com.lovisgod.sunmip2litese.domain.use_cases

import com.lovisgod.sunmip2litese.domain.SunmiLiteRepository
import com.lovisgod.sunmip2litese.utils.SunmiLiteException
import com.lovisgod.sunmip2litese.utils.models.TerminalInfo

class SetTerminalConfigUseCase(private val repository: SunmiLiteRepository) {
    @Throws(SunmiLiteException::class)
    suspend operator fun invoke(terminalInfo: TerminalInfo): Int{
        return repository.setTerminalConfig(terminalInfo)
    }
}