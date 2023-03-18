package com.lovisgod.sunmip2litese.domain

import EmvRequest
import com.lovisgod.sunmip2litese.utils.models.ConfigInfoHelper
import com.lovisgod.sunmip2litese.utils.models.iccData.RequestIccData
import com.lovisgod.sunmip2litese.utils.models.pay.OnlineRespEntity
import com.lovisgod.sunmip2litese.utils.networkHandler.KimonoClient
import com.lovisgod.sunmip2litese.utils.networkHandler.models.TerminalInformationRequest
import com.lovisgod.sunmip2litese.utils.networkHandler.models.TokenRequestModel
import com.pixplicity.easyprefs.library.Prefs
import okhttp3.MediaType
import okhttp3.RequestBody

class SampleNetworkRepository {

    val client = KimonoClient().getClient()

    suspend fun getToken(){
        val terminalInfo = ConfigInfoHelper.readTerminalInfo()
        var terminalInformationRequest =
            TerminalInformationRequest().fromTerminalInfo("Sunmip2LiteSe", terminalInfo, false)
        var tokenRequestModel = TokenRequestModel()
        tokenRequestModel.terminalInformation = terminalInformationRequest
        val response  = client.getISWToken(tokenRequestModel).run()
        if (response.isSuccessful) {
          val token = response.body()?.token
          println("token:::::: $token")
          Prefs.putString("TOKEN", token)
        }
    }

     fun makeTransactionOnline(icc: RequestIccData, amount: Int): OnlineRespEntity {
        val terminalInfo = ConfigInfoHelper.readTerminalInfo()
        val token = Prefs.getString("TOKEN", "")
        val requestBody = EmvRequest.getCashout(terminalInfo, icc, amount)
        println("info :::: requestbody::: $requestBody")
        val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)
        return try {
            val response = client.makeCashout(body, "Bearer $token").run()

            return if (response.isSuccessful) {
                println("info::::::: ${response.body()?.responseCode}")
                val respEntitiy = OnlineRespEntity()
                    .apply {
                        respCode = response.body()?.responseCode
                        iccData = icc.iccAsString
                    }
                respEntitiy
            } else {
                val errorRespEntitiy = OnlineRespEntity()
                    .apply {
                        respCode = "0XXX0"
                    }
                errorRespEntitiy
            }
        }catch (e:Exception) {
            println(e.printStackTrace())
            val errorRespEntitiy = OnlineRespEntity()
                .apply {
                    respCode = "0XXX0"
                }
            errorRespEntitiy
        }
    }
}