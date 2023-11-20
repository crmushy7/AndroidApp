package com.example.longlast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class Homepage extends AppCompatActivity {
    LinearLayout linearLayout, borrowmoney, pay;
    TextView textView;
    boolean serverRunning = false;
    private ServerHost serverHost;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        textView = findViewById(R.id.outdeni);
        linearLayout = findViewById(R.id.lendmoneylayout);
        borrowmoney = findViewById(R.id.borrowmoneylayout);
        pay = findViewById(R.id.payment);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, ServerActivity.class);
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
                checkAndEnableHotspot();
            }
        });
    }

    private void checkAndEnableHotspot() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null && isHotspotEnabled(wifiManager)) {
            // Hotspot is already on
            Toast.makeText(this, "Hotspot is already on", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Homepage.this, QRCodeScannerDialogActivity.class);
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

    public void onServerStart() {
        serverHost = new ServerHost(this);
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
}
