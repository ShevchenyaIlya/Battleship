package com.example.battleship;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleField implements Serializable {
    private Integer[][] field = new Integer[10][10];

    private ArrayList<Integer> vesselCounts = new ArrayList<Integer>();

    public BattleField()
    {
        vesselCounts.add(4);
        vesselCounts.add(3);
        vesselCounts.add(2);
        vesselCounts.add(1);

        for (Integer[] row: field)
            Arrays.fill(row, 0);
    }

    public Integer[][] getField() {
        return field;
    }

    public void setField(Integer[][] field) {
        this.field = field;
    }

    public ArrayList<Integer> getVesselCounts() {
        return vesselCounts;
    }

    private ArrayList<Pair<Integer, Integer>> getBorderCellsHorizontal(int deckCount, int i, int j)
    {
        ArrayList<Pair<Integer, Integer>> borderCoordinates = new ArrayList<Pair<Integer, Integer>>();
        int[] array = {-1, 0, 1};
        for (int value: array)
        {
            borderCoordinates.add(new Pair<>(i + value, j - 1));
        }
        for (int value: array)
        {
            borderCoordinates.add(new Pair<>(i + value, j + deckCount));
        }

        for (int l = deckCount - 1; l >= 0; l--)
        {
            borderCoordinates.add(new Pair<>(i - 1, j + l));
            borderCoordinates.add(new Pair<>(i + 1, j + l));
        }
        return borderCoordinates;
    }

    private ArrayList<Pair<Integer, Integer>> getVesselCoordinates(int i, int j, int deckCount, boolean orientation)
    {
        ArrayList<Pair<Integer, Integer>> vesselCoordinates = new ArrayList<Pair<Integer, Integer>>();
        if (orientation) {
            for (int l = deckCount - 1; l >= 0; l--) {
                vesselCoordinates.add(new Pair<>(i, j + l));
            }
        }
        else {
            for (int l = deckCount - 1; l >= 0; l--) {
                vesselCoordinates.add(new Pair<>(i + l, j));
            }
        }
        return vesselCoordinates;
    }

    public boolean setVessel(int i, int j, boolean orientation, String vesselType)
    {
        ArrayList<Pair<Integer, Integer>> borderCoordinates;
        ArrayList<Pair<Integer, Integer>> vesselCoordinates;
        int deckNumber = 0;
        switch (vesselType)
        {
            case "Single-deck":
            {
                deckNumber = 1;
            }
            break;
            case "Double-deck":
            {
                deckNumber = 2;
            }
            break;
            case "Three-decker":
            {
                deckNumber = 3;
            }
            break;
            case "Four-decker":
            {
                deckNumber = 4;
            }
            break;
        }

        vesselCoordinates = getVesselCoordinates(i, j, deckNumber, orientation);
        if (orientation) {
            borderCoordinates = getBorderCellsHorizontal(deckNumber, i, j);
        }
        else {
            borderCoordinates = getBorderCellsVertical(deckNumber, i, j);
        }

        boolean fail = false;
        for (int l = 0; l < borderCoordinates.size(); l++)
        {
            if (!checkIfCorrect(borderCoordinates.get(l).first, borderCoordinates.get(l).second))
            {
                fail = true;
                break;
            }
        }

        if(!fail) {
            if (checkCorrectVesselPosition(vesselCoordinates) && checkCorrectVesselCount(deckNumber)) {
                for (Pair<Integer, Integer> coordinate : vesselCoordinates) {
                    field[coordinate.first][coordinate.second] = 1;
                }
                changeVesselCount(deckNumber);

                return true;
            }
        }
        return false;
    }

    private boolean checkCorrectVesselCount(int deckCount)
    {
        return vesselCounts.get(deckCount - 1) != 0;
    }

    public  boolean checkCell(int i, int j)
    {
        return field[i][j] == 1;
    }

    public int isAttacked(int i, int j)
    {
        return field[i][j];
    }

    private boolean checkIfCorrect(int i, int j)
    {
        if ((i >= 0 && i < 10) && (j >= 0 && j < 10)) {
            return field[i][j] != 1;
        }
        return true;
    }

    private boolean checkCorrectVesselPosition(ArrayList<Pair<Integer, Integer>> coordinates)
    {
        for (Pair<Integer, Integer> coordinate : coordinates) {
            if (!((coordinate.first >= 0 && coordinate.first < 10) &&
                    (coordinate.second >= 0 && coordinate.second < 10)))
                return false;
            if (field[coordinate.first][coordinate.second] == 1)
                return false;
        }
        return true;
    }

    public String[] shipsTypes()
    {
        return new String[] {
                "Single-deck", "Double-deck", "Three-decker", "Four-decker"
        };
    }

    private void changeVesselCount(Integer type) {
        vesselCounts.set(type - 1, vesselCounts.get(type - 1) - 1);
    }

    public boolean isReady()
    {
        for (int element: vesselCounts)
        {
            if (element != 0)
                return false;
        }
        return true;
    }

    public List<Integer> serialize(int index)
    {
        return Arrays.asList(field[index]);
    }

    public int opponentAttack(int i, int j)
    {
        if (field[i][j] == 1) {
            field[i][j] = 3;
            return 1;
        }
        else if (field[i][j] == 0) {
            field[i][j] = 2;
            return 0;
        }
        return 2;
    }

    public Map<String, Object> getFieldAsMap()
    {
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            data.put("row " + i, serialize(i));
        }
        return data;
    }

    public void setFieldFromMap(Map<String, Object> data)
    {
        for (int i = 0; i < 10; i++)
        {
            ArrayList<Long> row = (ArrayList<Long>)data.get("row " + i);
            for (int j = 0; j < 10; j++) {
                Integer value = row.get(j).intValue();
                field[i][j] = value;
            }
        }
    }

    public boolean isAllVesselsKilled()
    {
        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                if (field[i][j] == 1)
                    return false;
            }
        }
    return true;
    }

    private ArrayList<Pair<Integer, Integer>> getBorderCellsVertical(int deckCount, int i, int j)
    {
        ArrayList<Pair<Integer, Integer>> borderCoordinates = new ArrayList<Pair<Integer, Integer>>();
        int[] array = {-1, 0, 1};
        for (int value: array)
        {
            borderCoordinates.add(new Pair<>(i - 1, j + value));
        }
        for (int value: array)
        {
            borderCoordinates.add(new Pair<>(i + deckCount, j + value));
        }

        for (int l = deckCount - 1; l >= 0; l--)
        {
            borderCoordinates.add(new Pair<>(i + l, j - 1));
            borderCoordinates.add(new Pair<>(i + l, j + 1));
        }
        return borderCoordinates;
    }
}
