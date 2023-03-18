package com.lovisgod.sunmip2litese.domain.use_cases

import com.lovisgod.sunmip2litese.domain.SunmiLiteRepository
import com.lovisgod.sunmip2litese.utils.SunmiLiteException

class WritePinkeyUseCase(val repository: SunmiLiteRepository) {

    @Throws(SunmiLiteException::class)
    suspend operator fun invoke(key: String, keyIndex: Int): Int? {
        return repository.writePinKey(keyIndex, key)
    }
}