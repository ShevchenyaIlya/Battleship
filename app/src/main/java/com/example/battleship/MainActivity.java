package com.example.battleship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

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
    Spinner shipTypes;
    TextView pageTitle;
    Button rotateShip;

    Game game;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        game = new ViewModelProvider(this).get(Game.class);
        if (game.getYourField() == null) {
            game.setYourField(new BattleField());
            game.setOpponentField(new BattleField());
            db = FirebaseFirestore.getInstance();
            game.setUserConnection((UserConnection) getIntent().getSerializableExtra("UserConnection"));
            game.setConnectionId(getIntent().getStringExtra("ConnectionString"));

            Map<String, Object> data = game.getYourField().getFieldAsMap();
            data.put("Mode", "Processing");
            data.put("user", game.getUserConnection().getUser().getEmail());
            db.collection("collections")
                    .add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            game.setDocumentId(documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        }
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
                android.R.layout.simple_spinner_item, game.getYourField().shipsTypes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shipTypes.setAdapter(adapter);
    }

    private void setListener(Button button, final int i, final int j)
    {
        final Context context = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (game.getMode().equals("Create")) {
                    game.setSelectedShip(shipTypes.getSelectedItem().toString());
                    if (game.getYourField().setVessel(i, j, game.isHorizontalOrientation(), game.getSelectedShip())) {
                        updateField(game.getYourField(), true);

                        if (game.getYourField().isReady()) {
                            Map<String, Object> data = game.getYourField().getFieldAsMap();
                            data.put("Mode", "Ready");
                            db.collection("collections").document(game.getDocumentId()).update(data);
                            game.setMode("Ready");
                            pageTitle.setText(R.string.waiting_opponetn);

                            Map<String, Object> recipient = new HashMap<>();
                            recipient.put(game.getUserConnection().getUser().getStatus(), game.getMode());
                            db.collection("connections").document(game.getUserConnection().getConnectionId())
                                    .update(recipient);

                            final DocumentReference docRef = db.collection("connections").document(game.getUserConnection().getConnectionId());
                            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                    @Nullable FirebaseFirestoreException e) {
                                    if (snapshot != null && snapshot.exists()) {
                                        Map<String, Object> result = snapshot.getData();
                                        if (!result.isEmpty() && !result.get("recipient").equals("")) {
                                            if (result.size() == 4) {
                                                if (game.getUserConnection().getUser().getStatus().equals("Creator")) {
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
                else if (game.getMode().equals("Ready")) {
                    int attackResult = game.getOpponentField().opponentAttack(i, j);
                    if (attackResult == 1) {
                        view.setBackground(getResources().getDrawable(R.drawable.ic_baseline_close_24));
                    }
                    else if (attackResult == 0) {
                        view.setBackground(getResources().getDrawable(R.drawable.ic_baseline_adjust_24));
                        db.collection("collections").document(game.getOpponentId()).update(game.getOpponentField().getFieldAsMap());
                        pageTitle.setText(R.string.opponent_turn);
                        updateField(game.getYourField(), true);
                        game.setMode("Wait");
                    }

                    if (game.getOpponentField().isAllVesselsKilled()) {
                        game.setMode("Close");
                        stopGame(game.getUserConnection().getUser().getEmail(), game.getOpponentEmail(), "You won");
                        Map<String, Object> recipient = new HashMap<>();
                        recipient.put("Mode", game.getMode());
                        db.collection("collections").document(game.getOpponentId())
                                .update(recipient);
                        saveScore(game.getUserConnection().getUser().getEmail(), game.getOpponentEmail());

                    }
                }
            }
        });
    }

    private void stopGame(String winner, String looser, String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        pageTitle.setText(message);
        updateField(game.getOpponentField(), true);
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
        game.setOpponentEmail(opponent);
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
                                game.setOpponentId(reference);
                                game.getOpponentField().setFieldFromMap(result);
                            }
                            if (game.getUserConnection().getUser().getStatus().equals("Creator")) {
                                pageTitle.setText(R.string.your_turn);
                                updateField(game.getOpponentField(), false);
                                game.setMode("Ready");
                            }
                            else {
                                pageTitle.setText(R.string.opponent_turn);
                                game.setMode("Wait");
                            }
                            db.collection("collections")
                                    .whereEqualTo("user", game.getUserConnection().getUser().getEmail())
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
                                                                if (game.isGameStart()) {
                                                                    if (pageTitle.getText().toString().equals("Your turn")) {
                                                                        pageTitle.setText(R.string.opponent_turn);
                                                                        game.setMode("Wait");
                                                                    } else {
                                                                        pageTitle.setText(R.string.your_turn);
                                                                        updateField(game.getOpponentField(), false);
                                                                        game.setMode("Ready");
                                                                    }
                                                                }
                                                                updateYourField(internalReference);
                                                            }
                                                            else {
                                                                game.setMode("Close");
                                                                stopGame(game.getOpponentId(), game.getUserConnection().getUser().getEmail(), "You lost");
                                                            }
                                                            game.setGameStart(true);
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
                                game.getYourField().setFieldFromMap(map);
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
        game.setHorizontalOrientation(!game.isHorizontalOrientation());
        if (game.isHorizontalOrientation())
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
        if (!game.getDocumentId().equals("")) {
            db.collection("collections").document(game.getDocumentId())
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
            db.collection("connections").document(game.getConnectionId())
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
        intent.putExtra("UserConnection", game.getUserConnection());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}