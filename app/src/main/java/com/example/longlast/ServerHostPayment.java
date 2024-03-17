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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class ServerHostPayment extends Thread {
    private ServerSocket srSocket;
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

    public ServerHostPayment(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
    }

    public void setEditTextForServerChatMessages(EditText editText) {
        this.editTextForServerChatMessages = editText;
    }

    public void startServer(String transactionId) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
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


//            receiptRef = userRef.child("Receipts").child("All Receipt").push();
//            receiptRef.child("debtor").setValue(parts[1]);
//            receiptRef.child("debtor Email").setValue(parts[2]);
//            receiptRef.child("Amount").setValue(Homepage.amountToSend);
//            receiptRef.child("Date").setValue(currentdate);
//            receiptRef.child("Time").setValue(formattedTime);
//            receiptRef.child("Status").setValue("Credit");



            //kwa mdaiwa
            DatabaseReference debtorRef=databaseReference.child(parts[3]);



//            debtorReceiptRef=debtorRef.child("Receipts").child("All Receipt").child(receiptRef.getKey().toString());
//            debtorReceiptRef.child("creditor").setValue(userRecords.getFullName());
//            debtorReceiptRef.child("creditor Email").setValue(userRecords.getEmail());
//            debtorReceiptRef.child("Amount").setValue(Homepage.amountToSend);
//            debtorReceiptRef.child("Date").setValue(currentdate);
//            debtorReceiptRef.child("Time").setValue(formattedTime);
//            debtorReceiptRef.child("Status").setValue("Debt");

            DatabaseReference creditReceiveRef = userRef.child("Receipts").child("Debit").child(Homepage.receiptID);
            DatabaseReference creditAllReceipts = userRef.child("Receipts").child("All Receipt").child(Homepage.receiptID);
            DatabaseReference debitAllReceipts = debtorRef.child("Receipts").child("All Receipt").child(Homepage.receiptID);
            debitAllReceipts.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Toast.makeText(context, "Exist...", Toast.LENGTH_SHORT).show();
                                DatabaseReference debtorReceiptRef=debtorRef.child("Receipts").child("Paid").child(Homepage.receiptID);
                                DatabaseReference debtorReceiptRefCleared=debtorRef.child("Receipts").child("Cleared").child(Homepage.receiptID);
                                DatabaseReference receiptRef = userRef.child("Receipts").child("Paid").child(Homepage.receiptID);
                                DatabaseReference receiptRefCleared = userRef.child("Receipts").child("Cleared").child(Homepage.receiptID);
                                DatabaseReference debitReceivedRef=debtorRef.child("Receipts").child("Credit").child(Homepage.receiptID);
                                int amount=0;
                                int payAmount=0;
                                amount=Integer.parseInt(snapshot.child("Amount").getValue(String.class));
                                payAmount=Integer.parseInt(Homepage.amounttopay);
                                Toast.makeText(context, amount+" "+payAmount, Toast.LENGTH_SHORT).show();

                                int actualAmount=amount-payAmount;
                                if (actualAmount==0){
                                    creditAllReceipts.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "successfully cleared!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    debitAllReceipts.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "successfully cleared!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    debitReceivedRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                debitReceivedRef.removeValue();
                                            }else{
                                                Toast.makeText(context, "debit unavailable", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    creditReceiveRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                creditReceiveRef.removeValue();
                                            }else{
                                                Toast.makeText(context, "credit unavailable", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    //nazihifadhi kwenye page ya history ambazo ni cleared zote
                                    debtorReceiptRefCleared.child("Amount").setValue(actualAmount+"");
                                    debtorReceiptRefCleared.child("creditor").setValue(userRecords.getFullName());
                                    debtorReceiptRefCleared.child("creditor Email").setValue(userRecords.getEmail());
                                    debtorReceiptRefCleared.child("Date").setValue(currentdate);
                                    debtorReceiptRefCleared.child("Time").setValue(formattedTime);
                                    debtorReceiptRefCleared.child("Status").setValue("Cleared");

                                    receiptRefCleared.child("Amount").setValue(actualAmount+"");
                                    receiptRefCleared.child("debtor").setValue(parts[1]);
                                    receiptRefCleared.child("debtor Email").setValue(parts[2]);
                                    receiptRefCleared.child("Date").setValue(currentdate);
                                    receiptRefCleared.child("Time").setValue(formattedTime);
                                    receiptRefCleared.child("Status").setValue("Debt");


                                } else if (actualAmount<0) {
                                    Toast.makeText(context, "The amount being paid exceeds the actual required amount", Toast.LENGTH_LONG).show();
                                    ServerHost.closeSocket();
                                }else{
                                    creditReceiveRef.child("Amount").setValue(actualAmount+"");
                                    debitReceivedRef.child("Amount").setValue(actualAmount+"");
                                    debtorReceiptRef.child("creditor").setValue(userRecords.getFullName());
                                    debtorReceiptRef.child("creditor Email").setValue(userRecords.getEmail());
                                    debtorReceiptRef.child("Date").setValue(currentdate);
                                    debtorReceiptRef.child("Time").setValue(formattedTime);
                                    debtorReceiptRef.child("Status").setValue("Debt");

                                    creditReceiveRef.child("debtor").setValue(parts[1]);
                                    creditReceiveRef.child("debtor Email").setValue(parts[2]);
                                    creditReceiveRef.child("Date").setValue(currentdate);
                                    creditReceiveRef.child("Time").setValue(formattedTime);
                                    creditReceiveRef.child("Status").setValue("credit");
                                    creditAllReceipts.child("Amount").setValue(actualAmount+"");
                                    debitAllReceipts.child("Amount").setValue(actualAmount+"");

                                }

                            }else {
                                Toast.makeText(context, "Receipt unavailable", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


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
            receivedAmount.setText(Homepage.amounttopay + " Tsh");
            receivedaMOUNT2.setText(Homepage.amounttopay + " Tsh");
            receiverEmail.setText(parts[2]);
            transactionID.setText(Homepage.receiptID);

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


