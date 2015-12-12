package teamrocket.appsassins;

import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    public void dismiss() {
        Integer id = notifID;
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormEncodingBuilder()
                .add("notifID", id.toString())
                .add("read", "1")
                .build();
        String url = "http://54.149.40.71/appsassins/api/index.php/getNotifications";
        Request request = new Request.Builder().url(url).post(formBody).build();


        Call call = client.newCall(request);
        //dialog.show();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("Error", "read request failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    parseResponse(jsonData);
                } catch (JSONException e) {
                    Log.e("parse", e.toString());
                }
            }
        });
    }

    private void parseResponse(String jsonData) throws JSONException {
        JSONObject json = new JSONObject(jsonData);
        String status = json.getString("status");
        Log.i("Send Read", status);
    }

}
