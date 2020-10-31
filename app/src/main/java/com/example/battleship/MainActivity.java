package com.example.battleship;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.ColorSpace;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private BattleField yourField;
    private BattleField opponentField;
    Spinner shipTypes;
    TextView pageTitle;
    Button rotateShip;

    private boolean horizontalOrientation = true;
    private String selectedShip = "Four-decker";
    private String mode = "Create";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        yourField = new BattleField();
        opponentField = new BattleField();
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