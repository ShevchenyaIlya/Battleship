package com.example.battleship;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BattleField implements Serializable {
    private int[][] field = new int[10][10];

    private ArrayList<Integer> vesselCounts = new ArrayList<Integer>();

    public BattleField()
    {
        vesselCounts.add(4);
        vesselCounts.add(3);
        vesselCounts.add(2);
        vesselCounts.add(1);
    }

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }

    public ArrayList<Integer> getVesselCounts() {
        return vesselCounts;
    }

    public boolean setVessel(int i, int j, boolean orientation, String vesselType)
    {
        ArrayList<Pair<Integer, Integer>> borderCoordinates = new ArrayList<Pair<Integer, Integer>>();
        ArrayList<Pair<Integer, Integer>> vesselCoordinates = new ArrayList<Pair<Integer, Integer>>();
        int deckNumber = 0;
        switch (vesselType)
        {
            case "Single-deck":
            {
                vesselCoordinates.add(new Pair<>(i, j));
                deckNumber = 1;

                borderCoordinates.add(new Pair<>(i + 1, j + 1));
                borderCoordinates.add(new Pair<>(i + 1, j));
                borderCoordinates.add(new Pair<>(i + 1, j - 1));
                borderCoordinates.add(new Pair<>(i, j - 1));
                borderCoordinates.add(new Pair<>(i, j + 1));
                borderCoordinates.add(new Pair<>(i - 1, j + 1));
                borderCoordinates.add(new Pair<>(i - 1, j));
                borderCoordinates.add(new Pair<>(i - 1, j - 1));
            }
            break;
            case "Double-deck":
            {
                deckNumber = 2;
                if (orientation) {
                    vesselCoordinates.add(new Pair<>(i, j));
                    vesselCoordinates.add(new Pair<>(i, j + 1));

                    borderCoordinates.add(new Pair<>(i + 1, j + 2));
                    borderCoordinates.add(new Pair<>(i + 1, j + 1));
                    borderCoordinates.add(new Pair<>(i + 1, j));
                    borderCoordinates.add(new Pair<>(i + 1, j - 1));
                    borderCoordinates.add(new Pair<>(i, j - 1));
                    borderCoordinates.add(new Pair<>(i, j + 2));

                    borderCoordinates.add(new Pair<>(i - 1, j + 2));
                    borderCoordinates.add(new Pair<>(i - 1, j + 1));
                    borderCoordinates.add(new Pair<>(i - 1, j));
                    borderCoordinates.add(new Pair<>(i - 1, j - 1));
                }
                else {
                    vesselCoordinates.add(new Pair<>(i, j));
                    vesselCoordinates.add(new Pair<>(i + 1, j));

                    borderCoordinates.add(new Pair<>(i - 1, j + 1));
                    borderCoordinates.add(new Pair<>(i - 1, j));
                    borderCoordinates.add(new Pair<>(i - 1, j - 1));
                    borderCoordinates.add(new Pair<>(i, j - 1));
                    borderCoordinates.add(new Pair<>(i, j + 1));
                    borderCoordinates.add(new Pair<>(i + 1, j - 1));
                    borderCoordinates.add(new Pair<>(i + 1, j + 1));

                    borderCoordinates.add(new Pair<>(i + 2, j + 1));
                    borderCoordinates.add(new Pair<>(i + 2, j));
                    borderCoordinates.add(new Pair<>(i + 2, j - 1));
                }
            }
            break;
            case "Three-decker":
            {
                deckNumber = 3;
                if (orientation) {
                    vesselCoordinates.add(new Pair<>(i, j));
                    vesselCoordinates.add(new Pair<>(i, j + 1));
                    vesselCoordinates.add(new Pair<>(i, j + 2));

                    borderCoordinates.add(new Pair<>(i + 1, j + 3));
                    borderCoordinates.add(new Pair<>(i + 1, j + 2));
                    borderCoordinates.add(new Pair<>(i + 1, j + 1));
                    borderCoordinates.add(new Pair<>(i + 1, j));
                    borderCoordinates.add(new Pair<>(i + 1, j - 1));

                    borderCoordinates.add(new Pair<>(i, j - 1));
                    borderCoordinates.add(new Pair<>(i, j + 3));

                    borderCoordinates.add(new Pair<>(i - 1, j + 3));
                    borderCoordinates.add(new Pair<>(i - 1, j + 2));
                    borderCoordinates.add(new Pair<>(i - 1, j + 1));
                    borderCoordinates.add(new Pair<>(i - 1, j));
                    borderCoordinates.add(new Pair<>(i - 1, j - 1));
                }
                else {
                    vesselCoordinates.add(new Pair<>(i, j));
                    vesselCoordinates.add(new Pair<>(i + 1, j));
                    vesselCoordinates.add(new Pair<>(i + 2, j));

                    borderCoordinates.add(new Pair<>(i - 1, j + 1));
                    borderCoordinates.add(new Pair<>(i - 1, j));
                    borderCoordinates.add(new Pair<>(i - 1, j - 1));
                    borderCoordinates.add(new Pair<>(i, j - 1));
                    borderCoordinates.add(new Pair<>(i, j + 1));
                    borderCoordinates.add(new Pair<>(i + 1, j - 1));
                    borderCoordinates.add(new Pair<>(i + 1, j + 1));
                    borderCoordinates.add(new Pair<>(i + 2, j - 1));
                    borderCoordinates.add(new Pair<>(i + 2, j + 1));

                    borderCoordinates.add(new Pair<>(i + 3, j + 1));
                    borderCoordinates.add(new Pair<>(i + 3, j));
                    borderCoordinates.add(new Pair<>(i + 3, j - 1));
                }
            }
            break;
            case "Four-decker":
            {
                deckNumber = 4;
                if (orientation) {
                    vesselCoordinates.add(new Pair<>(i, j));
                    vesselCoordinates.add(new Pair<>(i, j + 1));
                    vesselCoordinates.add(new Pair<>(i, j + 2));
                    vesselCoordinates.add(new Pair<>(i, j + 3));

                    borderCoordinates.add(new Pair<>(i + 1, j + 4));
                    borderCoordinates.add(new Pair<>(i + 1, j + 3));
                    borderCoordinates.add(new Pair<>(i + 1, j + 2));
                    borderCoordinates.add(new Pair<>(i + 1, j + 1));
                    borderCoordinates.add(new Pair<>(i + 1, j));
                    borderCoordinates.add(new Pair<>(i + 1, j - 1));

                    borderCoordinates.add(new Pair<>(i, j - 1));
                    borderCoordinates.add(new Pair<>(i, j + 4));

                    borderCoordinates.add(new Pair<>(i - 1, j + 4));
                    borderCoordinates.add(new Pair<>(i - 1, j + 3));
                    borderCoordinates.add(new Pair<>(i - 1, j + 2));
                    borderCoordinates.add(new Pair<>(i - 1, j + 1));
                    borderCoordinates.add(new Pair<>(i - 1, j));
                    borderCoordinates.add(new Pair<>(i - 1, j - 1));
                }
                else {
                    vesselCoordinates.add(new Pair<>(i, j));
                    vesselCoordinates.add(new Pair<>(i + 1, j));
                    vesselCoordinates.add(new Pair<>(i + 2, j));
                    vesselCoordinates.add(new Pair<>(i + 3, j));

                    borderCoordinates.add(new Pair<>(i - 1, j + 1));
                    borderCoordinates.add(new Pair<>(i - 1, j));
                    borderCoordinates.add(new Pair<>(i - 1, j - 1));
                    borderCoordinates.add(new Pair<>(i, j - 1));
                    borderCoordinates.add(new Pair<>(i, j + 1));
                    borderCoordinates.add(new Pair<>(i + 1, j - 1));
                    borderCoordinates.add(new Pair<>(i + 1, j + 1));
                    borderCoordinates.add(new Pair<>(i + 2, j - 1));
                    borderCoordinates.add(new Pair<>(i + 2, j + 1));
                    borderCoordinates.add(new Pair<>(i + 3, j - 1));
                    borderCoordinates.add(new Pair<>(i + 3, j + 1));

                    borderCoordinates.add(new Pair<>(i + 4, j + 1));
                    borderCoordinates.add(new Pair<>(i + 4, j));
                    borderCoordinates.add(new Pair<>(i + 4, j - 1));
                }
            }
            break;
        }

        boolean fail = false;
        for (int l = 0; l < borderCoordinates.size(); l++)
        {
            if (!checkIfCorrect(borderCoordinates.get(l).first, borderCoordinates.get(l).second, orientation))
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

    private boolean checkIfCorrect(int i, int j, boolean orientation)
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

    public List<int[]> serialize()
    {
        return Arrays.asList(field);
    }
}
