import com.lovisgod.sunmip2litese.utils.Constants.CLSS_POS_DATA_CODE
import com.lovisgod.sunmip2litese.utils.Constants.POS_ENTRY_MODE
import com.lovisgod.sunmip2litese.utils.Constants.getKeyLabl
import com.lovisgod.sunmip2litese.utils.Constants.getNextStan
import com.lovisgod.sunmip2litese.utils.DateUtils
import com.lovisgod.sunmip2litese.utils.DeviceUtils
import com.lovisgod.sunmip2litese.utils.StringManipulator
import com.lovisgod.sunmip2litese.utils.models.TerminalInfo
import com.lovisgod.sunmip2litese.utils.models.iccData.RequestIccData
import com.lovisgod.sunmip2litese.utils.networkHandler.models.AccountType
import java.util.*

object EmvRequest {

     fun getCashout(terminalInfo: TerminalInfo, icc: RequestIccData, amount: Int) : String {
         val serialId = DeviceUtils.getDeviceSerialSunmi()
//         var dedicatedFileTag = ""
         var pinData = ""
//         dedicatedFileTag=  """"<DedicatedFileName>${icc.DEDICATED_FILE_NAME}</DedicatedFileName>"""
         if(!icc.EMV_CARD_PIN_DATA.CardPinBlock.isEmpty())
             pinData= """<pinData><ksnd>605</ksnd><pinType>Dukpt</pinType><ksn>${StringManipulator.dropFirstCharacter(icc.EMV_CARD_PIN_DATA.ksn)}</ksn><pinBlock>${icc.EMV_CARD_PIN_DATA.CardPinBlock}</pinBlock></pinData>"""
         val requestBody = """<transferRequest>
                                <terminalInformation>
                                     <batteryInformation>-1</batteryInformation>
                                     <currencyCode>566</currencyCode>
                                     <languageInfo>EN</languageInfo>
                                     <merchantId>${terminalInfo.merchantId}</merchantId>
                                     <merhcantLocation>${terminalInfo.merchantAddress1}</merhcantLocation>
                                     <posConditionCode>00</posConditionCode>
                                     <posDataCode>${CLSS_POS_DATA_CODE}</posDataCode> 
                                     <posEntryMode>${POS_ENTRY_MODE}</posEntryMode>
                                     <posGeoCode>00234000000000566</posGeoCode>
                                     <printerStatus>1</printerStatus>
                                     <terminalId>${terminalInfo.terminalCode}</terminalId>
                                     <terminalType>SUNMI</terminalType>
                                     <transmissionDate>${DateUtils.universalDateFormat.format(Date())}</transmissionDate>
                                     <uniqueId>$serialId</uniqueId>
                                </terminalInformation>
                                <cardData>
                                    <cardSequenceNumber>${icc.APP_PAN_SEQUENCE_NUMBER}</cardSequenceNumber>
                                    <emvData>
                                        <AmountAuthorized>${icc.TRANSACTION_AMOUNT}</AmountAuthorized>
                                        <AmountOther>${icc.ANOTHER_AMOUNT}</AmountOther>
                                        <ApplicationInterchangeProfile>${icc.APPLICATION_INTERCHANGE_PROFILE}</ApplicationInterchangeProfile>
                                        <atc>${icc.APPLICATION_TRANSACTION_COUNTER}</atc>
                                        <Cryptogram>${icc.AUTHORIZATION_REQUEST}</Cryptogram>
                                        <CryptogramInformationData>${icc.CRYPTOGRAM_INFO_DATA}</CryptogramInformationData>
                                        <CvmResults>${icc.CARD_HOLDER_VERIFICATION_RESULT}</CvmResults>
                                        <iad>${icc.ISSUER_APP_DATA}</iad>
                                        <TransactionCurrencyCode>${icc.TRANSACTION_CURRENCY_CODE}</TransactionCurrencyCode>
                                        <TerminalVerificationResult>${icc.TERMINAL_VERIFICATION_RESULT}</TerminalVerificationResult>
                                        <TerminalCountryCode>${icc.TERMINAL_COUNTRY_CODE}</TerminalCountryCode>
                                        <TerminalType>${icc.TERMINAL_TYPE}</TerminalType>
                                        <TerminalCapabilities>${icc.TERMINAL_CAPABILITIES}</TerminalCapabilities>
                                        <TransactionDate>${icc.TRANSACTION_DATE}</TransactionDate>
                                        <TransactionType>${icc.TRANSACTION_TYPE}</TransactionType>
                                        <UnpredictableNumber>${icc.UNPREDICTABLE_NUMBER}</UnpredictableNumber>
                                        <DedicatedFileName>${icc.DEDICATED_FILE_NAME}</DedicatedFileName>
                                    </emvData>
                                    <track2>
                                        <pan>${icc.EMC_CARD_?.cardNumber}</pan>
                                        <expiryMonth>${icc.EMC_CARD_?.expireDate?.takeLast(2)}</expiryMonth>
                                        <expiryYear>${icc.EMC_CARD_?.expireDate?.take(2)}</expiryYear>
                                        <track2>${icc.EMC_CARD_?.emvData?.track2}</track2>
                                    </track2>
                                </cardData>
                                <originalTransmissionDateTime>${DateUtils.universalDateFormat.format(Date())}</originalTransmissionDateTime>
                                <stan>${getNextStan()}</stan>
                                <fromAccount>${AccountType.Default.name}</fromAccount>
                                <toAccount></toAccount>
                                <minorAmount>${amount}</minorAmount>
                                <receivingInstitutionId>627629</receivingInstitutionId>
                                $pinData
                                <keyLabel>${getKeyLabl(false)}</keyLabel>
                                <destinationAccountNumber>2089430464</destinationAccountNumber>
                                 <extendedTransactionType>6103</extendedTransactionType>
</transferRequest>"""
         return requestBody
     }
 }