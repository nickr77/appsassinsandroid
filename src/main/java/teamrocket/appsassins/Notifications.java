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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Notifications extends AppCompatActivity {
    private String jsonData;
    final ArrayList<NotificationItem> notifList = new ArrayList<>();
    private static final String TAG = "Notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);
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
                    .add("email", "jrthomas@smu.edu")
                    .build();
            String url = "http://54.149.40.71/appsassins/api/index.php/getNotifications";
            //url = "http://private-f462a-appsassins.apiary-mock.com/getNotifications";
            Request request = new Request.Builder().url(url).post(formBody).build();


            Call call = client.newCall(request);
            //dialog.show();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                            final ListView notifView = (ListView)findViewById(R.id.notif_view);
                            final ArrayAdapter<NotificationItem> adapter = new ArrayAdapter<>(Notifications.this,
                                    android.R.layout.simple_list_item_2, android.R.id.text1, notifList);
                            notifView.setAdapter(adapter);

                            notifView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Integer position = i;
                                    Log.i("clicked", position.toString());
                                    int type = notifList.get(i).getType();
                                    if (type == 0 || type == 3) {
                                        NotificationAlert alertDialog = new NotificationAlert();
                                        alertDialog.setNotifID(notifList.get(i).getNotifID());
                                        alertDialog.setType(type);
                                        alertDialog.show(getFragmentManager(), "missiles");
                                    }
                                }
                            });

                            // Create a ListView-specific touch listener.
                            SwipeDismissListViewTouchListener touchListener =
                                    new SwipeDismissListViewTouchListener(
                                            notifView,
                                            new SwipeDismissListViewTouchListener.DismissCallbacks() {
                                                @Override
                                                public boolean canDismiss(int position) {
                                                    return true;
                                                }

                                                @Override
                                                public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                                    for (int position : reverseSortedPositions) {
                                                        adapter.remove(adapter.getItem(position));
                                                    }
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                            notifView.setOnTouchListener(touchListener);
                            notifView.setOnScrollListener(touchListener.makeScrollListener());

                        }
                    });

                }

            });
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
            NotificationItem newNotif;

            if (type == 0) {
                newNotif = new NotificationItem("You have a pending kill");
            }
            else if (type == 1) {
                newNotif = new NotificationItem("You have been killed by " +
                        item.getString("user2") + " in " + item.getString("gameName"));
            }
            else if (type == 2) {
                newNotif = new NotificationItem("Your kill of " + item.getString("user2") +
                        " has been denied in " + item.getString("gameName"));
            }
            else if (type == 3) {
                newNotif = new NotificationItem(item.getString("user2") +
                        " has invited you to play in " + item.getString("gameName"));
            }
            else if (type == 4) {
                newNotif = new NotificationItem("The game " + item.getString("gameName") +
                        " has started");
            }
            else if (type == 5) {
                newNotif = new NotificationItem("The game " + item.getString("gameName") +
                        " has ended" );
            }
            else if (type == 6) {
                newNotif = new NotificationItem("Welcome to Appsassins");
            }
            else {
                newNotif = new NotificationItem("Your game " + item.getString("gameName")
                        + " is ready to begin");
            }
            newNotif.setType(type);
            newNotif.setNotifID(item.getInt("notifID"));
            notifList.add(newNotif);
        }
    }
}
