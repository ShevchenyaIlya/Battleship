package com.example.battleship;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    final String TAG = "LoginActivity";
    FirebaseFirestore db;
    EditText login, password;
    LoginViewModel inputData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputData = new ViewModelProvider(this).get(LoginViewModel.class);
        db = FirebaseFirestore.getInstance();
        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);
    }

    public void loginClick(View view) {
        inputData.setEmail(login.getText().toString());
        inputData.setPassword(password.getText().toString());
        if (!inputData.getEmail().equals("") && !inputData.getPassword().equals("")) {
            if (inputData.getPassword().length() >= 6 && inputData.getEmail().contains("@")) {
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

                                        if (result.get("nickname") != null)
                                            user.setNickname(result.get("nickname").toString());

                                        if (result.get("imageUrl") != null)
                                            user.setImageUrl(result.get("imageUrl").toString());

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
                                    else
                                    {
                                        Toast.makeText(context, "Wrong password for this email.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
            else
                Toast.makeText(this, "Short password or wrong email.", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "Fill all field.", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        inputData.setEmail(login.getText().toString());
        inputData.setPassword(password.getText().toString());
        super.onDestroy();
    }
}