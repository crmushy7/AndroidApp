package com.example.longlast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class Homepage extends AppCompatActivity {
  LinearLayout linearLayout,borrowmoney,pay;
  TextView textView;
  boolean serverRunning = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        textView=findViewById(R.id.outdeni);
        linearLayout=findViewById(R.id.lendmoneylayout);
        borrowmoney=findViewById(R.id.borrowmoneylayout);
        pay=findViewById(R.id.payment);
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Homepage.this, ServerActivity.class);
                startActivity(intent);
            }
        });





        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onServerStart();
                checkAndEnableWifi();
            }
        });
        borrowmoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                QRCodeScannerUtil.startQRCodeScan(Homepage.this);
                checkAndEnableHotspot();


            }
        });

    }

    private void checkAndEnableHotspot() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null && isHotspotEnabled(wifiManager)) {
            // Hotspot is already on
            Toast.makeText(this, "Hotspot is already on", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(Homepage.this,QRCodeScannerDialogActivity.class);
            startActivity(intent);

        } else {
            // Hotspot is off or not supported
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 (Q) and above, use the Settings panel
                intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            } else {
                // For versions below Android 10, redirect to Wi-Fi settings
                intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            }

            startActivity(intent);
            Toast.makeText(this, "Please enable hotspot in Settings", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isHotspotEnabled(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

private ServerHost serverHost;
    public void onServerStart(){
        serverHost = new ServerHost();
        serverHost.startServer();
    }

    private void checkAndEnableWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            // Hotspot is off, let's turn it on
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 (Q) and above, use the Settings panel
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
                Toast.makeText(this, "Please enable wifi in Settings", Toast.LENGTH_SHORT).show();
            } else {
                // For versions below Android 10, enable hotspot programmatically
                wifiManager.setWifiEnabled(true);
                Toast.makeText(this, "wifi turned on", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Hotspot is already on
            Toast.makeText(this, "wifi is already on", Toast.LENGTH_SHORT).show();

            QRCodeDialogue.show(Homepage.this);

        }
    }

    public class ServerHost extends Thread implements Runnable{
        private ServerSocket srSocket;
        boolean serverRunning;

        public  void startServer(){
            serverRunning = true;
            start();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void run() {
            try {
                srSocket = new ServerSocket(1234);

                while (serverRunning){
                    Socket socket = srSocket.accept();

                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    printWriter.write("Gideon connected!");
                    printWriter.flush();


//                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    String receivedText=bufferedReader.readLine();
//                    if(receivedText.length() != 0){
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                textView.setText(receivedText);
//                            }
//                        });
//                    }


                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}