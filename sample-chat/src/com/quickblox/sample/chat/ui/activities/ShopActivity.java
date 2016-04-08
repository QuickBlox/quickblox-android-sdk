package com.quickblox.sample.chat.ui.activities;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.billing.PricePoint;

/**
 * Sample charging activity.
 *
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class ShopActivity extends vc908.stickerfactory.ui.activity.ShopWebViewActivity {
    @Override
    protected void onPurchase(String packTitle, final String packName, PricePoint pricePoint) {
        // YOu need to implement own charging functionality. This is only sample
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Charge dialog")
                .setMessage("Purchase " + packTitle + " for " + pricePoint.getLabel() + "?")
                .setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        StickersManager.onPackPurchased(packName);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onPurchaseFail();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
    }
}
