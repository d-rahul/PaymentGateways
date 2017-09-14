package com.inappbillingsample.inapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class InAppHelper implements DialogInterface.OnClickListener, Constants {


    IInAppBillingService iInAppBillingService;
    MyAsynkTask itemRetreaverAsynkTask;

    int choosenId;

    /**
     * product list
     */
    protected CharSequence choices[] = new CharSequence[]{PROD_PURCHASE, PROD_CANCELED, PROD_REFUNDED, PROD_ITEM_UNAVAILABLE};

    Activity activity;

    public InAppHelper(Activity a) {
        this.activity = a;
    }


    /**
     * creates IInAppBillingService intent and bind to activity
     */
    public void initInAppBilling() {

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");

        activity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * create service connection to bind service
     */
    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            iInAppBillingService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            iInAppBillingService = null;
        }
    };

    /**
     * make a toast
     *
     * @param msg text that want to desplay
     */
    public void showToast(String msg) {

        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    protected void showAlert(String title) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(title);
        dialog.setSingleChoiceItems(choices, 0, this);
        dialog.setPositiveButton("Ok", this);
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }

    /**
     * To check response code coming from in app billing methods
     *
     * @param rc response code
     */
    public void checkGooglePlayResponseCode(int rc) {

        switch (rc) {
            case 0:
                showToast("Success");
                break;
            case 1:
                showToast("User pressed back or canceled a dialog");
                break;
            case 2:
                showToast("Network connection is down");
                break;
            case 3:
                showToast("Billing API version is not supported for the type requested");
                break;
            case 4:
                showToast("Requested product is not available for purchase");
                break;
            case 5:
                showToast("Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest");
                break;
            case 6:
                showToast("Fatal error during the API action");
                break;
            case 7:
                showToast("Failure to purchase since item is already owned");
                break;
            case 8:
                showToast("Failure to consume since item is not owned");
                break;
            default:
                showToast("No response");
                break;
        }
    }

    public boolean verifyPurchase(String base64PublicKey, String purchasedData, String resultSignature) {

        if (base64PublicKey != null && resultSignature != null) {
            PublicKey key = generateKey(base64PublicKey);

            return verify(key, purchasedData, resultSignature);
        }

        return false;
    }

    /**
     * Generates and return a instance of PublicKey from the given base64Public key
     *
     * @param base64PublicKey base 64 public key
     * @return
     */
    private PublicKey generateKey(String base64PublicKey) {

        try {
            byte[] decodedKey = Base64.decode(base64PublicKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            Log.e("Generated key : ", "Invalid key specification.");
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey  public key associated with the developer account
     * @param signedData signed data from server
     * @param signature  server signature
     * @return true if the data and signature match
     */
    private boolean verify(PublicKey publicKey, String signedData, String signature) {
        byte[] signatureBytes;
        try {
            signatureBytes = Base64.decode(signature, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            Log.e("verify key :", "Base64 decoding failed.");
            return false;
        }
        try {
            java.security.Signature sig = java.security.Signature.getInstance(KEY_SHA1WITHRSA);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(signatureBytes)) {
                Log.e("verify key :", "Signature verification failed.");
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            Log.e("verify key :", "NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            Log.e("verify key :", "Invalid key specification.");
        } catch (SignatureException e) {
            Log.e("verify key :", "Signature exception.");
        }
        return false;
    }

    public boolean checkInppBillingVersion(int version, String typOfPurchase) {
        int res = -1;
        try {
            res = iInAppBillingService.isBillingSupported(version, activity.getPackageName(), typOfPurchase);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.i("version verfication ", "for v:" + version + " t:" + typOfPurchase + " response:" + res);

        return (res == 0);
    }

    public boolean consumingPurchase(String purchaseToken, int ver) {

        int response = -1;

        try {
            response = iInAppBillingService.consumePurchase(ver, activity.getPackageName(), purchaseToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        checkGooglePlayResponseCode(response);

        if (response == 0) {
            Log.i("consumingPurchase", "-------------");
            Log.i("consumingPurchase", "Successullly completed");
        }

        return response == 0;
    }

    public void purchaseItems(String productId, String type, int requestCode, String developerPayLoad, int ver) {


        Bundle purchaseItemBundle = null;

        try {
            purchaseItemBundle = iInAppBillingService.getBuyIntent(ver, activity.getPackageName(), productId, type, developerPayLoad);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        int response = purchaseItemBundle.getInt(RESPONSE_CODE);
        checkGooglePlayResponseCode(response);

        if (response == 0) {

            PendingIntent pendingIntent = purchaseItemBundle.getParcelable(BUY_INTENT);

            try {
                /**
                 * this method will give result in onActivityResult
                 */
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * @return list of AvailableItemDetails class objects
     */
    public ArrayList<AvailableItemDetails> getItemsForPurchase(int ver, String type) {

        ArrayList<AvailableItemDetails> availableItemDetailsList = new ArrayList<AvailableItemDetails>();
        /**
         * create array list of product ids,where each string is a product ID for an purchasable item.
         */
        ArrayList<String> purchasableIds = new ArrayList<String>();
        purchasableIds.add(PROD_PURCHASE);
//        purchasableIds.add(PROD_CANCELED);
//        purchasableIds.add(PROD_REFUNDED);
//        purchasableIds.add(PROD_ITEM_UNAVAILABLE);

        /**
         * create a Bundle that contains a String ArrayList of product IDs with key "ITEM_ID_LIST"
         */
        Bundle queryItemsBundle = new Bundle();
        queryItemsBundle.putStringArrayList(ITEM_ID_LIST, purchasableIds);

        /**
         * create separate thread for execution
         */
        itemRetreaverAsynkTask = new MyAsynkTask(activity, iInAppBillingService, ver, type);

        int response = -1;
        Bundle availableItemsBundle = null;

        try {

            try {
                availableItemsBundle = itemRetreaverAsynkTask.execute(queryItemsBundle).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            response = availableItemsBundle.getInt(RESPONSE_CODE);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        checkGooglePlayResponseCode(response);

        ArrayList<String> responseList = null;

        if (response == 0) {
            responseList = availableItemsBundle.getStringArrayList(DETAILS_LIST);

            for (String thisResponse : responseList) {
                AvailableItemDetails availableItem = new AvailableItemDetails();
                /**
                 * response item format : {"productId":"exampleSku","type":"inapp","price":"$5.00","title" : "Example Title", "description" : "This is an example description" }'
                 */

                JSONObject object = null;

                try {
                    object = new JSONObject(thisResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (object != null) {

                    String pId = object.optString("productId");
                    String pPrice = object.optString("price");
                    String pTitle = object.optString("title");
                    String pDescription = object.optString("description");

                    availableItem.setProductId(pId);
                    availableItem.setProductDescription(pDescription);
                    availableItem.setProductPrice(pPrice);
                    availableItem.setProductTitle(pTitle);

                    Log.i("getSkuDetails : ", "-------------");
                    Log.i("product id : ", pId);
                    Log.i("product price : ", pPrice);
                    Log.i("product title : ", pTitle);
                    Log.i("product description : ", pDescription);

                    availableItemDetailsList.add(availableItem);

                }
            }
        }

        return availableItemDetailsList;

    }


    /**
     * @return list of ProductDetail object
     */
    public ArrayList<ProductDetail> queryForPurchasedItemes(String continuouToken, String purchaseType, ArrayList<ProductDetail> list, int ver) {

        ArrayList<ProductDetail> mProductDetails = list;
        if (mProductDetails == null)
            mProductDetails = new ArrayList<>();

        Bundle ownedItemsBundle = null;


        try {
            /**
             * The Google Play service returns only the purchases made by the user account that is currently logged in to the device
             *  return up to 700 products that are owned by the user when getPurchase is first called
             *  If the user owns a large number of products, Google Play includes a String token mapped to the key INAPP_CONTINUATION_TOKEN in the response Bundle to indicate that more products can be retrieved
             */
            ownedItemsBundle = iInAppBillingService.getPurchases(ver, activity.getPackageName(), purchaseType, continuouToken);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (ownedItemsBundle != null) {

            int response = ownedItemsBundle.getInt(RESPONSE_CODE);

            checkGooglePlayResponseCode(response);

            if (response == 0) {

                ArrayList<String> ownedItemsIDList = ownedItemsBundle.getStringArrayList(INAPP_PURCHASE_ITEM_LIST);
                Log.i("owned items:", ownedItemsIDList + "");
                if (ownedItemsIDList.size() == 0) {
                    showToast("No Owned Items");
                    return null;
                }
                ArrayList<String> itemDetailsList = ownedItemsBundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST);
                ArrayList<String> itemSignatureList = ownedItemsBundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST);

                String continuousToken = ownedItemsBundle.getString(INAPP_CONTINUATION_TOKEN);

                Log.i("itemDetailsList:", itemDetailsList.toString());
                Log.i("itemSignatureList:", itemSignatureList.toString());
                if (continuousToken != null)
                    Log.i("continuous token:", continuousToken);

                for (int i = 0; i < ownedItemsIDList.size(); i++) {

                    ProductDetail mProduct = new ProductDetail();
                    String itemId = ownedItemsIDList.get(i);
                    mProduct.setProductId(itemId);
                    /**
                     * {"packageName":"com.inappbillingdemo","orderId":"transactionId.android.test.purchased","productId":"android.test.purchased","developerPayload":"testRefKey","purchaseTime":0,"purchaseState":0,"purchaseToken":"inapp:com.inappbillingdemo:android.test.purchased"}

                     */
                    String itemData = itemDetailsList.get(i);
                    try {
                        JSONObject dataObj = new JSONObject(itemData);
                        String packageName = dataObj.optString("packageName");
                        String orderId = dataObj.optString("orderId");
                        String developerPayload = dataObj.optString("developerPayload");
                        String purchaseTime = dataObj.optString("purchaseTime");
                        String purchaseState = dataObj.optString("purchaseState");
                        String purchaseToken = dataObj.optString("purchaseToken");

                        mProduct.setPackageName(packageName);
                        mProduct.setOrderId(orderId);
                        mProduct.setDeveloperPayload(developerPayload);
                        mProduct.setPurchaseTime(developerPayload);
                        mProduct.setPurchaseTime(purchaseTime);
                        mProduct.setPurchaseState(purchaseState);
                        mProduct.setPurchaseToken(purchaseToken);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String itemSignature = itemSignatureList.get(i);
                    mProduct.setItemSignature(itemSignature);

                    mProductDetails.add(mProduct);
                    showToast("purchase items : " + itemId);

                    mProductDetails.add(mProduct);
                }

                if (continuousToken != null) {
                    queryForPurchasedItemes(continuouToken, purchaseType, mProductDetails, ver);
                }

            }
        }

        return mProductDetails;
    }

    public void replacePurchasedItemWithOther(int ver, List<String> oldProdIDList, String newProdID, String payload, int requestCode) {

        Bundle purchaseItemBundle = null;

        try {
            purchaseItemBundle = iInAppBillingService.getBuyIntentToReplaceSkus(ver, activity.getPackageName(), oldProdIDList, newProdID,TYPE_INAPP, payload);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        int response = purchaseItemBundle.getInt(RESPONSE_CODE);
        checkGooglePlayResponseCode(response);

        if (response == 0) {

            PendingIntent pendingIntent = purchaseItemBundle.getParcelable(BUY_INTENT);

            try {
                /**
                 * this method will give result in onActivityResult
                 */
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:

                purchaseItems(choices[choosenId].toString(), TYPE_SUBS, REQUEST_CODE, KEY_PAYLOAD, BILLING_VER);

                break;
            case 1:
            case 2:
            case 3:
            case 0:
                choosenId = which;
                break;
            default:
                break;
        }

    }
}
