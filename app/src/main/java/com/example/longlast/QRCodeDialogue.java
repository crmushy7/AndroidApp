package com.example.longlast;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class QRCodeDialogue {

    public static void show(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);


        View view = LayoutInflater.from(context).inflate(R.layout.activity_qrcode_dialogue, null);
        dialog.setContentView(view);

        final ImageView imageViewQRCode = view.findViewById(R.id.imageViewQRCode);
        Button buttonGenerateQRCode = view.findViewById(R.id.buttonGenerateQRCode);

        // Get the device's IP address
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return getDeviceIPAddress(context);
            }

            @Override
            protected void onPostExecute(String ipAddress) {
                // Generate the QR code with the device's IP address
                if (ipAddress != null) {
                    Bitmap qrCode = generateQRCode(ipAddress);
                    if (qrCode != null) {
                        imageViewQRCode.setImageBitmap(qrCode);
                    } else {
                        Toast.makeText(context, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to get device IP address", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();

        buttonGenerateQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The button click event is now empty since you want to exclude textname and textprice
                // If you need to perform any action, you can add it here
            }
        });

        dialog.show();
    }

    private static Bitmap generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 500, 450);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getDeviceIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return formatIPAddress(ipAddress);
        } else {
            return getHotspotIPAddress();
        }
    }

    private static String formatIPAddress(int ipAddress) {
        return String.format(
                "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)
        );
    }

    private static String getHotspotIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan0") || intf.getName().contains("ap0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
