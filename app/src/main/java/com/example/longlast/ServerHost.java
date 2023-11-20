//package com.example.longlast;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AlertDialog;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//public class ServerHost extends Thread {
//    private ServerSocket srSocket;
//    private boolean serverRunning;
//    private Handler handler; // Handler for posting messages to the main thread
//    private Context context;
//    private PrintWriter out; // PrintWriter to send messages to the client
//
//    public ServerHost(Context context) {
//        this.context = context;
//        handler = new Handler(Looper.getMainLooper());
//    }
//
//    public PrintWriter getOut() {
//        return out;
//    }
//
//    public void startServer() {
//        serverRunning = true;
//        start();
//    }
//
//    @Override
//    public void run() {
//        try {
//            srSocket = new ServerSocket(1234);
//
//            while (serverRunning) {
//                Socket socket = srSocket.accept();
//
//                // Set up PrintWriter here
//                out = new PrintWriter(socket.getOutputStream(), true);
//
//                // Send an initial message to the client
//                out.println("Hello from the server!");
//
//                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                // Continuously listen for messages from the client
//                while (true) {
//                    String receivedText = in.readLine();
//                    if (receivedText == null) {
//                        break;
//                    }
//
//                    // Process the received message
//                    handler.post(() -> showReceivedTextPopup(receivedText));
//                }
//
//                in.close();
//                out.close();
//                socket.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void showReceivedTextPopup(String receivedText) {
//        handler.post(() -> createAndShowDialog(receivedText));
//    }
//
//    private void createAndShowDialog(String receivedText) {
//        // Use the receivedText to update UI elements (e.g., TextView, AlertDialog)
//        // For example, you can create an AlertDialog or update a TextView here
//
//        // Assuming you have a TextView with the id "textViewReceivedText" in your layout
//        View popupView = LayoutInflater.from(context).inflate(R.layout.server_popup_layout, null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(popupView);
//        AlertDialog dialog = builder.create();
//
//        // Get references to UI components in the pop-up dialog
//        TextView textViewReceived = popupView.findViewById(R.id.textViewReceivedText);
//        EditText editTextServerMessage = popupView.findViewById(R.id.editTextServerMessage);
//        Button buttonSendServerMessage = popupView.findViewById(R.id.buttonSendServerMessage);
//
//        // Set the received text to the TextView
//        textViewReceived.setText("Received Text: " + receivedText);
//
//        // Set up click listener for the Send button
//        buttonSendServerMessage.setOnClickListener(v -> {
//            String messageToSend = editTextServerMessage.getText().toString();
//
//            // Check if the serverHost has a valid PrintWriter
//            if (out != null) {
//                // Send the message to the client using the AsyncTask
//                new SendTask(out, messageToSend).execute();
//            }
//
//            // Update the UI or perform other actions as needed
//            //            dialog.dismiss();
//        });
//
//        // Show the pop-up dialog
//        dialog.show();
//    }
//
//
//    public class SendTask extends AsyncTask<Void, Void, Void> {
//        private final PrintWriter out;
//        private final String message;
//
//        // Constructor to receive PrintWriter and message
//        public SendTask(PrintWriter out, String message) {
//            this.out = out;
//            this.message = message;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            // Perform network operations in the background
//            if (out != null) {
//                // Send the message to the client using the PrintWriter
//                out.println(message);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            // Handle any post-execution tasks if needed
//        }
//    }
//}
package com.example.longlast;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerHost extends Thread {
    private ServerSocket srSocket;
    private boolean serverRunning;
    private Handler handler; // Handler for posting messages to the main thread
    private Context context;
    private PrintWriter out; // PrintWriter to send messages to the client

    public ServerHost(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
    }

    public PrintWriter getOut() {
        return out;
    }

    public void startServer() {
        serverRunning = true;
        start();
    }

    @Override
    public void run() {
        try {
            srSocket = new ServerSocket(1234);

            while (serverRunning) {
                Socket socket = srSocket.accept();

                // Set up PrintWriter here
                out = new PrintWriter(socket.getOutputStream(), true);

                // Send an initial message to the client
                out.println("Hello from the server!");

                // Show the popup after the connection is established
                showServerPopup();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Continuously listen for messages from the client
                while (true) {
                    String receivedText = in.readLine();
                    if (receivedText == null) {
                        break;
                    }

                    // Process the received message
                    handler.post(() -> showReceivedTextPopup(receivedText));
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

    private void showReceivedTextPopup(String receivedText) {
        handler.post(() -> createAndShowDialog(receivedText));
    }

    private void createAndShowDialog(String receivedText) {
        // Use the receivedText to update UI elements (e.g., TextView, AlertDialog)
        // For example, you can create an AlertDialog or update a TextView here

        // Assuming you have a TextView with the id "textViewReceivedText" in your layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.server_popup_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        // Get references to UI components in the pop-up dialog
        TextView textViewReceived = popupView.findViewById(R.id.textViewReceivedText);
        EditText editTextServerMessage = popupView.findViewById(R.id.editTextServerMessage);
        Button buttonSendServerMessage = popupView.findViewById(R.id.buttonSendServerMessage);

        // Set the received text to the TextView
        textViewReceived.setText("Received Text: " + receivedText);

        // Set up click listener for the Send button
        buttonSendServerMessage.setOnClickListener(v -> {
            String messageToSend = editTextServerMessage.getText().toString();

            // Check if the serverHost has a valid PrintWriter
            if (out != null) {
                // Send the message to the client using the AsyncTask
                new SendTask(out, messageToSend).execute();
            }

            // Update the UI or perform other actions as needed
            //            dialog.dismiss();
        });

        // Show the pop-up dialog
        dialog.show();
    }

    // ...

    private void showServerPopup() {
        handler.post(() -> {
            // Create an AlertDialog with the look of server_popup_layout
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View popupView = LayoutInflater.from(context).inflate(R.layout.server_popup_layout, null);

            // Get references to UI components in the pop-up dialog
            TextView textViewReceived = popupView.findViewById(R.id.textViewReceivedText);
            EditText editTextServerMessage = popupView.findViewById(R.id.editTextServerMessage);
            Button buttonSendServerMessage = popupView.findViewById(R.id.buttonSendServerMessage);

            // Customize the AlertDialog
            builder.setView(popupView);
            AlertDialog dialog = builder.create();

            // Set up click listener for the Send button
            buttonSendServerMessage.setOnClickListener(v -> {
                String messageToSend = editTextServerMessage.getText().toString();

                // Check if the serverHost has a valid PrintWriter
                if (out != null) {
                    // Send the message to the client using the AsyncTask
                    new SendTask(out, messageToSend).execute();
                }

                // Update the UI or perform other actions as needed
                dialog.dismiss(); // Dismiss the dialog after sending the message
            });

            // Show the AlertDialog
            dialog.show();
        });
    }

// ...


    public class SendTask extends AsyncTask<Void, Void, Void> {
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
            if (out != null) {
                // Send the message to the client using the PrintWriter
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
