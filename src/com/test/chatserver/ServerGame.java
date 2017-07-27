package com.test.chatserver;

import java.util.ArrayList;

public class ServerGame {
    private final int numPlayers;
    private final ArrayList<String> players;
    
    public ServerGame(int numPlayers, ArrayList<String> players) throws Exception {
        if (players.size() > numPlayers) {
            throw new Exception("Invalid Game creation state");
        }
        this.numPlayers = numPlayers;
        this.players = players;
    }
    
    public ArrayList<String> players() {
        return this.players;
    }
    
    public int numPlayers() {
        return numPlayers;
    }

}
