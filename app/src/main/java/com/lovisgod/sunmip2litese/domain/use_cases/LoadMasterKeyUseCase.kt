package com.lovisgod.sunmip2litese.domain.use_cases

import com.lovisgod.sunmip2litese.domain.SunmiLiteRepository
import com.lovisgod.sunmip2litese.utils.SunmiLiteException

class LoadMasterKeyUseCase(val repository: SunmiLiteRepository) {

    @Throws(SunmiLiteException::class)
    suspend operator fun invoke(key: String): Int? {
        return repository.loadMasterKey(key)
    }
}