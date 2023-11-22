package com.example.longlast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Button login;
    LinearLayout loginlay,registratinlay;
    TextView textView,registerpage,loginpage;
    Button registerBtn;
    EditText fullName;
    EditText emailreg,emaillog,passlog;
    EditText phone;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    EditText pswd, cpswd;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login=findViewById(R.id.loginbutton);
        textView=findViewById(R.id.title);
        loginlay=findViewById(R.id.loginlayout);
        registratinlay=findViewById(R.id.registerLayout);
        registerpage=findViewById(R.id.donthaveacount);
        loginpage=findViewById(R.id.tologinpage);
        //-------------------------Register Variables---------------------------//
        registerBtn = findViewById(R.id.registerBtn);
        fullName = findViewById(R.id.fullName);
        emailreg = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        pswd = findViewById(R.id.pswd);
        cpswd = findViewById(R.id.cPswd);
        emaillog=findViewById(R.id.emaillogin);
        passlog=findViewById(R.id.passwdlog);
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();
        firebaseAuth=FirebaseAuth.getInstance();

        loginpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registratinlay.setVisibility(View.GONE);
                loginlay.setVisibility(View.VISIBLE);

            }
        });


        registerpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginlay.setVisibility(View.GONE);
                registratinlay.setVisibility(View.VISIBLE);

            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emaillog.getText().toString().trim();
                String password=passlog.getText().toString().trim();
                if (email.length()==0){
                    emaillog.setError("Write your email");
                } else if (password.length()==0) {
                    passlog.setError("fill password");
                }
                else{
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        emaillog.setText("");
                                        passlog.setText("");
                                        Toast.makeText(MainActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(MainActivity.this,Homepage.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(MainActivity.this, "Failed to log in", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }


            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserRegister();
            }
        });
    }

    public void onUserRegister(){
        DatabaseSupport databaseSupport = new DatabaseSupport(this,"msomali.db");

        if (fullName.getText().equals("")||emailreg.getText().equals("")||phone.getText().equals("")||pswd.getText().equals("")||cpswd.getText().equals("")){
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_LONG).show();
        }else {
            if (pswd.getText().toString().length()<6){
                pswd.setError("Fill a strong password ,atleast 6 characters");
            } else if (phone.getText().toString().length()!=10) {
                phone.setError("Must be 10 numbers only");
            }else {
                UserRecords userRecords = new UserRecords(0,fullName.getText().toString().trim(),emailreg.getText().toString().trim(),phone.getText().toString().trim(),pswd.getText().toString().trim());

                if(pswd.getText().toString().trim().equals(cpswd.getText().toString().trim())){
                    boolean addUser = databaseSupport.addUser(userRecords);
                    String email=emailreg.getText().toString().trim();
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("Fullname ",fullName.getText().toString().trim());
                    hashMap.put("username ",email);
                    hashMap.put("PhoneNumber ",phone.getText().toString().trim());
                    hashMap.put("Password ",pswd.getText().toString().trim());


                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        // Handle the case where the email is badly formatted
                        emailreg.setError("Invalid email");
//                    Toast.makeText(this, "Invalid email address format", Toast.LENGTH_SHORT).show();
                    }else{
                        firebaseAuth.createUserWithEmailAndPassword(email, pswd.getText().toString())
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {


                                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                            // Your database operations here
                                            databaseReference.child("All Users")
                                                    .child(fullName.getText().toString().trim()+phone.getText().toString())
                                                    .setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                            if (addUser){
                                                Toast.makeText(MainActivity.this, "User Registered!", Toast.LENGTH_LONG).show();
                                                fullName.setText("");
                                                emailreg.setText("");
                                                phone.setText("");
                                                pswd.setText("");
                                                cpswd.setText("");
                                                registratinlay.setVisibility(View.GONE);
                                                loginlay.setVisibility(View.VISIBLE);
                                            } else {
                                                Toast.makeText(MainActivity.this, "Fail! User not registered!", Toast.LENGTH_LONG).show();
                                            }
                                        } else {

                                            // If sign in fails, check the exception and handle specific errors
                                            if (task.getException() != null && task.getException() instanceof FirebaseAuthException) {
                                                FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                                                String errorCode = firebaseAuthException.getErrorCode();
                                                Toast.makeText(MainActivity.this, errorCode, Toast.LENGTH_SHORT).show();
                                                emailreg.setError("Email already in use");
                                            }
                                        }
                                    }
                                });



                    }
                }
                else {
                    Toast.makeText(this, "Password does not Match!", Toast.LENGTH_LONG).show();
                }
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            Intent intent=new Intent(MainActivity.this, Homepage.class);
            startActivity(intent);
            finish();
        }
    }
}