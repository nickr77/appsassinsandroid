package teamrocket.appsassins;

import java.util.List;

/**
 * Created by Nick on 11/21/2015.
 */
public class CurrentGame {
    private List<Player> players;
    private List<Location> locations;


    public void addPlayer(String name, int status) {
        players.add(new Player(name, status));
    }
    public void addLocation(double lat, double lon){
        locations.add(new Location(lat, lon));
    }
    public void killPlayer(String username){
        //where players username == username, set alive to 0
    }



}
