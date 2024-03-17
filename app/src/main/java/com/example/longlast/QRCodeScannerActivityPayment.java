package com.example.longlast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class QRCodeScannerActivityPayment extends AppCompatActivity {

    private IntentIntegrator integrator;
    private EditText editTextForClientChatMessages;
    private EditText editTextUserInput;
    TextView textView;
    private Button buttonSend;
    UserRecords userRecords;
    private PrintWriter out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);

        editTextForClientChatMessages = findViewById(R.id.editTextForClientChatMessages);
        editTextUserInput = findViewById(R.id.editTextUserInput);
        textView = findViewById(R.id.textViewReceivedText);
        buttonSend = findViewById(R.id.buttonSend);

        textView.setVisibility(View.GONE);
        editTextUserInput.setVisibility(View.GONE);

        integrator = new IntentIntegrator(this);

        showScanDialog();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = "OKAY";
                Intent intent = new Intent(QRCodeScannerActivityPayment.this, Homepage.class);
                startActivity(intent);

                // Send the userRecords details to the server
                if (out != null) {
                    String fullName = userRecords.getFullName();
                    String username = userRecords.getEmail();
                    String phoneNumber = userRecords.getMobileNumber();

                    new SendTask(out, fullName, username, phoneNumber).execute();
                }
            }
        });
    }

    private void showScanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan QR Code");
        builder.setMessage("You are about to receive money (As a payment) from a person. If this is your desired intention, then click SCAN.");

        builder.setPositiveButton("Scan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startQRCodeScan();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startQRCodeScan() {
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(QRCodeScannerActivity.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                QRCodeDialogue qrCodeDialogue = new QRCodeDialogue();
                qrCodeDialogue.setIsOpen(false);

                String scannedText = result.getContents();
                setupSocket(scannedText);
            } else {
                finish();
            }
        }
    }

    private void setupSocket(String serverAddress) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(serverAddress, 1234);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                DatabaseSupport databaseSupport = new DatabaseSupport(this, "msomali");
                userRecords = databaseSupport.getUser();
                String client = userRecords.getFullName();

                while (true) {
                    String receivedMessage = in.readLine();
                    String serverName = in.readLine();
                    if (receivedMessage == null) {
                        break;
                    }

                    runOnUiThread(() -> {
                        showReceivedText(receivedMessage);
                        showReceivedText(serverName);
                    });
                }

                in.close();
                out.close();
                socket.close();

                showScanDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showReceivedText(String receivedText) {
        editTextForClientChatMessages.append(receivedText + "\n");
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private final PrintWriter out;
        private final String fullName;
        private final String username;
        private final String phoneNumber;

        public SendTask(PrintWriter out, String fullName, String username, String phoneNumber) {
            this.out = out;
            this.fullName = fullName;
            this.username = username;
            this.phoneNumber = phoneNumber;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (out != null) {
                    out.println("OKAY"+","+fullName+","+username+","+ FirebaseAuth.getInstance().getUid().toString());
                    out.println(fullName);
                    out.println(username);
                    out.println(phoneNumber);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            runOnUiThread(() -> {
                editTextUserInput.setText("");
            });
        }
    }
}
