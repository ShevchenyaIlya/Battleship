package com.example.battleship;

import java.io.Serializable;

public class UserConnection implements Serializable {
    private User user;
    private String connectionId;

    public UserConnection(User user, String connectionId) {
        this.user = user;
        this.connectionId = connectionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
