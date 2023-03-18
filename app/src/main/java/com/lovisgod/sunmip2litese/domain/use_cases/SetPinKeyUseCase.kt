package com.lovisgod.sunmip2litese.domain.use_cases

import com.lovisgod.sunmip2litese.domain.SunmiLiteRepository
import com.lovisgod.sunmip2litese.utils.SunmiLiteException

class SetPinKeyUseCase(val repository: SunmiLiteRepository) {

    @Throws(SunmiLiteException::class)
    suspend operator fun invoke(isDukpt: Boolean = true, key: String, ksn: String): Int{
        return repository.setPinKey(isDukpt, key, ksn)
    }
}