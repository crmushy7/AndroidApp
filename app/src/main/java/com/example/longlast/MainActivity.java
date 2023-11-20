package com.example.longlast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button login;
    LinearLayout loginlay,registratinlay;
    TextView textView,registerpage,loginpage;
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
}