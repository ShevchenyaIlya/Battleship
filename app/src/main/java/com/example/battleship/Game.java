package com.example.battleship;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;

public class Game extends ViewModel {
    private BattleField yourField;
    private BattleField opponentField;

    private boolean horizontalOrientation = true;
    private String selectedShip = "Four-decker";
    private String mode = "Create";
    private String documentId;

    private UserConnection userConnection;
    private String connectionId;
    private String opponentEmail;
    private String opponentId;
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

    public String getSelectedShip() {
        return selectedShip;
    }

    public void setSelectedShip(String selectedShip) {
        this.selectedShip = selectedShip;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
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

    public String getOpponentEmail() {
        return opponentEmail;
    }

    public void setOpponentEmail(String opponentEmail) {
        this.opponentEmail = opponentEmail;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public void setGameStart(boolean gameStart) {
        this.gameStart = gameStart;
    }
}
