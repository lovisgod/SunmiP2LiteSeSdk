package com.lovisgod.sunmip2litese.utils.models.iccData

import com.lovisgod.sunmip2litese.data.EmvPaymentHandler

enum class ICCData(val tagName: String, val tag: String, val min: Int, val max: Int) {
    // emv request and sensitive data
    AUTHORIZATION_REQUEST("Authorization Request", "9F26", 8, 8,),
    CRYPTOGRAM_INFO_DATA("Cryptogram Information Data", "9F27",  1, 1),
    ISSUER_APP_DATA("Issuer Application Data", "9F10", 0, 32),
    UNPREDICTABLE_NUMBER("Unpredictable Number", "9F37", 4, 4),
    APPLICATION_TRANSACTION_COUNTER("App Transaction Counter", "9F36", 2, 2),
    TERMINAL_VERIFICATION_RESULT("Terminal Verification Result", "95", 5, 5),
    TRANSACTION_DATE("Transaction Date", "9A", 3, 3),
    TRANSACTION_TYPE("Transaction Type", "9C", 1, 1),
    TRANSACTION_AMOUNT("Transaction Amount", "9F02", 6, 6),
    TRANSACTION_CURRENCY_CODE("Currency Code", "5F2A", 2, 2),
    APPLICATION_INTERCHANGE_PROFILE("App Interchange Profile", "82", 2, 2),
    TERMINAL_COUNTRY_CODE("Terminal Country Code", "9F1A", 2, 2),
    CARD_HOLDER_VERIFICATION_RESULT("CVM Results", "9F34", 3, 3),
    TERMINAL_CAPABILITIES("Terminal Capabilities", "9F33", 3, 3),
    TERMINAL_TYPE("Terminal Type", "9F35", 1, 1),
    INTERFACE_DEVICE_SERIAL_NUMBER("IDSN", "9F1E", 8, 8),
    DEDICATED_FILE_NAME("Dedicated File Name", "84", 5, 16),
    APP_VERSION_NUMBER("App Version Number", "9F09", 2, 2),
    ANOTHER_AMOUNT("Amount", "9F03", 6, 6),
    APP_PAN_SEQUENCE_NUMBER("App PAN Sequence Number", "5F34", 1, 1),
    CARD_HOLDER_NAME("Cardholder Name", "5F20", 2, 26),
    TRACK_2_DATA("Track 2 Data", "57", 2, 37),


    // Issuer response
    ISSUER_AUTHENTICATION("Issuer Authentication", "91", 0, 16),
    ISSUER_SCRIPT1("Issuer script 1", "71", 0, 128),
    ISSUER_SCRIPT2("Issuer script 1", "72", 0, 128)

}

internal val REQUEST_TAGS = listOf(
    ICCData.AUTHORIZATION_REQUEST,
    ICCData.CRYPTOGRAM_INFO_DATA,
    ICCData.ISSUER_APP_DATA,
    ICCData.UNPREDICTABLE_NUMBER,
    ICCData.APPLICATION_TRANSACTION_COUNTER,
    ICCData.TERMINAL_VERIFICATION_RESULT,
    ICCData.TRANSACTION_DATE,
    ICCData.TRANSACTION_TYPE,
    ICCData.TRANSACTION_AMOUNT,
    ICCData.TRANSACTION_CURRENCY_CODE,
    ICCData.APPLICATION_INTERCHANGE_PROFILE,
    ICCData.TERMINAL_COUNTRY_CODE,
    ICCData.CARD_HOLDER_VERIFICATION_RESULT,
    ICCData.TERMINAL_CAPABILITIES,
    ICCData.TERMINAL_TYPE,
    ICCData.INTERFACE_DEVICE_SERIAL_NUMBER,
    ICCData.DEDICATED_FILE_NAME,
    ICCData.APP_VERSION_NUMBER,
    ICCData.ANOTHER_AMOUNT,
    ICCData.APP_PAN_SEQUENCE_NUMBER,
    ICCData.CARD_HOLDER_NAME,
    ICCData.TRACK_2_DATA
)


internal fun getIccData(tlvDataString: String): RequestIccData {
    // set icc data using specified icc tags
    return RequestIccData(
        TRANSACTION_AMOUNT = ICCData.TRANSACTION_AMOUNT.getTlv(tlvDataString) ?: "",
        ANOTHER_AMOUNT = ICCData.ANOTHER_AMOUNT.getTlv(tlvDataString) ?: "000000000000",
        APPLICATION_INTERCHANGE_PROFILE = ICCData.APPLICATION_INTERCHANGE_PROFILE.getTlv(tlvDataString) ?: "",
        APPLICATION_TRANSACTION_COUNTER = ICCData.APPLICATION_TRANSACTION_COUNTER.getTlv(tlvDataString) ?: "",
        CRYPTOGRAM_INFO_DATA = ICCData.CRYPTOGRAM_INFO_DATA.getTlv(tlvDataString) ?: "",
        AUTHORIZATION_REQUEST = ICCData.AUTHORIZATION_REQUEST.getTlv(tlvDataString) ?: "",
        CARD_HOLDER_VERIFICATION_RESULT = ICCData.CARD_HOLDER_VERIFICATION_RESULT.getTlv(tlvDataString) ?: "",
        ISSUER_APP_DATA = ICCData.ISSUER_APP_DATA.getTlv(tlvDataString) ?: "",
        TERMINAL_VERIFICATION_RESULT = ICCData.TERMINAL_VERIFICATION_RESULT.getTlv(tlvDataString) ?: "",
        // remove leading zero in currency and country codes
        TRANSACTION_CURRENCY_CODE = ICCData.TERMINAL_COUNTRY_CODE.getTlv(tlvDataString).substring(1) ?: "",
        TERMINAL_COUNTRY_CODE = ICCData.TERMINAL_COUNTRY_CODE.getTlv(tlvDataString).substring(1) ?: "",

        TERMINAL_TYPE = ICCData.TERMINAL_TYPE.getTlv(tlvDataString) ?: "",
        TERMINAL_CAPABILITIES = ICCData.TERMINAL_CAPABILITIES.getTlv(tlvDataString) ?: "",
        TRANSACTION_DATE = ICCData.TRANSACTION_DATE.getTlv(tlvDataString) ?: "",
        TRANSACTION_TYPE = ICCData.TRANSACTION_TYPE.getTlv(tlvDataString) ?: "",
        UNPREDICTABLE_NUMBER = ICCData.UNPREDICTABLE_NUMBER.getTlv(tlvDataString) ?: "",
        DEDICATED_FILE_NAME = ICCData.DEDICATED_FILE_NAME.getTlv(tlvDataString) ?: "").apply {


        INTERFACE_DEVICE_SERIAL_NUMBER = ICCData.INTERFACE_DEVICE_SERIAL_NUMBER.getTlv(tlvDataString) ?: ""
        APP_VERSION_NUMBER = ICCData.APP_VERSION_NUMBER.getTlv(tlvDataString) ?: ""
        CARD_HOLDER_NAME = ""?: ""
        APP_PAN_SEQUENCE_NUMBER = ICCData.APP_PAN_SEQUENCE_NUMBER.getTlv(tlvDataString) ?: ""
        TRACK_2_DATA = ICCData.TRACK_2_DATA.getTlv(tlvDataString) ?: ""
    }


}


fun ICCData.getTlv(tlvDataString: String): String{
//    val tlvParser = BerTlvParser()
//    val tlvs = tlvParser.parse(data)
//    var tlv = tlvs.find(BerTag("${this.tag}"))
    var tlv = EmvPaymentHandler().getTlvData(this.tag, tlvDataString)
    if (tlv.isNotEmpty()) {
        println("tlv data:::: ${tlv}")
       return tlv
    } else {
        return ""
    }
}


