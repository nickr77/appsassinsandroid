package teamrocket.appsassins;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 11/21/2015.
 */
public class CurrentGame {
    private List<Player> players;
    private List<Location> locations;
    public boolean isGameMaster;
    private String gameName;

    public CurrentGame(){
        isGameMaster = false;
        players = new ArrayList<>();
    }

    public void addPlayer(String name, String email, int status) {
        players.add(new Player(name, email, status));
    }
    public void addLocation(double lat, double lon){
        locations.add(new Location(lat, lon));
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void killPlayer(String username){
        //where players username == username, set alive to 0
    }
    public int getPlayerAmount(){
        return players.size();
    }
    public String getPlayerName(int index){
        return players.get(index).getName();
    }
    public String getPlayerEmail(int index){
        return players.get(index).getEmail();
    }
    public boolean getPlayerAlive(int index){
        return players.get(index).getStatus();
    }
    public int getRemainingPlayers(){
        int count = 0;
        for (int i = 0; i < players.size(); i++){
            if (players.get(i).isAlive())
                count++;
        }
        return count;
    }




}
