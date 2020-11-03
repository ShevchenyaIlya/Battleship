package com.example.battleship;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ConnectionActivity extends AppCompatActivity {

    private String reference;
    private EditText key;
    private FirebaseFirestore db;
    private boolean listening = true;
    private boolean noBlock = false;
    private User user;

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
                                final ListenerRegistration registration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                        @Nullable FirebaseFirestoreException e) {
                                        if (listening) {
                                            if (noBlock) {
                                                if (snapshot != null && snapshot.exists()) {
                                                    Map<String, Object> result = snapshot.getData();
                                                    if (!result.isEmpty() && !result.get("recipient").equals("")) {
                                                        Intent intent = new Intent(context, MainActivity.class);
                                                        user.setStatus("Creator");
                                                        user.setConnectionId(reference);
                                                        intent.putExtra("User", user);
                                                        listening = false;
                                                        context.startActivity(intent);
                                                    }
                                                }
                                            }
                                            noBlock = true;
                                        }
                                    }
                                });
                            }
                        }
                });
    }

    public void Connect(View view) {
        final String connectionString = key.getText().toString();
        final Map<String, Object> recipient = new HashMap<>();
        recipient.put("recipient", user.getEmail());
        final Context context = this;
        if (!connectionString.equals("")) {
            db.collection("connections")
                    .document(connectionString)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    db.collection("connections").document(connectionString)
                                            .update(recipient);
                                    Intent intent = new Intent(context, MainActivity.class);
                                    user.setStatus("Connected");
                                    user.setConnectionId(connectionString);
                                    listening = false;
                                    intent.putExtra("User", user);
                                    intent.putExtra("ConnectionString", document.getId());
                                    context.startActivity(intent);
                                }
                                else
                                {
                                    Toast.makeText(context, "Invalid connection string", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

        }
        else
            Toast.makeText(this, "Enter connection string", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        menu.add("Profile");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("User", user);
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();
        startActivityForResult(intent, 1);
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        user = (User) data.getSerializableExtra("User");
    }


}
