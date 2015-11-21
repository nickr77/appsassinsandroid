package teamrocket.appsassins;

/**
 * Created by Nick on 11/21/2015.
 */
public class Player {
    private String name;
    private boolean alive;

    public Player(String name, int status){
        this.name = name;
        if (status == 1){
            alive = true;
        }
        else {
            alive = false;
        }
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



}
