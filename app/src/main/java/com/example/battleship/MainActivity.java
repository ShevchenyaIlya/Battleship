package com.example.battleship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private BattleField yourField;
    private BattleField opponentField;
    Spinner shipTypes;
    TextView pageTitle;
    Button rotateShip;

    private boolean horizontalOrientation = true;
    private String selectedShip = "Four-decker";
    private String mode = "Create";

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        yourField = new BattleField();
        opponentField = new BattleField();
        db = FirebaseFirestore.getInstance();
        db.collection("collections")
                .add(yourField.serialize())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("-----", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("------", "Error adding document", e);
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode.equals("Create"))
                    selectedShip = shipTypes.getSelectedItem().toString();
                    if(yourField.setVessel(i, j, horizontalOrientation, selectedShip)) {
                        updateField();

                        if (yourField.isReady()) {
                            mode = "Ready";
                            changeActivityState();
                        }
                    }
            }
        });
    }

    private void updateField()
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
                if (yourField.checkCell(i - 1, j))
                    button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        menu.add("Profile");
        menu.add("Options");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }

    public void rotateClick(View view) {
        horizontalOrientation = !horizontalOrientation;
        if (horizontalOrientation)
            rotateShip.setText(getResources().getString(R.string.horizontal));
        else
            rotateShip.setText(getResources().getString(R.string.vertical));
    }


    public void changeActivityState() {
        shipTypes.setVisibility(View.GONE);
        rotateShip.setVisibility(View.GONE);
        pageTitle.setText(R.string.ready_state);
    }

}