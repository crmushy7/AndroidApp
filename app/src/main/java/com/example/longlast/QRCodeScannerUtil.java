// QRCodeScannerUtil.java
package com.example.longlast;

import android.app.Activity;
import com.google.zxing.integration.android.IntentIntegrator;

public class QRCodeScannerUtil {

    public static void startQRCodeScan(Activity activity) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(QRCodeScannerActivity.class); // Custom QRCodeScannerActivity
        integrator.initiateScan();
    }
}
