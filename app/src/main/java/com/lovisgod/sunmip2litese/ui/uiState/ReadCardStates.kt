package com.lovisgod.sunmip2litese.ui.uiState

import com.lovisgod.sunmip2litese.utils.models.iccData.RequestIccData
import com.lovisgod.sunmip2litese.utils.models.pay.OnlineRespEntity
import com.lovisgod.sunmip2litese.utils.models.pay.TransactionResultCode

interface ReadCardStates {

    fun onInsertCard()
    fun onRemoveCard()
    fun onPinInput()
    fun getCardScheme(cardType: CardScheme) {
        println(cardType)
    }
    fun sendTransactionOnline(emvData: RequestIccData): OnlineRespEntity
    fun onEmvProcessing(message: String = "Please wait while we read card")
    fun onEmvProcessed(data: Any?, code: TransactionResultCode)
    fun onCardFind(cardType: CardType) {
        println(println("${cardType.name} is found"))
    }
}

enum class CardType {
    CONTACT, CONTACTLESS, MAG, ERROR, NONE
}

enum class CardScheme {
    MASTER, VERVE, VISA, AMEX, DEFAULT, UNION, JCB, RUPAY
}