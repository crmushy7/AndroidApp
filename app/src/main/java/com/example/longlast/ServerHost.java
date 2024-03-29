package com.example.longlast;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ServerHost extends Thread {
    private ServerSocket srSocket;
    public static int portNumber=0;
    private boolean serverRunning;
    private Handler handler;
    private Context context;
    private PrintWriter out;
    UserRecords userRecords;
    public static String title;
    String newme = "";
    private EditText editTextForServerChatMessages;
    private TextView TextviewForServerChatMessages;
    private ProgressDialog progressDialog;
    private AlertDialog dialog;

    public ServerHost(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
    }

    public void setEditTextForServerChatMessages(EditText editText) {
        this.editTextForServerChatMessages = editText;
    }

    public void startServer() {
        if (editTextForServerChatMessages == null) {
            throw new IllegalStateException("EditText instance is not set. Call setEditTextForServerChatMessages() before starting the server.");
        }

        serverRunning = true;
        start();
    }

    @Override
    public void run() {
        try {
            if (srSocket != null && !srSocket.isClosed()) {
                srSocket.close();
            }
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            srSocket = new ServerSocket();
            srSocket.setReuseAddress(true);
            srSocket.bind(new InetSocketAddress(Homepage.portNumber));
            DatabaseSupport databaseSupport = new DatabaseSupport(context, "msomali");
            userRecords = databaseSupport.getUser();

            while (serverRunning) {
                Socket socket = srSocket.accept();

                String newdata = "You are about to receive " + Homepage.amountToSend + "Tsh from " + userRecords.getFullName();
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Connected to " + userRecords.getFullName() + "!");
                sendToClient(newdata);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                handler.post(() -> {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Please wait for confirmation...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                });

                while (true) {
                    String receivedText = in.readLine();
                    if (receivedText == null) {
                        break;
                    } else {
                        String[] parts = receivedText.split(",");
                        String firstWord = parts[0];

                        if ("OKAY".equals(firstWord)) {
                            String restOfData = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                            progressDialog.dismiss();
                            showServerPopup(receivedText);
                            break; // Exiting the inner loop as you've received "OKAY"
                        }
                    }
                }

                in.close();
                out.close();
                socket.close();
                portNumber = 0; // Not sure what you intend to do with this line
            }
        } catch (IOException e) {
            e.printStackTrace();
//            throw new RuntimeException(e+"hapaa");
        } finally {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (srSocket != null && !srSocket.isClosed()) {
                            srSocket.close();
                            Toast.makeText(context, "ServerSocket closed successfully", Toast.LENGTH_SHORT).show();
                            Log.d("ServerHost", "ServerSocket closed successfully");
                        } else {
                            Toast.makeText(context, "ServerSocket is already closed or null", Toast.LENGTH_SHORT).show();
                            Log.d("ServerHost", "ServerSocket is already closed or null");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error closing ServerSocket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ServerHost", "Error closing ServerSocket: " + e.getMessage());
                    }
                }
            });
        }


    }


    private void sendToClient(String userData) {
        out.println(userData);
    }

    private void showServerPopup(String data) {
        handler.post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            View popupView = LayoutInflater.from(context).inflate(R.layout.risiti, null);

            Button okReceipt=popupView.findViewById(R.id.risitibutton);
            TextView receiverName = popupView.findViewById(R.id.risitiName);
            TextView receiverEmail = popupView.findViewById(R.id.risitiEmail);
            TextView receivedAmount = popupView.findViewById(R.id.risitiAmount);
            TextView receivedDate = popupView.findViewById(R.id.risitiDate);
            TextView receivedTime = popupView.findViewById(R.id.risitiTime);
            TextView receivedaMOUNT2 = popupView.findViewById(R.id.msomi2);
            TextView transactionID = popupView.findViewById(R.id.risitiTransactionId);
            TextView kifupiChaJina=popupView.findViewById(R.id.risitiShortForm);
            Calendar calendar = Calendar.getInstance();
            String currentdate = DateFormat.getDateInstance().format(calendar.getTime());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
            String formattedTime = simpleDateFormat.format(new Date());
            String[] parts = data.split(",");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("All Users");
            DatabaseReference userRef = databaseReference.child(FirebaseAuth.getInstance().getUid().toString());
            DatabaseReference receiptRef = userRef.child("Receipts").child("Credit").push();
            receiptRef.child("debtor").setValue(parts[1]);
            receiptRef.child("debtor Email").setValue(parts[2]);
            receiptRef.child("Amount").setValue(Homepage.amountToSend);
            receiptRef.child("Date").setValue(currentdate);
            receiptRef.child("Time").setValue(formattedTime);
            receiptRef.child("Status").setValue("Credit");

            receiptRef = userRef.child("Receipts").child("All Receipt").push();
            receiptRef.child("debtor").setValue(parts[1]);
            receiptRef.child("debtor Email").setValue(parts[2]);
            receiptRef.child("Amount").setValue(Homepage.amountToSend);
            receiptRef.child("Date").setValue(currentdate);
            receiptRef.child("Time").setValue(formattedTime);
            receiptRef.child("Status").setValue("Credit");



            //kwa mdaiwa
            DatabaseReference debtorRef=databaseReference.child(parts[3]);
            DatabaseReference debtorReceiptRef=debtorRef.child("Receipts").child("Debit").child(receiptRef.getKey().toString());
            debtorReceiptRef.child("creditor").setValue(userRecords.getFullName());
            debtorReceiptRef.child("creditor Email").setValue(userRecords.getEmail());
            debtorReceiptRef.child("Amount").setValue(Homepage.amountToSend);
            debtorReceiptRef.child("Date").setValue(currentdate);
            debtorReceiptRef.child("Time").setValue(formattedTime);
            debtorReceiptRef.child("Status").setValue("Debt");


            debtorReceiptRef=debtorRef.child("Receipts").child("All Receipt").child(receiptRef.getKey().toString());
            debtorReceiptRef.child("creditor").setValue(userRecords.getFullName());
            debtorReceiptRef.child("creditor Email").setValue(userRecords.getEmail());
            debtorReceiptRef.child("Amount").setValue(Homepage.amountToSend);
            debtorReceiptRef.child("Date").setValue(currentdate);
            debtorReceiptRef.child("Time").setValue(formattedTime);
            debtorReceiptRef.child("Status").setValue("Debt");


            receivedTime.setText(formattedTime);
            receivedDate.setText(currentdate);


            String fullName=parts[1]+"";
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
            receiverName.setText(parts[1]);
            receivedAmount.setText(Homepage.amountToSend + " Tsh");
            receivedaMOUNT2.setText(Homepage.amountToSend + " Tsh");
            receiverEmail.setText(parts[2]);
            transactionID.setText(receiptRef.getKey());

            okReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    dialog.cancel();
                    closeSocket();


                }
            });


            // Use the 'data' variable in your UI components as needed

            builder.setView(popupView);
            dialog = builder.create();
            dialog.show();



        });
    }




    public class SendTask extends AsyncTask<Void, Void, Void> {
        private final PrintWriter out;
        private final String message;

        public SendTask(PrintWriter out, String message) {
            this.out = out;
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (out != null) {
                out.println(message);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Handle any post-execution tasks if needed
        }
    }
    public static void closeSocket() {
        try (ServerSocket srSocket = new ServerSocket(1234)) {
            // Close the server socket
            srSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


