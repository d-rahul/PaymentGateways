package com.inappbillingsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.inappbillingsample.inapp.utils.AvailableItemDetails;
import com.inappbillingsample.inapp.utils.Constants;
import com.inappbillingsample.inapp.utils.InAppHelper;
import com.inappbillingsample.inapp.utils.ProductDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Constants{
    InAppHelper inAppHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInApp();
    }


    public void initInApp(){
        inAppHelper = new InAppHelper(this);

        /**
         * to initialise IInAppBillingService and bind service to our activity
         */
        inAppHelper.initInAppBilling();
    }

    public void onConsumeInApp(View view){
        //multiple purchasing of same item
        inAppHelper.purchaseItems(PROD_PURCHASE, TYPE_INAPP, REQUEST_CODE_IN_APP_CONSUME_ITEM, KEY_PAYLOAD, BILLING_VER);
    }
    public void onNonConsumeInApp(View view){
        inAppHelper.purchaseItems(PROD_PURCHASE, TYPE_INAPP, REQUEST_CODE_IN_APP_NON_CONSUME_ITEM, KEY_PAYLOAD, BILLING_VER);
    }
    public void onSubscription(View view){
        inAppHelper.purchaseItems(PROD_PURCHASE, TYPE_INAPP, REQUEST_CODE_IN_APP_NON_CONSUME_ITEM, KEY_PAYLOAD, BILLING_VER);
    }


    /**
     * You must send a consumption request for the in-app product before Google Play makes it available for purchase again.
     *
     * @param purchaseToken is part of the data returned in the INAPP_PURCHASE_DATA
     * @param ver           inpp billing version using in application
     * @return true if successfully consumed
     */
    private boolean consumingPurchase(String purchaseToken, int ver) {
        return inAppHelper.consumingPurchase(purchaseToken, ver);
    }

    /**
     * return true if billing api version support for the requested billing API version,
     *
     * @param version       the inpp billing version using in application
     * @param typOfPurchase type of purchase either "inapp" or "subs"
     * @return true if the version will support
     */
    private boolean checkInppBillingVersion(int version, String typOfPurchase) {
        return inAppHelper.checkInppBillingVersion(version, typOfPurchase);
    }

    /**
     * Returns true if the data is correctly signed.
     *
     * @param base64PublicKey public key
     * @param purchasedData   data.getStringExtra("INAPP_PURCHASE_DATA");
     * @param resultSignature data.getStringExtra("INAPP_DATA_SIGNATURE");
     * @return true if verified successfully
     */
    private boolean verifyPurchase(String base64PublicKey, String purchasedData, String resultSignature) {
        return inAppHelper.verifyPurchase(base64PublicKey, purchasedData, resultSignature);
    }

    /**
     * use to get available items to purchase
     *
     * @param ver  billing version that we want to use in application
     * @param type type of purchase either "inapp" or "subs"
     */
    private void getItemsForPurchase(int ver, String type) {

        ArrayList<AvailableItemDetails> responseList = inAppHelper.getItemsForPurchase(ver, type);

    }


    /**
     * To start a purchase request from your app
     *
     * @param productId        product id of item that you want to purchase
     * @param type             indicates type of purchase either "inapp" or "subs"
     * @param requestCode      response that you want in onActivityResult
     * @param developerPayLoad refference key that you will get in INAPP_PURCHASE_DATA as developerPayload in onActivityResult
     * @param ver              billing version that we want to use in application
     */
    private void purchaseItems(String productId, String type, int requestCode, String developerPayLoad, int ver) {

        inAppHelper.purchaseItems(productId, type, requestCode, developerPayLoad, ver);

    }

    /**
     * To retrieve information about purchases made by a user from your app
     *
     * @param purchaseType indicates type of purchase either "inapp" or "subs"
     * @param ver          billing version that we want to use in application
     */
    private void queryForPurchasedItemes(String purchaseType, int ver) {
        ArrayList<ProductDetail> productDetailsList = inAppHelper.queryForPurchasedItemes(null, purchaseType, null, ver);
    }

    /**
     * This method is used to upgrade or downgrade a subscription purchase.
     * it will giver result call back in onActivityResult
     *
     * @param ver           This method was added with version 5 of the in-app billing API
     * @param oldProdIDList list of already-purchased SKUs
     * @param newProdID     product id that we want to replace with
     * @param payload       security key
     */
    private void replacePurchasedItemWithOther(int ver, List<String> oldProdIDList, String newProdID, String payload, int requestCode) {

        inAppHelper.replacePurchasedItemWithOther(ver, oldProdIDList, newProdID, payload, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {

        if ((requestCode == REQUEST_CODE_IN_APP_CONSUME_ITEM||requestCode == REQUEST_CODE_IN_APP_NON_CONSUME_ITEM) && result == RESULT_OK) {

            int responseCode = data.getIntExtra(RESPONSE_CODE, 0);
            inAppHelper.checkGooglePlayResponseCode(responseCode);

            /**
             * purchaseData json string formate :
             * {"packageName":"com.inappbillingdemo",
             * "orderId":"transactionId.android.test.purchased",
             * "productId":"android.test.purchased",
             * "developerPayload":"testRefKey","purchaseTime":0,"purchaseState":0,
             * "purchaseToken":"inapp:com.inappbillingdemo:android.test.purchased"}
             * */

            String purchaseData = data.getStringExtra(INAPP_PURCHASE_DATA);
            String purchaseSignature = data.getStringExtra(INAPP_DATA_SIGNATURE);

            if (purchaseData != null) {

                Log.i("purchased data : ", purchaseData);
                Log.i("signature : ", purchaseSignature);

                try {
                    JSONObject purchaseObj = new JSONObject(purchaseData);
                    String item = purchaseObj.optString("productId");
                    String reffId = purchaseObj.optString("developerPayload");
                    String packageName = purchaseObj.optString("packageName");
                    String orderId = purchaseObj.optString("orderId");
                    String purchaseToken = purchaseObj.optString("purchaseToken");

                    if (reffId.compareTo(KEY_PAYLOAD) == 0) {

                        Log.i("Purchase status : ", "--------------");
                        Log.i("Purchase status :", "Successfully " + item + " purchased");
                        Log.i("package name : ", packageName);
                        Log.i("orderId : ", orderId);
                        Log.i("purchaseToken : ", purchaseToken);

                        inAppHelper.showToast("purchased item : " + item);
                    }

                    if(requestCode == REQUEST_CODE_IN_APP_CONSUME_ITEM){
                        consumingPurchase(purchaseToken, BILLING_VER);
                    }
                } catch (JSONException e) {

                    Log.i("Purchase status :", "Failed to parse purchase data");
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, result, data);
        }

    }

}
