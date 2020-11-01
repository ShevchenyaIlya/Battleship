package com.example.battleship;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db;
    EditText login, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = FirebaseFirestore.getInstance();
        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);
    }

    public void loginClick(View view) {
        if (!login.getText().toString().equals("") && !password.getText().toString().equals(""))
        {
            User user = new User(login.getText().toString(), password.getText().toString());
            db.collection("users").add(user);
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }
    }
}