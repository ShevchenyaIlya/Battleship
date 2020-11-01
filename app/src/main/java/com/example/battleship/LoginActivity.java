package com.example.battleship;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

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
        if (!login.getText().toString().equals("") && !password.getText().toString().equals("")) {
            if (password.getText().toString().length() >= 6 && login.getText().toString().contains("@")) {
                final String TAG = "-----------";
                final User user = new User(login.getText().toString(), password.getText().toString());
                final Context context = this;
                db.collection("users")
                        .whereEqualTo("email", user.getEmail())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                boolean flag = true;
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Map<String, Object> result = document.getData();
                                        if (result.containsValue(user.getEmail())) {
                                            if (!result.get("password").equals(user.getPassword()))
                                                flag = false;
                                        }
                                    }
                                    if (task.getResult().isEmpty()) {
                                        db.collection("users").add(user);
                                    }
                                    if (flag) {
                                        Intent intent = new Intent(context, ConnectionActivity.class);
                                        intent.putExtra("User", user);
                                        context.startActivity(intent);
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("login", login.getText().toString());
        outState.putString("password", password.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        login.setText(savedInstanceState.getString("login"));
        password.setText(savedInstanceState.getString("password"));
    }
}