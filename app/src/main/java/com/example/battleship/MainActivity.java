package com.example.battleship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MainActivity extends AppCompatActivity{
    private final String TAG = "MainActivity";
    private BattleField yourField;
    private BattleField opponentField;
    Spinner shipTypes;
    TextView pageTitle;
    Button rotateShip;

    private boolean horizontalOrientation = true;
    private String selectedShip = "Four-decker";
    private String mode = "Create";
    private String documentId;
    private String connectionId;

    FirebaseFirestore db;
    User user;
    String opponentEmail;
    String opponentId;
    boolean gameStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        yourField = new BattleField();
        opponentField = new BattleField();
        db = FirebaseFirestore.getInstance();
        user = (User)getIntent().getSerializableExtra("User");
        connectionId = getIntent().getStringExtra("ConnectionString");

        Map<String, Object> data = yourField.getFieldAsMap();
        data.put("Mode", "Processing");
        data.put("user", user.getEmail());
        db.collection("collections")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        documentId = documentReference.getId();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
        initButtons();
    }

    private void initButtons() {
        shipTypes = (Spinner) findViewById(R.id.ShipType);
        pageTitle = (TextView) findViewById(R.id.PageTitle);
        rotateShip = (Button) findViewById(R.id.RotateButton);
        initSpinner();

        TableLayout table = (TableLayout) findViewById(R.id.table);
        int rowCount = table.getChildCount();
        for (int i = 1; i < rowCount - 1; i++)
        {
            TableRow row = (TableRow) table.getChildAt(i);
            int columnCount = row.getChildCount();
            for (int j = 0; j < columnCount; j++)
            {
                Button button = (Button) row.getChildAt(j);
                setListener(button, i - 1, j);
            }
        }
    }

    private void initSpinner() {
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, yourField.shipsTypes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shipTypes.setAdapter(adapter);
    }

    private void setListener(Button button, final int i, final int j)
    {
        final Context context = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode.equals("Create")) {
                    selectedShip = shipTypes.getSelectedItem().toString();
                    if (yourField.setVessel(i, j, horizontalOrientation, selectedShip)) {
                        updateField(yourField, true);

                        if (yourField.isReady()) {
                            Map<String, Object> data = yourField.getFieldAsMap();
                            data.put("Mode", "Ready");
                            db.collection("collections").document(documentId).update(data);
                            mode = "Ready";
                            pageTitle.setText(R.string.waiting_opponetn);

                            Map<String, Object> recipient = new HashMap<>();
                            recipient.put(user.getStatus(), mode);
                            db.collection("connections").document(user.getConnectionId())
                                    .update(recipient);

                            final DocumentReference docRef = db.collection("connections").document(user.getConnectionId());
                            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                    @Nullable FirebaseFirestoreException e) {
                                    if (snapshot != null && snapshot.exists()) {
                                        Map<String, Object> result = snapshot.getData();
                                        if (!result.isEmpty() && !result.get("recipient").equals("")) {
                                            if (result.size() == 4) {
                                                if (user.getStatus().equals("Creator")) {
                                                    loadOpponentField(result.get("recipient").toString());
                                                }
                                                else {
                                                    loadOpponentField(result.get("sender").toString());
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            changeActivityState();
                        }
                    }
                }
                else if (mode.equals("Ready")) {
                    int attackResult = opponentField.opponentAttack(i, j);
                    if (attackResult == 1) {
                        view.setBackground(getResources().getDrawable(R.drawable.ic_baseline_close_24));
                    }
                    else if (attackResult == 0) {
                        view.setBackground(getResources().getDrawable(R.drawable.ic_baseline_adjust_24));
                        db.collection("collections").document(opponentId).update(opponentField.getFieldAsMap());
                        pageTitle.setText(R.string.opponent_turn);
                        updateField(yourField, true);
                        mode = "Wait";
                    }

                    if (opponentField.isAllVesselsKilled()) {
                        mode = "Close";
                        stopGame(user.getEmail(), opponentEmail, "You won");
                        Map<String, Object> recipient = new HashMap<>();
                        recipient.put("Mode", mode);
                        db.collection("collections").document(opponentId)
                                .update(recipient);
                        saveScore(user.getEmail(), opponentEmail);

                    }
                }
            }
        });
    }

    private void stopGame(String winner, String looser, String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        pageTitle.setText(message);
        updateField(opponentField, true);
    }

    private void saveScore(String winner, String looser)
    {
        Map<String, Object> score = new HashMap<>();
        score.put("winner", winner);
        score.put("looser", looser);
        db.collection("scores").add(score);
    }

    private void loadOpponentField(final String opponent)
    {
        opponentEmail = opponent;
        final Context context = this;
        db.collection("collections")
                .whereEqualTo("user", opponent)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String reference = "";
                        if (task.isSuccessful()) {
                            Map<String, Object> result;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                result = document.getData();
                                reference = document.getId();
                                opponentId = reference;
                                opponentField.setFieldFromMap(result);
                            }
                            if (user.getStatus().equals("Creator")) {
                                pageTitle.setText(R.string.your_turn);
                                updateField(opponentField, false);
                                mode = "Ready";
                            }
                            else {
                                pageTitle.setText(R.string.opponent_turn);
                                mode = "Wait";
                            }
                            db.collection("collections")
                                    .whereEqualTo("user", user.getEmail())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                String referenceInternal = "";
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    referenceInternal = document.getId();
                                                }
                                                final DocumentReference docRef = db.collection("collections").document(referenceInternal);
                                                final String internalReference = referenceInternal;
                                                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                                        @Nullable FirebaseFirestoreException e) {
                                                        if (snapshot != null && snapshot.exists()) {
                                                            Map<String, Object> result = snapshot.getData();
                                                            if (!result.get("Mode").equals("Close")) {
                                                                if (gameStart) {
                                                                    if (pageTitle.getText().toString().equals("Your turn")) {
                                                                        pageTitle.setText(R.string.opponent_turn);
                                                                        mode = "Wait";
                                                                    } else {
                                                                        pageTitle.setText(R.string.your_turn);
                                                                        updateField(opponentField, false);
                                                                        mode = "Ready";
                                                                    }
                                                                }
                                                            updateYourField(internalReference);
                                                            }
                                                            else {
                                                                mode = "Close";
                                                                stopGame(opponentId, user.getEmail(), "You lost");
                                                            }
                                                            gameStart = true;
                                                        }
                                                    }
                                                });
                                            }

                                        }
                                    });

                        }
                    }
                });
    }

    private void updateYourField(String reference)
    {
        db.collection("collections")
                .document(reference)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> map = document.getData();
                                yourField.setFieldFromMap(map);
                            }
                        }
                    }
                });
    }

    private void updateField(BattleField field, boolean mode)
    {
        TableLayout table = (TableLayout) findViewById(R.id.table);
        int rowCount = table.getChildCount();
        for (int i = 1; i < rowCount - 1; i++)
        {
            TableRow row = (TableRow) table.getChildAt(i);
            int columnCount = row.getChildCount();
            for (int j = 0; j < columnCount; j++)
            {
                Button button = (Button) row.getChildAt(j);
                int primaryResult = field.isAttacked(i - 1, j);
                if (mode) {
                    if (primaryResult == 3)
                        button.setBackgroundColor(Color.RED);
                    else if (primaryResult == 2)
                        button.setBackground(getResources().getDrawable(R.drawable.ic_baseline_adjust_24));
                    else if (primaryResult == 1)
                        button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    else
                        button.setBackgroundColor(Color.TRANSPARENT);
                }
                else {
                    if (primaryResult == 3)
                        button.setBackground(getResources().getDrawable(R.drawable.ic_baseline_close_24));
                    else if (primaryResult == 2)
                        button.setBackground(getResources().getDrawable(R.drawable.ic_baseline_adjust_24));
                    else
                        button.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }

    public void rotateClick(View view) {
        horizontalOrientation = !horizontalOrientation;
        if (horizontalOrientation)
            rotateShip.setText(getResources().getString(R.string.horizontal));
        else
            rotateShip.setText(getResources().getString(R.string.vertical));
    }


    public void changeActivityState() {
        shipTypes.setVisibility(View.INVISIBLE);
        rotateShip.setVisibility(View.INVISIBLE);
        pageTitle.setText(R.string.ready_state);
    }

    @Override
    protected void onDestroy() {
        if (!documentId.equals("")) {
            db.collection("collections").document(documentId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });
            db.collection("connections").document(connectionId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        intent.putExtra("User", user);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}