package com.example.longlast;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerHost extends Thread {
    private ServerSocket srSocket;
    private boolean serverRunning;
    private Handler handler;
    private Context context;
    private PrintWriter out;
    UserRecords userRecords;
    public  static  String title;

    String newme="";
    private EditText editTextForServerChatMessages;
    private TextView TextviewForServerChatMessages;



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

            // Reference to the "All Users" node in your Firebase Realtime Database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("All Users").child(userUid).child("Details");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("Fullname").exists() ? dataSnapshot.child("Fullname").getValue(String.class) : "";
                        String email = dataSnapshot.child("username").exists() ? dataSnapshot.child("username").getValue(String.class) : "";
                        String phoneNumber = dataSnapshot.child("PhoneNumber").exists() ? dataSnapshot.child("PhoneNumber").getValue(String.class) : "";

                        newme=fullName;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that occur
                }
            });

            srSocket = new ServerSocket(1234);
            DatabaseSupport databaseSupport = new DatabaseSupport(context,"msomali");
            userRecords = databaseSupport.getUser();

//pull jina langu hapa
            while (serverRunning) {
                Socket socket = srSocket.accept();
                String newdata="You are about to receive "+ Homepage.amountToSend +"Tsh from "+userRecords.getFullName();
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Connected to "+userRecords.getFullName()+"!");
                sendToClient(newdata);


                showServerPopup();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {

                    String newTitle=in.readLine();
                    String receivedText = in.readLine();
                    title=newTitle;
                    if (receivedText == "OK") {
                        break;
                    }

                    handler.post(() -> {
                        // Append the received message to the main EditText
                        editTextForServerChatMessages.append(receivedText + "\n");

                        // Append the received message to the TextView in the server popup
                        showReceivedTextPopupInServerPopup(receivedText);
                    });
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

    private void showReceivedTextPopupInServerPopup(String receivedText) {
        handler.post(() -> {
            // Append the received message to the TextView in the server popup
//            TextView textViewReceived = ((AlertDialog) dialog).findViewById(R.id.textViewReceivedText);
//            textViewReceived.append("Sender: " + receivedText + "\n");
            EditText editText=((AlertDialog) dialog).findViewById(R.id.editTextForServerChatMessages);
            editText.append( receivedText +"\n");
        });
    }

    private AlertDialog dialog;

    private void showServerPopup() {

        String jinaClient=QRCodeScannerDialogActivity.clientName;
        handler.post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            View popupView = LayoutInflater.from(context).inflate(R.layout.server_popup_layout, null);

            EditText editTextServerChatMessages = popupView.findViewById(R.id.editTextForServerChatMessages);
            TextView textViewReceived = popupView.findViewById(R.id.textViewReceivedText);
            textViewReceived.setVisibility(View.GONE);
            EditText editTextServerMessage = popupView.findViewById(R.id.editTextServerMessage);
            editTextServerMessage.setVisibility(View.GONE);
            Button buttonSendServerMessage = popupView.findViewById(R.id.buttonSendServerMessage);

            builder.setView(popupView);
            dialog = builder.create();

            // Click listener for the Send button
            buttonSendServerMessage.setOnClickListener(v -> {
                String messageToSend = editTextServerMessage.getText().toString();

                if (out != null) {
                    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    // Send the message to the client using the AsyncTask
                    new SendTask(out, messageToSend).execute();
                }

                // Append the sent message to the top EditText
                editTextServerChatMessages.append("You: " + messageToSend + "\n");

                // Clear the input EditText
                editTextServerMessage.setText("");
            });

            // Initialize socket communication with the server for received messages
            setupSocketForReceivedMessages(textViewReceived);

            dialog.show();
        });
    }

    private void setupSocketForReceivedMessages(TextView textViewReceived) {
        new Thread(() -> {

            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Reference to the "All Users" node in your Firebase Realtime Database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("All Users").child(userUid).child("Details");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("Fullname").exists() ? dataSnapshot.child("Fullname").getValue(String.class) : "";
                        String email = dataSnapshot.child("username").exists() ? dataSnapshot.child("username").getValue(String.class) : "";
                        String phoneNumber = dataSnapshot.child("PhoneNumber").exists() ? dataSnapshot.child("PhoneNumber").getValue(String.class) : "";

                        newme=fullName;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that occur
                }
            });


            try {
                while (serverRunning) {
                    Socket socket = srSocket.accept();
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("Connected to "+newme);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    while (true) {
                        String receivedText = in.readLine();
                        if (receivedText == null) {
                            break;
                        }

                        showReceivedTextPopupInServerPopup(receivedText);
                    }

                    in.close();
                    out.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).start();
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
}
