package com.lovisgod.sunmip2litese.utils.networkHandler

import com.lovisgod.sunmip2litese.utils.Constants
import com.lovisgod.sunmip2litese.utils.networkHandler.simplecalladapter.Simple
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface AuthInterface
{
    @Headers("Content-Type: application/xml")
    @GET(Constants.KIMONO_MERCHANT_DETAILS_END_POINT_AUTO)
    fun getMerchantDetails(@Path("terminalSerialNo") terminalSerialNo: String):
            Simple<Any>

}