package com.lovisgod.sunmip2litese.utils;

import android.os.RemoteException;
import android.util.Log;

import com.lovisgod.sunmip2litese.data.EmvPaymentHandler;
import com.lovisgod.sunmip2litese.utils.AidHelpers.EmvUtil;
import com.lovisgod.sunmip2litese.utils.models.pay.CreditCard;
import com.lovisgod.sunmip2litese.utils.tlvHelper.EmvTags;

import java.util.HashMap;
import java.util.Map;

public class CardUtil {
    private static final String TAG = "CardUtil";

    public static String getCardTypFromAid(String aid) {
        if (aid == null || aid.length() < 10) {
            return "";
        }
        Log.d(TAG, "getCardTypFromAid: " + aid.length());
        if (cardType.containsKey(aid.substring(0, 10))) {
            return cardType.get(aid.substring(0, 10));
        }
        return "";
    }

    private static Map<String, String> cardType = new HashMap<String, String>();

    static {
        cardType.put("A000000004", "MASTER");
        cardType.put("A000000003", "VISA");
        cardType.put("A000000025", "AMEX");
        cardType.put("A000000065", "JCB");
        cardType.put("A000000152", "DISCOVER");
        cardType.put("A000000324", "DISCOVER");
        cardType.put("A000000333", "PBOC");
        cardType.put("A000000524", "RUPAY");
    }


    public static String getCurrencyName(String code) {
        try {
            if (cardCurrency.containsKey(code.substring(0, 3))) {
                return cardCurrency.get(code.substring(0, 3));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return "UnKnown";
    }

    private static Map<String, String> cardCurrency = new HashMap<String, String>();

    static {
        cardCurrency.put("156", "RMB");
        cardCurrency.put("344", "HKD");
        cardCurrency.put("446", "MOP");
        cardCurrency.put("458", "MYR");
        cardCurrency.put("702", "SGD");
        cardCurrency.put("978", "EUR");
        cardCurrency.put("036", "AUD");
        cardCurrency.put("764", "THB");
        cardCurrency.put("784", "AED");
        cardCurrency.put("392", "JPY");
        cardCurrency.put("360", "IDR");
        cardCurrency.put("840", "USD");
        cardCurrency.put("566", "NGN");
        cardCurrency.put("356", "INR");
        cardCurrency.put("364", "IRR");
        cardCurrency.put("400", "JOD");
        cardCurrency.put("116", "KHR");
        cardCurrency.put("480", "MUR");
        cardCurrency.put("938", "SDG");
    }

    public static CreditCard getEmvCardInfo(String tlvDataString) {
        CreditCard creditCard = new CreditCard();
        EmvPaymentHandler handler = new EmvPaymentHandler();
//        creditCard.setCardReadMode(mCardReadMode);
        try {
            String cardsn = handler.getTlvData(EmvTags.EMV_TAG_IC_PANSN, tlvDataString);
            System.out.println("cardsn:::::" + cardsn);
            if (cardsn != null && !cardsn.isEmpty()) {
                creditCard.setCardSequenceNumber(cardsn);
            }

            String track2 = handler.getTlvData(EmvTags.EMV_TAG_IC_TRACK2DATA, tlvDataString);
            System.out.println("track2:::::" + track2);
            if (track2 == null || track2.isEmpty()) {
                track2 = handler.getTlvData(EmvTags.M_TAG_IC_9F6B, tlvDataString);
            }
            if (track2 != null && track2.length() > 20) {
                if (track2.endsWith("F") || track2.endsWith("f")) {
                    track2 = track2.substring(0, track2.length() - 1);
                }
                String formatTrack2 = track2.toUpperCase().replace('=', 'D');

                int idx = formatTrack2.indexOf('D');
                String expDate = track2.substring(idx + 1, idx + 5);

                creditCard.setExpireDate(expDate);

                String pan = track2.substring(0, idx);
                creditCard.setCardNumber(pan);
                CreditCard.EmvData emvData = new CreditCard.EmvData("", formatTrack2, handler.getTlvDataString());
                creditCard.setEmvData(emvData);
            }

//            String name = EmvUtil.readCardHolder();
            creditCard.setHolderName("");
            return creditCard;
        } catch (Exception e) {
            e.printStackTrace();
            return new CreditCard();
        }
    }

}
