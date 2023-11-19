package com.example.longlast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class QRCodeScannerDialogActivity extends AppCompatActivity {

    private IntentIntegrator integrator;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        textView=findViewById(R.id.deni);

        // Initialize the integrator
        integrator = new IntentIntegrator(this);

        // Show the dialog on button click
        showScanDialog();
    }

    private void showScanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan QR Code");
        builder.setMessage("You are about to receive money(As a debt) from a person, if this is your desired intention,Click Lend money button from the other device of the person you want to receive money from then click SCAN ");

        builder.setPositiveButton("Scan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Start QR code scanning
                startQRCodeScan();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancel action
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startQRCodeScan() {
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(QRCodeScannerActivity.class); // Custom QRCodeScannerActivity
        integrator.initiateScan();
    }

    String readedText;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of the QR code scan
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Handle the scanned QR code data (result.getContents())
                String scannedText = result.getContents();


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket(scannedText,1234);
                            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            PrintWriter out=new PrintWriter(socket.getOutputStream());

                            out.println("Connection established");
                            String response=in.readLine();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView.setText(response);
//                                    Intent intent=new Intent(QRCodeScannerDialogActivity.this, ClientActivity.class);
//                                    startActivity(intent);

                                }
                            });
                            in.close();
                            out.close();
                            socket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();

                // Open a dialog box with the extracted text
                showTextDialog(readedText);
//                Intent intent=new Intent(QRCodeScannerDialogActivity.this,ClientActivity.class);
//                intent.putExtra(ClientActivity.ipAdress,scannedText);
//                startActivity(intent);
            } else {
                // Handle the case where scanning was canceled
                // You may want to show a message or take appropriate action
                finish(); // Finish the activity if scanning was canceled
            }
        }
    }

    private void showTextDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanned Text");
        builder.setMessage(text);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle OK button click if needed
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
