package com.example.battleship;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

public class ConnectionService extends Service {
    private String reference;
    private boolean listening = true;
    private boolean noBlock = false;

    public ConnectionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        HashMap<String, Object> connection = (HashMap<String, Object>)intent.getSerializableExtra("connection");
        String email = intent.getStringExtra("email");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("connections").add(connection);
        db.collection("connections")
                .whereEqualTo("sender", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ConnectionActivity.setConnectionStringText(document.getId());
//                                key.setText(document.getId());
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
                                                    sendMessageToActivity();
                                                    onDestroy();
                                                    listening = false;
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

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessageToActivity() {
        Intent intent = new Intent("ServiceFinish");
        intent.putExtra("reference", reference);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
