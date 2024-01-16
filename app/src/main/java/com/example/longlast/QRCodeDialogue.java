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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

public class QRCodeDialogue {


    UserRecords userRecords;
    private boolean isOpen = true;

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }


    public static void show(final Context context,UserRecords userRecords) {
         AlertDialog dialog;
//        final Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.activity_qrcode_dialogue, null);
        builder.setView(view);
        dialog = builder.create();




//        dialog.setContentView(view);
        TextView username=view.findViewById(R.id.tvusernameqrcode);
        TextView phonenumber=view.findViewById(R.id.tvusernumberqrcode);
        TextView currenttme=view.findViewById(R.id.tvcurrenttimeqr);
        TextView currentdatee=view.findViewById(R.id.tvcurrentdateqr);
        TextView currentday=view.findViewById(R.id.tvpresentday);
        TextView currentmonth=view.findViewById(R.id.tvpresentmonth);
        TextView currentyear=view.findViewById(R.id.tvcurrentyear);
        TextView kiasi=view.findViewById(R.id.tvamountqr);
        TextView useremail=view.findViewById(R.id.tvuserEmailqr);
        Calendar calendar=Calendar.getInstance();
        String currentdate= DateFormat.getDateInstance().format(calendar.getTime());
        SimpleDateFormat dayfFormat=new SimpleDateFormat("EEEE",Locale.getDefault());
        SimpleDateFormat monthfFormat=new SimpleDateFormat("MMMM",Locale.getDefault());
        SimpleDateFormat yearfFormat=new SimpleDateFormat("YYYY",Locale.getDefault());
        String currentday1=dayfFormat.format(calendar.getTime());
        String currentMonth=monthfFormat.format(calendar.getTime());
        String currentYear=yearfFormat.format(calendar.getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        String formattedTime = simpleDateFormat.format(new Date());
        currenttme.setText(formattedTime);
        currentdatee.setText(currentdate);
        currentday.setText(currentday1);
        currentmonth.setText(currentMonth);
        currentyear.setText(currentYear);

//        DatabaseSupport databaseSupport = new DatabaseSupport(userRecords,"msomali");
//        userRecords =view.databaseSupport.getUser();
        username.setText(userRecords.getFullName());
        phonenumber.setText(userRecords.getMobileNumber());
        kiasi.setText(Homepage.amountToSend+" Tsh");
        useremail.setText(userRecords.getEmail());




        final ImageView imageViewQRCode = view.findViewById(R.id.imageViewQRCode);
//        Button buttonGenerateQRCode = view.findViewById(R.id.buttonGenerateQRCode);


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

//        buttonGenerateQRCode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // The button click event is now empty since you want to exclude textname and textprice
//                // If you need to perform any action, you can add it here
//            }
//        });


        dialog.show();

        while (!dialog.isShowing()) {
            dialog.cancel();
        }
//        Window window=dialog.getWindow();
//        WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
//        layoutParams.copyFrom(window.getAttributes());
//        layoutParams.width=1000;
//        layoutParams.height=1300;
//        window.setAttributes(layoutParams);
    }

    private static Bitmap generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType,Object> hints=new Hashtable<>();
            hints.put(EncodeHintType.MARGIN,0);
            BitMatrix bmat=new MultiFormatWriter().encode(text,BarcodeFormat.QR_CODE,900,900,hints);
//            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 900, 900);
            int width = 900;
            int height = 900;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bmat.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
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
        int ipAddress=0;

        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
             ipAddress = wifiInfo.getIpAddress();
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
