package com.example.battleship;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    private int[][] fieldMatrix = new int[10][10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initButtons();
    }

//    @Override
//    public void onClick(View view) {
//        view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//        TableRow raw = (TableRow) view.getParent();
//        int id = raw.getId();
//        TableLayout table = (TableLayout) raw.getParent();
//
//    }

    private void initButtons() {
        TableLayout table = (TableLayout) findViewById(R.id.table);
        int rowCount = table.getChildCount();
        for (int i = 0; i < rowCount; i++)
        {
            TableRow row = (TableRow) table.getChildAt(i);
            int columnCount = row.getChildCount();
            for (int j = 0; j < columnCount; j++)
            {
                Button button = (Button) row.getChildAt(j);
                setListener(button, i, j);
            }
        }
    }

    private void setListener(Button button, final int i, final int j)
    {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int row = i;
                int column = j;
                if (fieldMatrix[row][column] != 1)
                    //view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    view.setBackground(getResources().getDrawable(R.drawable.ic_baseline_close_24));
                fieldMatrix[row][column] = 1;
            }
        });
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
}