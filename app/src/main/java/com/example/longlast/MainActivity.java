package com.example.longlast;

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

public class MainActivity extends AppCompatActivity {
    Button login;
    LinearLayout loginlay,registratinlay;
    TextView textView,registerpage,loginpage;
    Button registerBtn;
    EditText fullName;
    EditText email;
    EditText phone;
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
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        pswd = findViewById(R.id.pswd);
        cpswd = findViewById(R.id.cPswd);

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
                Intent intent=new Intent(MainActivity.this,Homepage.class);
                startActivity(intent);
            }
        });
    }
    public void onUserRegister(View view){
        DatabaseSupport databaseSupport = new DatabaseSupport(this,"msomali.db");

        if (fullName.getText().equals("")||email.getText().equals("")||phone.getText().equals("")||pswd.getText().equals("")||cpswd.getText().equals("")){
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_LONG).show();
        }else {
            UserRecords userRecords = new UserRecords(0,fullName.getText().toString(),email.getText().toString(),phone.getText().toString(),pswd.getText().toString());

            if(pswd.getText().toString().equals(cpswd.getText().toString())){
                boolean addUser = databaseSupport.addUser(userRecords);
                if (addUser){
                    Toast.makeText(this, "User Registered!", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(this, "Fail! User not registered!", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(this, "Password does not Match!", Toast.LENGTH_LONG).show();
            }
        }

    }
}