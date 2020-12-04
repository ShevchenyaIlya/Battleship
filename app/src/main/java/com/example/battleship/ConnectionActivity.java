package com.example.battleship;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ConnectionActivity extends AppCompatActivity {

    private boolean listening = true;
    private static EditText key;
    private FirebaseFirestore db;

    private UserConnection userConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        key = (EditText) findViewById(R.id.keyField);
        db = FirebaseFirestore.getInstance();
        userConnection = new UserConnection((User)getIntent().getSerializableExtra("User"), "");
    }

    public void createConnect(View view) {
        HashMap<String, Object> connection = new HashMap<>();
        connection.put("sender", userConnection.getUser().getEmail());
        connection.put("recipient", "");
        Intent intent = new Intent(this, ConnectionService.class);
        intent.putExtra("connection", connection).putExtra("email", userConnection.getUser().getEmail());
        this.startService(intent);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("ServiceFinish"));
    }

    public void Connect(View view) {
        final String connectionString = key.getText().toString();
        final Map<String, Object> recipient = new HashMap<>();
        recipient.put("recipient", userConnection.getUser().getEmail());
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
                                    userConnection.getUser().setStatus("Connected");
                                    userConnection.setConnectionId(connectionString);
                                    listening = false;
                                    intent.putExtra("UserConnection", userConnection);
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
        intent.putExtra("UserConnection", userConnection);
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
        userConnection = (UserConnection) data.getSerializableExtra("UserConnection");
    }

    public static void setConnectionStringText(String text) {
        key.setText(text);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String reference = intent.getStringExtra("reference");
            Intent newIntent = new Intent(context, MainActivity.class);
            userConnection.getUser().setStatus("Creator");
            userConnection.setConnectionId(reference);
            newIntent.putExtra("UserConnection", userConnection);
            context.startActivity(newIntent);
        }
    };

}
