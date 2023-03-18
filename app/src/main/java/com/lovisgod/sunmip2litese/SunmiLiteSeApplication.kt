package com.lovisgod.sunmip2litese

import android.app.Service
import android.content.*
import android.os.IBinder
import com.lovisgod.sunmip2litese.utils.SunmiLiteContainer
import com.pixplicity.easyprefs.library.Prefs
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import com.sunmi.pay.hardware.aidlv2.etc.ETCOptV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2
import com.sunmi.pay.hardware.aidlv2.print.PrinterOptV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2
import com.sunmi.pay.hardware.aidlv2.tax.TaxOptV2
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import sunmi.paylib.SunmiPayKernel
import sunmi.paylib.SunmiPayKernel.ConnectCallback

object SunmiLiteSeApplication {
    private val TAG = "sunmip2liteApplication"
//    private var context: Application? = null
    var basicOptV2
            : BasicOptV2? = null
    var readCardOptV2
            : ReadCardOptV2? = null
    var pinPadOptV2
            : PinPadOptV2? = null
    var securityOptV2
            : SecurityOptV2? = null
    var emvOptV2: EMVOptV2? = null
    var taxOptV2
            : TaxOptV2? = null
    var etcOptV2
            : ETCOptV2? = null
    var printerOptV2
            : PrinterOptV2? = null
//    var testOptV2
//            : TestOptV2? = null
//    var devCertManagerV2 //设备证书操作模块
//            : DevCertManagerV2? = null
//    var sunmiPrinterService: SunmiPrinterService? = null
//    var scanInterface: IScanInterface? = null
    var connectPaySDK = false


    object container {
        var sunmiLiteContainer = SunmiLiteContainer()
        var horizonPayUseCase = sunmiLiteContainer.getUseCases()
    }

    fun isConnectPaySDK(): Boolean {
        return connectPaySDK
    }


    fun onCreate(context: Context) {

        println("this is called first")

        Prefs.Builder()
            .setContext(context)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName("com.lovisgod.sunmip2litese")
            .setUseDefaultSharedPreference(true)
            .build()

//        BaseUtils.init(this.context!!)
//        bindDriverService(context)
        println("this is context ::: ${context.applicationContext}")
        bindDriverService(context)
    }

//    fun bindDriverService(context: Context) {
//        println("this is called third")
//    }


    /** bind PaySDK service  */
    fun bindDriverService(context: Context) {
        val payKernel = SunmiPayKernel.getInstance()
        payKernel.initPaySDK(context, object : ConnectCallback {
            override fun onConnectPaySDK() {
                println("onConnectSDK....")
                this@SunmiLiteSeApplication.emvOptV2 = payKernel.mEMVOptV2
                basicOptV2 = payKernel.mBasicOptV2
                pinPadOptV2 = payKernel.mPinPadOptV2
                readCardOptV2 = payKernel.mReadCardOptV2
                securityOptV2 = payKernel.mSecurityOptV2
                taxOptV2 = payKernel.mTaxOptV2
                etcOptV2 = payKernel.mETCOptV2
                printerOptV2 = payKernel.mPrinterOptV2
//                testOptV2 = payKernel.mTestOptV2
//                devCertManagerV2 = payKernel.mDevCertManagerV2
                connectPaySDK = true

                println("emvoptionxxxx::::::: ${this@SunmiLiteSeApplication.emvOptV2}")
            }

            override fun onDisconnectPaySDK() {
                println("onDisconnectPaySDK...")
//                connectPaySDK = false
//                emvOptV2 = null
//                basicOptV2 = null
//                pinPadOptV2 = null
//                readCardOptV2 = null
//                securityOptV2 = null
//                taxOptV2 = null
//                etcOptV2 = null
//                printerOptV2 = null
//                devCertManagerV2 = null
//                Utility.showToast(R.string.connect_fail)
            }
        })
    }


//    /** bind printer service  */
//    private fun bindPrintService() {
//        try {
//            InnerPrinterManager.getInstance().bindService(this, object : InnerPrinterCallback() {
//                override fun onConnected(service: SunmiPrinterService) {
//                    sunmiPrinterService = service
//                }
//
//                override fun onDisconnected() {
//                    sunmiPrinterService = null
//                }
//            })
//        } catch (e: InnerPrinterException) {
//            e.printStackTrace()
//        }
//    }

//    /** bind scanner service  */
//    fun bindScannerService() {
//        val intent = Intent()
//        intent.setPackage("com.sunmi.scanner")
//        intent.action = "com.sunmi.scanner.IScanInterface"
//        bindService(intent, object : ServiceConnection {
//            override fun onServiceConnected(name: ComponentName, service: IBinder) {
//                scanInterface = IScanInterface.Stub.asInterface(service)
//            }
//
//            override fun onServiceDisconnected(name: ComponentName) {
//                scanInterface = null
//            }
//        }, Service.BIND_AUTO_CREATE)
//    }

}