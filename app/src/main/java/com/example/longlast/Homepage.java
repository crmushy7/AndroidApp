package com.example.longlast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Homepage extends AppCompatActivity {
    LinearLayout linearLayout, borrowmoney, pay;
    TextView textView,dplyUser,kifupiChaJina,emailpart,tarehe,saa;
    UserRecords userRecords;
    boolean serverRunning = false;
    private ServerHost serverHost;
    private Context context;
    ImageView imageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newhomepage);
        textView = findViewById(R.id.outdeni);
        linearLayout = findViewById(R.id.lendmoneylayout);
        borrowmoney = findViewById(R.id.borrowmoneylayout);
        pay = findViewById(R.id.payment);
        kifupiChaJina=findViewById(R.id.shortName);
        emailpart=findViewById(R.id.userEmaildisplay);
        imageView=findViewById(R.id.settings);
        dplyUser=findViewById(R.id.displayUserName);
        tarehe=findViewById(R.id.currentdateset);
        saa=findViewById(R.id.currenttimeset);
        context=this;
        DatabaseSupport databaseSupport = new DatabaseSupport(this,"msomali");
        userRecords = databaseSupport.getUser();
        dplyUser.setText(userRecords.getFullName());

        String fullName = userRecords.getFullName();

        if (fullName != null) {
            String[] names = fullName.split(" ", 2);

            if (names.length >= 2) {
                String firstNameLater = names[0].toUpperCase();
                String secondNameLater = names[1].toUpperCase();

                kifupiChaJina.setText(firstNameLater.charAt(0) + "" + secondNameLater.charAt(0));
            } else {
                String firstNameLater = names[0].toUpperCase();
                kifupiChaJina.setText(firstNameLater.charAt(0)+""+firstNameLater.charAt(1));
            }
        }

//        String firstNameLater = userRecords.getFullName().split(" ", 1)[0].toUpperCase();
//        String secondNameLater = userRecords.getFullName().split(" ", 1)[0].toUpperCase();
        emailpart.setText(userRecords.getEmail());
        tarehe.setVisibility(View.GONE);
        saa.setVisibility(View.GONE);

        Thread thread=new Thread(){
            @Override
            public void run() {
                try {
                    tarehe.setVisibility(View.VISIBLE);
                    saa.setVisibility(View.VISIBLE);
                    while (!isInterrupted()){
                        Thread.sleep(10);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar calendar=Calendar.getInstance();
                                String currentdate= DateFormat.getDateInstance().format(calendar.getTime());
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                                String formattedTime = simpleDateFormat.format(new Date());
                                saa.setText(formattedTime);
                                tarehe.setText(currentdate);
                            }
                        });
                    }

                }catch (Exception e){

                }


            }
        };
        thread.start();


//        kifupiChaJina.setText(firstNameLater.charAt(0)+""+secondNameLater.charAt(0)+"");


        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();







        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseAuth.signOut();
                Intent intent=new Intent(Homepage.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming you have the UID of the user you want to retrieve data for

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
        // Initialize the serverHost object
        serverHost = new ServerHost(this);

        // Inflate the server_popup_layout to access its views
        View popupView = LayoutInflater.from(this).inflate(R.layout.server_popup_layout, null);

        // Find the EditText instance in the inflated layout
        EditText editTextForServerChatMessages = popupView.findViewById(R.id.editTextForServerChatMessages);

        // Set the EditText instance in the ServerHost
        serverHost.setEditTextForServerChatMessages(editTextForServerChatMessages);

        // Start the server
        serverHost.startServer();
    }




    public static String amountToSend;
    private  AlertDialog dialog;
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

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View popupView = LayoutInflater.from(context).inflate(R.layout.activity_server, null);

            EditText edittextamount = popupView.findViewById(R.id.edMessage);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText desciptionmessage = popupView.findViewById(R.id.descriptioned);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button proceedtoQrcode = popupView.findViewById(R.id.proceedbutton);

            builder.setView(popupView);
            dialog = builder.create();


            proceedtoQrcode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String amount=edittextamount.getText().toString();
                    String description=desciptionmessage.getText().toString();
                    amountToSend=amount;
                    if (amount.length()==0){
                        edittextamount.setError("input amount");
                    }else {
                        QRCodeDialogue.show(Homepage.this,userRecords);
                        dialog.cancel();
                    }

                }
            });
            dialog.show();


        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        TextView tv=findViewById(R.id.tvdisplayname);
//
//        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        // Reference to the "All Users" node in your Firebase Realtime Database
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("All Users").child(userUid).child("Details");
//
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    Log.d("TAG", "DataSnapshot key: " + dataSnapshot.getKey());
//                    Log.d("TAG", "DataSnapshot value: " + dataSnapshot.getValue());
//
//                    Toast.makeText(Homepage.this, "Exists", Toast.LENGTH_SHORT).show();
//
//                    // Retrieve the user data
//                    String fullName = dataSnapshot.child("Fullname").exists() ? dataSnapshot.child("Fullname").getValue(String.class) : "";
//                    String email = dataSnapshot.child("username").exists() ? dataSnapshot.child("username").getValue(String.class) : "";
//                    String phoneNumber = dataSnapshot.child("PhoneNumber").exists() ? dataSnapshot.child("PhoneNumber").getValue(String.class) : "";
//
//
//                    tv.setText(fullName);
//                    Toast.makeText(Homepage.this, "Fullname: " + fullName + "\nEmail: " + email + "\nPhone Number: " + phoneNumber, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(Homepage.this, "Data not found", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle any errors that occur
//                Toast.makeText(Homepage.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
}
