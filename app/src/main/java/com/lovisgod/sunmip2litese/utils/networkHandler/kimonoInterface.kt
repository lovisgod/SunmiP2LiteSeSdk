package com.lovisgod.sunmip2litese.utils.networkHandler

import PurchaseResponxe
import com.lovisgod.sunmip2litese.utils.Constants
import com.lovisgod.sunmip2litese.utils.networkHandler.models.TokenConfigResponse
import com.lovisgod.sunmip2litese.utils.networkHandler.models.TokenRequestModel
import com.lovisgod.sunmip2litese.utils.networkHandler.simplecalladapter.Simple
import okhttp3.RequestBody
import retrofit2.http.*

interface kimonoInterface {

    @POST(Constants.ISW_TOKEN_URL)
    fun getISWToken( @Body request: TokenRequestModel):
            Simple<TokenConfigResponse>


    @Headers("Content-Type: application/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST(Constants.KIMONO_END_POINT)
    fun makeCashout(@Body request: RequestBody, @Header("Authorization") token: String ):
            Simple<PurchaseResponxe>
}