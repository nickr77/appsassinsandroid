package teamrocket.appsassins;

/**
 * Created by Danh on 12/2/2015.
 */
public class NotificationItem {
    private String value;
    private int type;
    private int notifID;

    public NotificationItem(String val, Integer type, Integer notifID) {
        this.value = val;
        this.type = type;
        this.notifID = notifID;
    }

    public NotificationItem(String val) {
        this.value = val;
    }

    public int getType() {
        return type;
    }

    public int getNotifID() {
        return notifID;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return getValue();
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setNotifID(Integer notifID) {
        this.notifID = notifID;
    }

    public void setValue(String val) {
        this.value = val;
    }
}
