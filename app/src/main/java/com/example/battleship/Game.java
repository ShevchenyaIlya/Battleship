package com.example.battleship;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;

public class Game extends ViewModel {
    private BattleField yourField;
    private BattleField opponentField;

    private boolean horizontalOrientation = true;
    private Vessel selectedShip = Vessel.fourDecker;
    private Mode mode = Mode.create;
    private String documentId;

    private UserConnection userConnection;
    private String connectionId;
    private Opponent opponent;
    private boolean gameStart = false;

    public BattleField getYourField() {
        return yourField;
    }

    public void setYourField(BattleField yourField) {
        this.yourField = yourField;
    }

    public BattleField getOpponentField() {
        return opponentField;
    }

    public void setOpponentField(BattleField opponentField) {
        this.opponentField = opponentField;
    }

    public boolean isHorizontalOrientation() {
        return horizontalOrientation;
    }

    public void setHorizontalOrientation(boolean horizontalOrientation) {
        this.horizontalOrientation = horizontalOrientation;
    }

    public Vessel getSelectedShip() {
        return selectedShip;
    }

    public void setSelectedShip(Vessel selectedShip) {
        this.selectedShip = selectedShip;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public UserConnection getUserConnection() {
        return userConnection;
    }

    public void setUserConnection(UserConnection user) {
        this.userConnection = user;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public void setGameStart(boolean gameStart) {
        this.gameStart = gameStart;
    }

    public Opponent getOpponent() {
        return opponent;
    }

    public void setOpponent(Opponent opponent) {
        this.opponent = opponent;
    }
}
