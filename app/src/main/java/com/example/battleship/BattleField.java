package com.example.battleship;

public class BattleField{
    private int[][] field = new int[10][10];

    private int fourDecker;
    private int threeDecker;
    private int twoDecker;
    private int oneDecker;

    public BattleField()
    {
        fourDecker = 1;
        threeDecker = 2;
        twoDecker = 3;
        oneDecker = 4;
    }

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }

    public void setVessel(int i, int j)
    {
       if (field[i + 1][j + 1] != 1 && field[i + 1][j - 1] != 1 && field[i - 1][j - 1] != 1 && field[i - 1][j + 1] != 1)
       {
           if (field[i + 1][j] != 1 && field[i - 1][j] != 1 && field[i][j - 1] != 1 && field[i][j + 1] != 1)
           {
               field[i][j] = 1;
           }
           else
           {
               if(checkSurroundVessel())
               {
                   field[i][j] = 1;
               }
           }
       }
    }

    private boolean checkSurroundVessel()
    {
        return true;
    }

    public String[] shipsTypes()
    {
        return new String[] {
                "Single-deck", "Double-deck", "Three-decker", "Four-decker"
        };
    }
}
