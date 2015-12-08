package teamrocket.appsassins;

import android.util.Log;

/**
 * Created by Nick on 11/21/2015.
 */
public class Player {
    private String name;
    private boolean alive;
    private String email;

    public Player(String name, String email, int status){
        this.name = name;
        this.email = email;
        if (status == 1){
            alive = true;
        }
        else {
            alive = false;
        }
        Log.d("PLAYEROBJECT", "Added player: " + this.name + " " + this.email + " that is status " + status);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public boolean getStatus(){
        return alive;
    }
}
