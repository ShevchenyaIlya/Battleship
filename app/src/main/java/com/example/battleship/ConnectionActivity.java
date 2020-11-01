package com.example.battleship;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ConnectionActivity extends AppCompatActivity {

    EditText key;
    String reference;
    FirebaseFirestore db;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        key = (EditText) findViewById(R.id.keyField);
        db = FirebaseFirestore.getInstance();
        user = (User)getIntent().getSerializableExtra("User");
    }

    public void createConnect(View view) {
        Map<String, Object> connection = new HashMap<>();
        connection.put("sender", user.getEmail());
        connection.put("recipient", "");
        final Context context = this;
        db.collection("connections").add(connection);
        db.collection("connections")
                .whereEqualTo("sender", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                key.setText(document.getId());
                                reference = document.getId();
                            }
                            final DocumentReference docRef = db.collection("connections").document(reference);
                            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                    @Nullable FirebaseFirestoreException e) {
                                    if (snapshot != null && snapshot.exists()) {
                                        Map<String, Object> result = snapshot.getData();
                                        if (!result.isEmpty() && !result.get("recipient").equals(""))
                                        {
                                            Intent intent = new Intent(context, MainActivity.class);
                                            intent.putExtra("User", user);
                                            context.startActivity(intent);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
    }

    public void Connect(View view) {
        String connectionString = key.getText().toString();
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("recipient", user.getEmail());

        db.collection("connections").document(connectionString)
                .update(recipient);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("User", user);
        this.startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("key", key.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        key.setText(savedInstanceState.getString("key"));
    }
}
