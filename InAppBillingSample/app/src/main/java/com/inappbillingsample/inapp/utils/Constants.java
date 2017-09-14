package com.inappbillingsample.inapp.utils;

public interface Constants {

    public final int BILLING_VER = 3;
    public final int REQUEST_CODE = 111;

    public final String RESPONSE_CODE = "RESPONSE_CODE";

    public final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public final String INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";

    public final String TYPE_INAPP = "inapp";
    public final String TYPE_SUBS = "subs";

    public final String KEY_RSA = "RSA";
    public final String KEY_SHA1WITHRSA = "sha1withrsa";

    public final String BUY_INTENT = "BUY_INTENT";
    public final String ITEM_ID_LIST = "ITEM_ID_LIST";
    public final String DETAILS_LIST = "DETAILS_LIST";

    public final String INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public final String INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    public final String KEY_PAYLOAD = "testRefKey";

    public final String PROD_PURCHASE = "android.test.purchased";
    public final String PROD_CANCELED = "android.test.canceled";
    public final String PROD_REFUNDED = "android.test.refunded";
    public final String PROD_ITEM_UNAVAILABLE = "android.test.item_unavailable";
    int REQUEST_CODE_IN_APP_CONSUME_ITEM = 1000;
    int REQUEST_CODE_IN_APP_NON_CONSUME_ITEM = 2000;


}
