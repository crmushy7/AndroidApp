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
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("All Users").child(userUid).child("Details");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("Fullname").exists() ? dataSnapshot.child("Fullname").getValue(String.class) : "";
                        newme = fullName;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that occur
                }
            });

            srSocket = new ServerSocket(1234);
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
                        } else {
                            break;
                        }
                    }
                }

                in.close();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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

            //kwa mdaiwa
            DatabaseReference debtorRef=databaseReference.child(parts[3]);
            DatabaseReference debtorReceiptRef=debtorRef.child("Receipts").child("Debit").child(receiptRef.getKey().toString());
            debtorReceiptRef.child("creditor").setValue(userRecords.getFullName());
            debtorReceiptRef.child("creditor Email").setValue(userRecords.getEmail());
            debtorReceiptRef.child("Amount").setValue(Homepage.amountToSend);
            debtorReceiptRef.child("Date").setValue(currentdate);
            debtorReceiptRef.child("Time").setValue(formattedTime);

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
                }
            });


            // Use the 'data' variable in your UI components as needed

            builder.setView(popupView);
            dialog = builder.create();
            dialog.show();


//            // Generate a unique filename for the PDF
//            String pdfFileName = "receipt_" + UUID.randomUUID().toString() + ".pdf";
//
//            // Capture the XML layout content using a WebView
//            WebView webView = new WebView(context);
//            webView.loadUrl("file:///android_res/layout/risiti.xml"); // Replace with your XML layout file
//
//            // Print the content as PDF
//            PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
//            printManager.print("Document", new MyPrintDocumentAdapter(webView, webView.createPrintDocumentAdapter(), pdfFileName), null);

            // Save and upload the PDF to Firebase Storage
//            saveAndUploadToFirebase(pdfFileName);
        });
    }

//    private void saveAndUploadToFirebase(String pdfFileName) {
//        // Delay to ensure the PDF creation completes before capturing the WebView content
//        handler.postDelayed(() -> {
//            // Capture the WebView content as a Bitmap
//            Bitmap bitmap = Bitmap.createBitmap(dialog.getWindow().getDecorView().getWidth(), dialog.getWindow().getDecorView().getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
//            dialog.getWindow().getDecorView().draw(canvas);
//
//            // Save the captured content to a PDF file
//            File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName);
//            try {
//                FileOutputStream fos = new FileOutputStream(pdfFile);
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // Upload the PDF file to Firebase Storage
//            uploadToFirebase(pdfFile, pdfFileName);
//        }, 1000); // Delay time in milliseconds
//    }
//    private void uploadToFirebase(File pdfFile, String pdfFileName) {
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference();
//
//        // Replace "user_id_1" with the actual user ID
//        String userId = FirebaseAuth.getInstance().getUid().toString();
//
//        // Create a reference to the "receipts" folder in Firebase Storage
//        StorageReference receiptsRef = storageRef.child("Receipts").child(userId).child(pdfFileName);
//
//        // Upload the file to Firebase Storage
//        receiptsRef.putFile(Uri.fromFile(pdfFile))
//                .addOnSuccessListener(taskSnapshot -> {
//                    // File uploaded successfully
//                    Log.d("Upload", "Success");
//                })
//                .addOnFailureListener(e -> {
//                    // Handle unsuccessful uploads
//                    Log.e("Upload", "Failed", e);
//                });
//    }
//    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {
//        private final PrintDocumentAdapter delegate;
//        private final String fileName;
//        private final WebView webView;
//
//        public MyPrintDocumentAdapter(WebView webView, PrintDocumentAdapter delegate, String fileName) {
//            this.webView = webView;
//            this.delegate = delegate;
//            this.fileName = fileName;
//        }
//
//        @Override
//        public void onStart() {
//            delegate.onStart();
//        }
//
//        @Override
//        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
//            delegate.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras);
//        }
//
//
//
//
//        @Override
//        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
//            // Let the delegate handle the initial writing
//            delegate.onWrite(pages, destination, cancellationSignal, callback);
//
//            // Perform additional actions after the writing has finished
//            callback.onWriteFinished(pages);
//
//            // Create a PDF file from the ParcelFileDescriptor
//            File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
//
//            try (FileOutputStream fos = new FileOutputStream(pdfFile);
//                 FileInputStream fis = new FileInputStream(destination.getFileDescriptor())) {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = fis.read(buffer)) != -1) {
//                    fos.write(buffer, 0, bytesRead);
//                }
//
//                // Upload the PDF file to Firebase Storage
//                uploadToFirebase(pdfFile, fileName);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onFinish() {
//            delegate.onFinish();
//        }
//
//
//
//        public PrintDocumentInfo getInfo() {
//            PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder(fileName);
//            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
//                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
//                    .build();
//            return builder.build();
//
//
//        }
//    }


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
}


