package com.lovisgod.sunmip2litese.data

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.RemoteException
import android.text.TextUtils
import com.isw.pinencrypter.Converter.GetPinBlock
import com.lovisgod.sunmip2litese.SunmiLiteSeApplication
import com.lovisgod.sunmip2litese.ui.uiState.CardScheme
import com.lovisgod.sunmip2litese.ui.uiState.CardType
import com.lovisgod.sunmip2litese.ui.uiState.PrintingState
import com.lovisgod.sunmip2litese.ui.uiState.ReadCardStates
import com.lovisgod.sunmip2litese.utils.*
import com.lovisgod.sunmip2litese.utils.AidHelpers.EmvUtil.Constant
import com.lovisgod.sunmip2litese.utils.models.iccData.EmvPinData
import com.lovisgod.sunmip2litese.utils.models.iccData.ICCData
import com.lovisgod.sunmip2litese.utils.models.iccData.RequestIccData
import com.lovisgod.sunmip2litese.utils.models.iccData.getIccData
import com.lovisgod.sunmip2litese.utils.models.pay.TransactionResultCode
import com.lovisgod.sunmip2litese.utils.tlvHelper.TLV
import com.lovisgod.sunmip2litese.utils.tlvHelper.TLVUtil
import com.pixplicity.easyprefs.library.Prefs
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidl.bean.CardInfo
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2
import java.util.*
import java.util.regex.Pattern

class EmvPaymentHandler {
    private var readCardStates: ReadCardStates?  = null
    private var printingState: PrintingState? = null
    private var emvOptV2: EMVOptV2? = null
    private var pinPad: PinPadOptV2? = null
    private var readCardOptV2: ReadCardOptV2? = null
    private var basicOptV2 : BasicOptV2? = null
    private var cardType = 0
    var amount: Long = 0L
    var pinTypex = 0
    var cardNoX = ""
    var isOnlinePin = true
    private var cardScheme: CardScheme = CardScheme.DEFAULT
    private var carpin = ""
    private var ksn  = ""
    private var iccData: RequestIccData? = null


    fun initialize(context: Context) {

    }

    fun pay (amount: Long, readCardStates: ReadCardStates, context: Context) {
        this.readCardStates = readCardStates
        this.amount = amount
        emvOptV2 = SunmiLiteSeApplication.emvOptV2
        pinPad = SunmiLiteSeApplication.pinPadOptV2
        readCardOptV2 = SunmiLiteSeApplication.readCardOptV2
        basicOptV2 = SunmiLiteSeApplication.basicOptV2

        // init the env process
        println("emvoptv2 ::::$emvOptV2")
        var ret  = emvOptV2?.initEmvProcess()
        println("init emv process::: $ret")
        // init essential tlv data for some kernel types// might remove this later though
        initEmvTlvData()
        // check the card type
        checkCard()
        // start transaction process
    }


    /**
     * Set tlv essential tlv data
     */
    private fun initEmvTlvData() {
        println("got here for for init data")
        try {
            // set PayPass(MasterCard) tlv data
            val tagsPayPass = arrayOf(
                "DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C",
                "DF8123", "DF8124", "DF8125", "DF8126",
                "DF811B", "DF811D", "DF8122", "DF8120", "DF8121"
            )
            val valuesPayPass = arrayOf(
                "E0", "F8", "F8", "E8", "00", "00",
                "000000000000", "000000100000", "999999999999", "000000100000",
                "30", "02", "0000000000", "000000000000", "000000000000"
            )
            emvOptV2?.setTlvList(
                AidlConstants.EMV.TLVOpCode.OP_PAYPASS,
                tagsPayPass,
                valuesPayPass
            )

            // set AMEX(AmericanExpress) tlv data
            val tagsAE =
                arrayOf("9F6D", "9F6E", "9F33", "9F35", "DF8168", "DF8167", "DF8169", "DF8170")
            val valuesAE = arrayOf("C0", "D8E00000", "E0E888", "22", "00", "00", "00", "60")
            emvOptV2?.setTlvList(AidlConstants.EMV.TLVOpCode.OP_AE, tagsAE, valuesAE)
            val tagsJCB = arrayOf("9F53", "DF8161")
            val valuesJCB = arrayOf("708000", "7F00")
            emvOptV2?.setTlvList(AidlConstants.EMV.TLVOpCode.OP_JCB, tagsJCB, valuesJCB)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /**
     * Start check card
     */
    private fun checkCard() {
        println("got here for check card")
        try {
            val cardType: Int =
                AidlConstants.CardType.NFC.getValue() or AidlConstants.CardType.IC.getValue()
            println("card type: $cardType")
            println("readcardOptv2:::: ${readCardOptV2}")
            readCardOptV2?.checkCard(cardType, checkCardCallback, 60)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Check card callback
     */
    private val checkCardCallback: CheckCardCallbackV2 = object : CheckCardCallbackV2Wrapper() {
        @Throws(RemoteException::class)
        override
        fun findMagCard(bundle: Bundle) {
            this@EmvPaymentHandler.readCardStates?.onCardFind(CardType.MAG)
            LogUtil.e(Constant.TAG, "findMagCard:$bundle")
        }

        @Throws(RemoteException::class)
        override fun findICCard(atr: String) {
            println("ic card found")
            LogUtil.e(Constant.TAG, "findICCard:$atr")
            //IC card Beep buzzer when check card success
            basicOptV2?.buzzerOnDevice(1, 2750, 200, 0)
            cardType = AidlConstants.CardType.IC.getValue()
            this@EmvPaymentHandler.readCardStates?.onCardFind(CardType.CONTACT)
            transactProcess()
        }

        @Throws(RemoteException::class)
        override fun findRFCard(uuid: String) {
            println("nfc card found")
            cardType = AidlConstants.CardType.NFC.getValue()
            this@EmvPaymentHandler.readCardStates?.onCardFind(CardType.CONTACTLESS)
            LogUtil.e(Constant.TAG, "findRFCard:$uuid")
            transactProcess()
        }

        @Throws(RemoteException::class)
        override fun onError(code: Int, message: String) {
            println("error found :::: $code ::: message ::::$message")
            val error = "onError:$message -- $code"
            this@EmvPaymentHandler.readCardStates?.onCardFind(CardType.ERROR)
            LogUtil.e(Constant.TAG, error)
        }
    }

    /**
     * Start emv transact process
     */
    private fun transactProcess() {
        println("amount is :::::: ${this.amount.toString()}")
        LogUtil.e(Constant.TAG, "transactProcess")
        try {
            val bundle = Bundle()
            bundle.putString("amount", this.amount.toString())
            bundle.putString("transType", "00")
            //flowType:0x01-emv standard, 0x04：NFC-Speedup
            //Note:(1) flowType=0x04 only valid for QPBOC,PayPass,PayWave contactless transaction
            //     (2) set fowType=0x04, only EMVListenerV2.onRequestShowPinPad(),
            //         EMVListenerV2.onCardDataExchangeComplete() and EMVListenerV2.onTransResult() may will be called.
            if (cardType == AidlConstants.CardType.NFC.getValue()) {
                bundle.putInt("flowType", AidlConstants.EMV.FlowType.TYPE_NFC_SPEEDUP)
            } else {
                bundle.putInt("flowType", AidlConstants.EMV.FlowType.TYPE_EMV_STANDARD)
            }
            bundle.putInt("cardType", cardType)
            //            bundle.putBoolean("preProcessCompleted", false);
//            bundle.putInt("emvAuthLevel", 0);
            emvOptV2?.transactProcessEx(bundle, mEMVListener)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Notify emv process the Application select result
     *
     * @param selectIndex the index of selected App, start from 0
     */
    private fun importAppSelect(selectIndex: Int) {
        LogUtil.e(Constant.TAG, "importAppSelect selectIndex:$selectIndex")
        try {
            emvOptV2?.importAppSelect(selectIndex)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Notify emv process the final Application select result
     *
     * @param status 0:success, other value:failed
     */
    private fun importFinalAppSelectStatus(status: Int) {
        try {
            LogUtil.e(Constant.TAG, "importFinalAppSelectStatus status:$status")
            emvOptV2?.importAppFinalSelectStatus(status)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /**
     * Notify emv process the card number confirm status
     *
     * @param status 0:success, other value:failed
     */
    private fun importCardNoStatus(status: Int) {
        LogUtil.e(Constant.TAG, "importCardNoStatus status:$status")
        try {
            emvOptV2?.importCardNoStatus(status)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Notify emv process the certification verify status
     *
     * @param status 0:success, other value:failed
     */
    private fun importCertStatus(status: Int) {
        LogUtil.e(Constant.TAG, "importCertStatus status:$status")
        try {
            emvOptV2?.importCertStatus(status)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Notify emv process the PIN input result
     *
     * @param inputResult 0:success,1:input PIN canceled,2:input PIN skipped,3:PINPAD problem,4:input PIN timeout
     */
    private fun importPinInputStatus(inputResult: Int) {
        LogUtil.e(Constant.TAG, "importPinInputStatus:$inputResult")
        try {
            emvOptV2?.importPinInputStatus(pinTypex, inputResult)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun tryAgain() {
        try {
        checkCard()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Import online process result data(eg: field 55 ) to emv process.
     * if no date to import, set param tags and values as empty array
     *
     * @param status 0:online approval, 1:online denial, 2:online failed
     */
    private fun importOnlineProcessStatus(stat: Int) {
        LogUtil.e(Constant.TAG, "importOnlineProcessStatus status:$stat")
        try {
            val tags = arrayOf("71", "72", "91", "8A", "89")
            val values = arrayOf("", "", "", "", "")
            val out = ByteArray(1024)
            val len: Int? = emvOptV2?.importOnlineProcStatus(stat, tags, values, out)
            if (len != null) {
                if (len < 0) {
                    LogUtil.e(Constant.TAG, "importOnlineProcessStatus error,code:$len")
                } else {
                    val bytes = Arrays.copyOf(out, len)
                    val hexStr = ByteUtil.bytes2HexStr(bytes)
                    LogUtil.e(Constant.TAG, "importOnlineProcessStatus outData:$hexStr")
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * EMV process callback
     */
    private val mEMVListener: EMVListenerV2 = object : EMVListenerV2.Stub() {
        /**
         * Notify client to do multi App selection, this method may called when card have more than one Application
         * <br></br> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param appNameList   The App list for selection
         * @param isFirstSelect is first time selection
         */
        @Throws(RemoteException::class)
        override fun onWaitAppSelect(appNameList: List<EMVCandidateV2>, isFirstSelect: Boolean) {
            LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:$isFirstSelect")
            importAppSelect(0)
        }

        /**
         * Notify client the final selected Application
         * <br></br> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param tag9F06Value The final selected Application id
         */
        @Throws(RemoteException::class)
        override fun onAppFinalSelect(tag9F06Value: String) {
            LogUtil.e(Constant.TAG, "onAppFinalSelect tag9F06Value:$tag9F06Value")
            if (tag9F06Value != null && tag9F06Value.length > 0) {
                val isUnionPay = tag9F06Value.startsWith("A000000333")
                val isVisa = tag9F06Value.startsWith("A000000003")
                val isMaster = (tag9F06Value.startsWith("A000000004")
                        || tag9F06Value.startsWith("A000000005"))
                val isAmericanExpress = tag9F06Value.startsWith("A000000025")
                val isJCB = tag9F06Value.startsWith("A000000065")
                val isRupay = tag9F06Value.startsWith("A000000524")
                val isPure = (tag9F06Value.startsWith("D999999999")
                        || tag9F06Value.startsWith("D888888888")
                        || tag9F06Value.startsWith("D777777777")
                        || tag9F06Value.startsWith("D666666666")
                        || tag9F06Value.startsWith("A000000615")
                        || tag9F06Value.startsWith("A000000371"))
                var paymentType = "unknown"
                if (isUnionPay) {
                    paymentType = "UnionPay"
                    this@EmvPaymentHandler.readCardStates?.getCardScheme(CardScheme.DEFAULT)
                    cardScheme = CardScheme.DEFAULT
                } else if (isVisa) {
                    paymentType = "Visa"
                    this@EmvPaymentHandler.readCardStates?.getCardScheme(CardScheme.VISA)
                    cardScheme = CardScheme.VISA
                } else if (isMaster) {
                    paymentType = "MasterCard"
                    this@EmvPaymentHandler.readCardStates?.getCardScheme(CardScheme.MASTER)
                    cardScheme = CardScheme.MASTER
                } else if (isAmericanExpress) {
                    paymentType = "AmericanExpress"
                    this@EmvPaymentHandler.readCardStates?.getCardScheme(CardScheme.AMEX)
                    cardScheme = CardScheme.AMEX
                } else if (isJCB) {
                    paymentType = "JCB"
                    this@EmvPaymentHandler.readCardStates?.getCardScheme(CardScheme.JCB)
                    cardScheme = CardScheme.JCB
                } else if (isRupay) {
                    paymentType = "Rupay"
                    this@EmvPaymentHandler.readCardStates?.getCardScheme(CardScheme.RUPAY)
                    cardScheme = CardScheme.RUPAY
                } else if (isPure) {
                    paymentType = "Pure"
                    this@EmvPaymentHandler.readCardStates?.getCardScheme(CardScheme.VERVE)
                    cardScheme = CardScheme.VERVE
                }
                LogUtil.e(Constant.TAG, "detect $paymentType card")
            }
            importFinalAppSelectStatus(0)
        }

        /**
         * Notify client to confirm card number
         * <br></br> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param cardNo The card number
         */
        @Throws(RemoteException::class)
        override fun onConfirmCardNo(cardNo: String) {
            cardNoX = cardNo
            LogUtil.e(Constant.TAG, "onConfirmCardNo cardNo:$cardNo")
            importCardNoStatus(0);
        }

        /**
         * Notify client to input PIN
         *
         * @param pinType    The PIN type, 0-online PIN，1-offline PIN
         * @param remainTime The the remain retry times of offline PIN, for online PIN, this param
         * value is always -1, and if this is the first time to input PIN, value
         * is -1 too.
         */
        @Throws(RemoteException::class)
        override fun onRequestShowPinPad(pinType: Int, remainTime: Int) {
            LogUtil.e(Constant.TAG, "onRequestShowPinPad pinType:$pinType remainTime:$remainTime")
            pinTypex = pinType
            isOnlinePin = pinType == 0
            if (cardNoX.isNullOrEmpty()) {
                getCardNo().let {
                    cardNoX = it!!
                }
            }
           initPinPad()
        }

        /**
         * Notify  client to do signature
         */
        @Throws(RemoteException::class)
        override fun onRequestSignature() {

            LogUtil.e(Constant.TAG, "onRequestSignature")
        }

        /**
         * Notify client to do certificate verification
         *
         * @param certType The certificate type, refer to AidlConstants.CertType
         * @param certInfo The certificate info
         */
        @Throws(RemoteException::class)
        override fun onCertVerify(certType: Int, certInfo: String) {
            LogUtil.e(Constant.TAG, "onCertVerify certType:$certType certInfo:$certInfo")
//            mCertInfo = certInfo
            importCertStatus(0)
        }

        /**
         * Notify client to do online process
         */
        @Throws(RemoteException::class)
        override fun onOnlineProc() {
            val tlvDataString = getTlvDataString()
            LogUtil.e(Constant.TAG, "onOnlineProcess")
            iccData = getIccData(tlvDataString)
            println("iccdata:::::: ${iccData?.APPLICATION_INTERCHANGE_PROFILE}")
            var creditCard = CardUtil.getEmvCardInfo(tlvDataString)
            creditCard.ksnData = ksn
            creditCard.pin = carpin
            iccData?.apply {
                EMC_CARD_ = creditCard
                iccAsString = tlvDataString
                CARD_HOLDER_NAME = creditCard?.holderName.toString()
                EMV_CARD_PIN_DATA = if (isOnlinePin) EmvPinData(creditCard.ksnData, creditCard.pin) else EmvPinData()
            }
           var response  = this@EmvPaymentHandler.readCardStates?.sendTransactionOnline(iccData!!)
            println("trans online res :: ${response?.respCode}")
            if (response?.respCode?.toString()?.isNotEmpty()!! && response.respCode?.toString() == "00" )  {
                println("called for suc")
                importOnlineProcessStatus(0)
            } else {
                println("called for fail")
                importOnlineProcessStatus(-1)
            }
            carpin = ""
            ksn = ""
        }

        /**
         * Notify client EMV kernel and card data exchange finished, client can remove card
         */
        @Throws(RemoteException::class)
        override fun onCardDataExchangeComplete() {

            LogUtil.e(Constant.TAG, "onCardDataExchangeComplete")
            if (cardType == AidlConstants.CardType.NFC.getValue()) {
                //NFC card Beep buzzer to notify remove card
                basicOptV2?.buzzerOnDevice(1, 2750, 200, 0)
            }
        }

        /**
         * Notify client EMV process ended
         *
         * @param code The transaction result code, 0-success, 1-offline approval, 2-offline denial,
         * 4-try again, other value-error code
         * @param desc The corresponding message of this code
         */
        @Throws(RemoteException::class)
        override fun onTransResult(code: Int, desc: String?) {
            try {
                LogUtil.e(Constant.TAG, "onTransResult code:$code desc:$desc")
                LogUtil.e(
                    Constant.TAG,
                    "***************************************************************"
                )
                LogUtil.e(
                    Constant.TAG,
                    "****************************End Process************************"
                )
                LogUtil.e(
                    Constant.TAG,
                    "***************************************************************"
                )
                if (code == 0) {
                    // TRANS SUC
                    checkAndRemoveCard()
                    val isOfflineApproved = if(isOnlinePin) TransactionResultCode.APPROVED_BY_ONLINE else TransactionResultCode.APPROVED_BY_OFFLINE
                    this@EmvPaymentHandler.readCardStates?.onEmvProcessed(iccData, isOfflineApproved)
                } else if (code == 4) {
                    // TRY AGAIN
                    tryAgain()
                } else {
                    // TRANS FAIL
                    checkAndRemoveCard()
                    val isOfflineNotApproved = if(isOnlinePin) TransactionResultCode.DECLINED_BY_ONLINE else TransactionResultCode.DECLINED_BY_ONLINE
                    this@EmvPaymentHandler.readCardStates?.onEmvProcessed(iccData, isOfflineNotApproved)
                }
                // clear ICc data
                iccData = null
            } catch (e: RemoteException) {
                e.printStackTrace()
                iccData = null
            }
        }

        /**
         * Notify client the confirmation code verified(See phone)
         */
        @Throws(RemoteException::class)
         override fun onConfirmationCodeVerified() {

            LogUtil.e(Constant.TAG, "onConfirmationCodeVerified")
            val outData = ByteArray(512)
            val len: Int? =
                emvOptV2?.getTlv(AidlConstants.EMV.TLVOpCode.OP_PAYPASS, "DF8129", outData)
            if (len != null) {
                if (len > 0) {
                    val data = ByteArray(len)
                    System.arraycopy(outData, 0, data, 0, len)
                    val hexStr: String = ByteUtil.bytes2HexStr(data)
                    LogUtil.e(Constant.TAG, "DF8129: $hexStr")
                }
            }
            // card off
            SunmiLiteSeApplication.readCardOptV2?.cardOff(cardType)
            emvOptV2?.initEmvProcess()
            checkCard()
        }

        /**
         * Notify client to exchange data
         * <br></br> This method only used for Russia MIR
         *
         * @param cardNo The card number
         */
        @Throws(RemoteException::class)
        override fun onRequestDataExchange(cardNo: String) {
            LogUtil.e(Constant.TAG, "onRequestDataExchange,cardNo:$cardNo")
            emvOptV2?.importDataExchangeStatus(0)
        }

        @Throws(RemoteException::class)
        override fun onTermRiskManagement() {
            LogUtil.e(Constant.TAG, "onTermRiskManagement")
            emvOptV2?.importTermRiskManagementStatus(0)
        }

        @Throws(RemoteException::class)
        override fun onPreFirstGenAC() {
            LogUtil.e(Constant.TAG, "onPreFirstGenAC")
            emvOptV2?.importPreFirstGenACStatus(0)
        }

        @Throws(RemoteException::class)
        override fun onDataStorageProc(
            containerID: Array<String>,
            containerContent: Array<String>
        ) {
            LogUtil.e(Constant.TAG, "onDataStorageProc,")
            //此回调为Dpas2.0专用
            //根据需求配置tag及values
            val tags = arrayOfNulls<String>(0)
            val values = arrayOfNulls<String>(0)
            emvOptV2?.importDataStorage(tags, values)
        }
 }





    // PIN PAD AND LISTERNER

    /**
     * Start show PinPad
     */
    private fun initPinPad() {
        LogUtil.e(Constant.TAG, "initPinPad")
        try {
            val pinPadConfig = PinPadConfigV2()
            pinPadConfig.pinPadType = 0
            pinPadConfig.pinType = pinTypex
            pinPadConfig.isOrderNumKey = false
            val panBytes: ByteArray = cardNoX.substring(cardNoX.length - 13, cardNoX.length - 1)
                .toByteArray(charset("US-ASCII"))
            pinPadConfig.pan = panBytes
            pinPadConfig.timeout = 60 * 1000 // input password timeout
            pinPadConfig.pinKeyIndex = 12 // pik index
            pinPadConfig.maxInput = 12
            pinPadConfig.minInput = 0
            pinPadConfig.keySystem = 0
            pinPadConfig.algorithmType = 0
            pinPad?.initPinPad(pinPadConfig, mPinPadListener)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Input pin callback
     */
    private val mPinPadListener: PinPadListenerV2 = object : PinPadListenerV2.Stub() {
        override fun onPinLength(len: Int) {
            LogUtil.e(Constant.TAG, "onPinLength:$len")

        }

        override fun onConfirm(i: Int, pinBlock: ByteArray) {
            if (pinBlock != null) {
                val hexStr: String = ByteUtil.bytes2HexStr(pinBlock)
                LogUtil.e(Constant.TAG, "onConfirm pin block:$hexStr")

//            mPinPad.dukptKsnIncrease(PinpadConst.DukptKeyIndex.DUKPT_KEY_INDEX_0);
                val ksnCount: String = Constants.getNextKsnCounter()
                val ksnString = Prefs.getString("KSN", "") + ksnCount

                try {
                    val pin = TripleDES.decrypt(
                        cardNoX,
                        HexUtil.bytesToHexString(pinBlock),
                        "11111111111111111111111111111111"
                    )
                    println("pin is :::: $pin")
                    val pinBlock = GetPinBlock(
                        KeysUtilx.getIpekKsn(false).ipek,
                        ksnString,
                        pin,
                        cardNoX
                    )
                    DeviceUtils.showText("info::::::: $pinBlock")
                    carpin = pinBlock
                    ksn = ksnString
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                importPinInputStatus(0)
            } else {
                importPinInputStatus(2)
            }
        }

        override fun onCancel() {
            importPinInputStatus(1)
            LogUtil.e(Constant.TAG, "onCancel")
        }

        override fun onError(code: Int) {
            importPinInputStatus(3)
            LogUtil.e(Constant.TAG, "onError:$code")
            val msg = AidlErrorCodeV2.valueOf(code).msg
        }
    }


    fun handlePrinting(bitmap: Bitmap, printingState: PrintingState){
        this.printingState = printingState
        return try {
            this.printingState!!.onSuccess(0)
        } catch (e: RemoteException) {
            e.printStackTrace()
            this.printingState!!.onError(-11)
        }
    }



     fun stopEmvProcess() {
        println("this is called called called")
        try {
            cancelCheckCard()
        } catch (e: RemoteException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

    }


    /** getCard number  */
    fun getCardNo(): String? {
        LogUtil.e(Constant.TAG, "getCardNo")
        try {
            val tagList = arrayOf("57", "5A")
            val outData = ByteArray(256)
            val len: Int? =
                emvOptV2?.getTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tagList, outData)
            if (len != null) {
                if (len <= 0) {
                    LogUtil.e(Constant.TAG, "getCardNo error,code:$len")
                    return ""
                }
            }
            val bytes = len?.let { Arrays.copyOf(outData, it) }
            val tlvMap: Map<String, TLV> = TLVUtil.buildTLVMap(bytes)
            if (!TextUtils.isEmpty(Objects.requireNonNull<CharSequence?>(tlvMap["57"]?.value))) {
                val tlv57: TLV? = tlvMap["57"]
                val cardInfo: CardInfo? =
                    tlv57?.getValue()?.let { parseTrack2(it) }
                return cardInfo?.cardNo
            }
            if (!TextUtils.isEmpty(Objects.requireNonNull<CharSequence?>(tlvMap["5A"]?.value))) {
                return Objects.requireNonNull<CharSequence?>(tlvMap["5A"]?.value).toString()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * Parse track2 data
     */
    fun parseTrack2(track2: String): CardInfo {
        LogUtil.e(Constant.TAG, "track2:$track2")
        val track_2: String = stringFilter(track2)
        var index = track_2.indexOf("=")
        if (index == -1) {
            index = track_2.indexOf("D")
        }
        val cardInfo = CardInfo()
        if (index == -1) {
            return cardInfo
        }
        var cardNumber = ""
        if (track_2.length > index) {
            cardNumber = track_2.substring(0, index)
        }
        var expiryDate = ""
        if (track_2.length > index + 5) {
            expiryDate = track_2.substring(index + 1, index + 5)
        }
        var serviceCode = ""
        if (track_2.length > index + 8) {
            serviceCode = track_2.substring(index + 5, index + 8)
        }
        LogUtil.e(
            Constant.TAG,
            "cardNumber:$cardNumber expireDate:$expiryDate serviceCode:$serviceCode"
        )
        cardInfo.cardNo = cardNumber
        cardInfo.expireDate = expiryDate
        cardInfo.serviceCode = serviceCode
        return cardInfo
    }

    /**
     * remove characters not number,=,D
     */
    fun stringFilter(str: String?): String {
        val regEx = "[^0-9=D]"
        val p = Pattern.compile(regEx)
        val matcher = p.matcher(str)
        return matcher.replaceAll("").trim { it <= ' ' }
    }

    /**
     * Read we interested tlv data
     */
     fun getTlvDataString() : String{
        return try {
            val tagList = arrayOf(
                "DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26",
                "9F27", "DF31", "5A", "57", "5F24", "9F1A", "9F33", "9F35", "9F40",
                "9F03", "9F10", "9F37", "9C", "9A", "9F02", "5F2A", "82", "9F34", "9F1E",
                "84", "4F", "9F66", "9F6C", "9F09", "9F41", "9F63", "5F20", "9F12", "50"
            )
            val outData = ByteArray(2048)
            val map: MutableMap<String, TLV> = TreeMap()
            val tlvOpCode: Int
            tlvOpCode = if (AidlConstants.CardType.NFC.getValue() == cardType) {
                if (cardScheme == CardScheme.MASTER) {
                    AidlConstants.EMV.TLVOpCode.OP_PAYPASS
                } else if (cardScheme == CardScheme.VISA) {
                    AidlConstants.EMV.TLVOpCode.OP_PAYWAVE
                } else {
                    AidlConstants.EMV.TLVOpCode.OP_NORMAL
                }
            } else {
                AidlConstants.EMV.TLVOpCode.OP_NORMAL
            }
            var len: Int? = emvOptV2?.getTlvList(tlvOpCode, tagList, outData)
            if (len != null) {
                if (len > 0) {
                    val bytes = Arrays.copyOf(outData, len)
                    val hexStr = ByteUtil.bytes2HexStr(bytes)
                    val tlvMap = TLVUtil.buildTLVMap(hexStr)
                    map.putAll(tlvMap)
                }
            }

            // payPassTags
            val payPassTags = arrayOf(
                "DF811E", "DF812C", "DF8118", "DF8119", "DF811F", "DF8117", "DF8124",
                "DF8125", "9F6D", "DF811B", "9F53", "DF810C", "9F1D", "DF8130", "DF812D",
                "DF811C", "DF811D", "9F7C"
            )
            len =
                emvOptV2?.getTlvList(AidlConstants.EMV.TLVOpCode.OP_PAYPASS, payPassTags, outData)
            if (len != null) {
                if (len > 0) {
                    val bytes = Arrays.copyOf(outData, len)
                    val hexStr = ByteUtil.bytes2HexStr(bytes)
                    val tlvMap = TLVUtil.buildTLVMap(hexStr)
                    map.putAll(tlvMap)
                }
            }
            val sb = StringBuilder()
            val keySet: Set<String> = map.keys
            for (key in keySet) {
                val tlv = map[key]
                if (tlv != null) {
                   val value = TLVUtil.revertToHexStr(tlv)
                    sb.append(value)
                }
            }
            println("icc data is ::::: ${sb.toString()}")
            sb.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ""
        }
    }


    /** Check and notify remove card  */
    private fun checkAndRemoveCard() {
        try {
            val status: Int? = readCardOptV2?.getCardExistStatus(cardType)
            if (status != null) {
                if (status < 0) {
                    LogUtil.e(Constant.TAG, "getCardExistStatus error, code:$status")
                    return
                }
            }
            if (status == AidlConstants.CardExistStatus.CARD_ABSENT) {

            } else if (status == AidlConstants.CardExistStatus.CARD_PRESENT) {
                SunmiLiteSeApplication.basicOptV2?.buzzerOnDevice(1, 2750, 200, 0)
                this.readCardStates?.onRemoveCard()
//                checkAndRemoveCard()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelCheckCard() {
        try {
            readCardOptV2?.cardOff(AidlConstants.CardType.NFC.getValue())
            readCardOptV2?.cancelCheckCard()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun getTlvData(tag: String, tlvString: String): String {
         return try {
             val map: MutableMap<String, TLV> = TreeMap()
             if (tlvString.isNotEmpty()) {
                 val tlvMap = TLVUtil.buildTLVMap(tlvString)
                 map.putAll(tlvMap)
             }
             val sb = StringBuilder()
             val tlv = map[tag]
             if (tlv != null) {
                 sb.append(tlv.value)
             }

             println("icc data is ::::: ${sb.toString()}")
             sb.toString()

         } catch (e: java.lang.Exception) {
             e.printStackTrace()
             ""
         }

    }
}