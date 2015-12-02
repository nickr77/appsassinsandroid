package teamrocket.appsassins;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Notifications extends AppCompatActivity {
    private String jsonData;
    final ArrayList<String> notifList = new ArrayList<>();
    private ArrayList<Integer> notifIds;
    private static final String TAG = "Notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);
        notifIds = new ArrayList<>();
        getNotifications();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void getNotifications() {
        if (isNetWorkAvailable()) {
            //final ProgressDialog dialog = new ProgressDialog(Notifications.this);
            //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //dialog.setMessage("Getting Notifications");
            //dialog.setIndeterminate(true);
            //dialog.setCanceledOnTouchOutside(false);


            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormEncodingBuilder()
                    .add("email", "spanky@smu.edu")
                    .build();
            String url = "http://54.149.40.71/appsassins/api/index.php/getNotifications";
            url = "http://private-f462a-appsassins.apiary-mock.com/getNotifications";
            Request request = new Request.Builder().url(url).post(formBody).build();


            Call call = client.newCall(request);
            //dialog.show();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Snackbar.make(this, getString(R.string.passwordAtLeast8) ,Snackbar.LENGTH_SHORT).show();
                            //dialog.hide();
                            Snackbar.make(findViewById(R.id.notif_view), "Could not contact the server", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {

                    try {
                        jsonData = response.body().string();
                        parseNotifs();
                    } catch (JSONException e) {
                        Log.e("parse", e.toString());
                    }
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListView notifView = (ListView)findViewById(R.id.notif_view);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Notifications.this,
                                    android.R.layout.simple_list_item_2, android.R.id.text1, notifList);
                            notifView.setAdapter(adapter);
                            //dialog.hide()
                            //Log.v(TAG, jsonData);
                            //Snackbar.make(findViewById(R.id.notifs), "Running", Snackbar.LENGTH_SHORT).show();

                        }
                    });

                }

            });
            //Log.d(TAG, "MAIN UI code is running!");
        }
        else {
            //Toast.makeText(this, getString(R.string.no_net_toast), Toast.LENGTH_LONG).show();
        }
    }

    private void parseNotifs() throws JSONException {
        JSONObject json = new JSONObject(jsonData);
        //String status = json.getString("status");
        JSONArray notifs = json.getJSONArray("notifications");
        for (int i = 0; i < notifs.length(); i++) {
            JSONObject item = notifs.getJSONObject(i);
            Integer type = item.getInt("type");
            //Log.v("notif", type.toString());
            if (type == 0) {
                notifList.add("You have a pending kill");
            }
            else if (type == 1) {
                notifList.add("You have been killed by " + item.getString("user2") + " in " +
                        item.getString("gameName"));
            }
            else if (type == 2) {
                notifList.add("Your kill of " + item.getString("user2") + " has been denied in " +
                        item.getString("gameName"));
            }
            else if (type == 3) {
                notifList.add(item.getString("user2") + " has invited you to play in " +
                        item.getString("gameName"));
            }
            else if (type == 4) {
                notifList.add("The game " + item.getString("gameName") + " has started");
            }
            else if (type == 5) {
                notifList.add("The game " + item.getString("gameName") + " has ended" );
            }
            else if (type == 6) {
                notifList.add("Welcome to Appsassins");
            }
            else {
                notifList.add("Your game " + item.getString("gameName") + " is ready to begin");
            }
            notifIds.add(item.getInt("notifID"));
            Log.d("parse", "executed");

        }
    }
}
