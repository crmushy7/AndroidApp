package com.example.longlast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    UserRecords userRecords;
    private IntentIntegrator integrator;
    private EditText editTextForClientChatMessages;
    private EditText editTextUserInput;
    private Button buttonSend;
    private PrintWriter out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);

        //Fetch USer details from local Database and set them to userRecords variable.
        DatabaseSupport databaseSupport = new DatabaseSupport(this,"msomali");
        userRecords = databaseSupport.getUser();

        // Initialize UI components
        editTextForClientChatMessages = findViewById(R.id.editTextForClientChatMessages);
        editTextUserInput = findViewById(R.id.editTextUserInput);
        buttonSend = findViewById(R.id.buttonSend);

        // Initialize the integrator
        integrator = new IntentIntegrator(this);

        // Show the dialog on button click
        showScanDialog();

        // Set up click listener for the Send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = editTextUserInput.getText().toString();
                // Send the user input to the server
                if (out != null) {
                    // Send the message to the server using the AsyncTask
                    new SendTask(out, userInput).execute();
                }
            }
        });
    }

    private void showScanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan QR Code");
        builder.setMessage("You are about to receive money (As a debt) from a person. If this is your desired intention, Click the Lend money button from the other person's device, then click SCAN.");

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of the QR code scan
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Handle the scanned QR code data (result.getContents())
                String scannedText = result.getContents();

                // Initialize socket communication with the server
                setupSocket(scannedText);
            } else {
                // Handle the case where scanning was canceled
                // You may want to show a message or take appropriate action
                finish(); // Finish the activity if scanning was canceled
            }
        }
    }

    private void setupSocket(String serverAddress) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(serverAddress, 1234);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    String receivedMessage = in.readLine();
                    if (receivedMessage == null) {
                        break;
                    }

                    runOnUiThread(() -> {
                        // Update UI with received message
                        showReceivedText(receivedMessage);
                    });
                }

                in.close();
                out.close();
                socket.close();

                // After socket communication is done, show the scan dialog again
                showScanDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showReceivedText(String receivedText) {
        // Append the received message to the EditText
        editTextForClientChatMessages.append("server: "+receivedText + "\n");
        Toast.makeText(this, receivedText, Toast.LENGTH_SHORT).show();
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private final PrintWriter out;
        private final String message;

        // Constructor to receive PrintWriter and message
        public SendTask(PrintWriter out, String message) {
            this.out = out;
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Perform network operations in the background
            try {
                if (out != null) {
                    // Send the message to the server using the PrintWriter
                    out.println("Client: " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // This method runs on the main thread, so you can safely update the UI here
            showSentMessageToast();
        }

        private void showSentMessageToast() {
            runOnUiThread(() -> {
                Toast.makeText(QRCodeScannerDialogActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                // Append the sent message to the EditText
                editTextForClientChatMessages.append("You: " + message + "\n");
                editTextUserInput.setText("");
            });
        }
    }

}
