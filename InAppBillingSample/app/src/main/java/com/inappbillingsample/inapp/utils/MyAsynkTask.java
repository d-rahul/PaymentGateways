package com.inappbillingsample.inapp.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

public class MyAsynkTask extends AsyncTask<Bundle, Void, Bundle> {

    Context mContext;
    IInAppBillingService mService;
    ProgressDialog pd;
    int billingVersion;
    String purchaseType;

    MyAsynkTask(Context cxt, IInAppBillingService service, int ver, String type) {

        this.mContext = cxt;
        this.mService = service;
        this.billingVersion = ver;
        this.purchaseType = type;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

//        pd = new ProgressDialog(mContext);
//        pd.setTitle("Loading ..");
//        pd.show();
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {

        Bundle b = queringForItemsAvailableforPurchase(params[0], purchaseType, billingVersion);
        return b;
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
//        if (pd.isShowing()) pd.dismiss();
        Log.i("asynk result : ", bundle + "");
        super.onPostExecute(bundle);
    }

    /**
     * Querying for Items Available for Purchase
     *
     * @param bundle       bundle of product ids
     * @param purchaseType type of purchase that we want to get result.it is whether "inapp" or "subs"
     * @return bundle of available items
     */
    private Bundle queringForItemsAvailableforPurchase(Bundle bundle, String purchaseType, int ver) {

        Bundle avaialableItemsDetails = null;

        /**
         * we can add items max of 20 to bundle
         * if the request is successful, the returned Bundle has a response code of BILLING_RESPONSE_RESULT_OK (0)
         * Don't call getSkuDetails on Main thread Calling this method triggers a network request which could block your main thread
         */
        try {
            avaialableItemsDetails = mService.getSkuDetails(ver, purchaseType, mContext.getPackageName(), bundle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return avaialableItemsDetails;
    }
}
