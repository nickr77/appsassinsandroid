package teamrocket.appsassins;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import butterknife.Bind;
import butterknife.ButterKnife;

public class GameWaitingActivity extends AppCompatActivity {

    @Bind(R.id.invitesListView) ListView inviteListView;

    private final String TAG = "GAMEWAIT";
    private String email;
    private SharedPreferences prefs;
    private ArrayList<String> invites;
    private ArrayList<Integer> notifIds;
    private ArrayAdapter<String> inviteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_waiting);
        ButterKnife.bind(this);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }
        invites = new ArrayList<>();
        prefs = getSharedPreferences("authUser", Context.MODE_PRIVATE);
        email = prefs.getString("username", "failure");
        inviteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, invites);
        inviteListView.setEmptyView(findViewById(R.id.noGameInvites));
        inviteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int notifID = notifIds.get(position);
                sendResponse(notifID);
            }
        });
        getInvites();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_waiting, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.refreshInvites:
                getInvites();
                return true;

            case R.id.logoutButton2:
                prefs.edit().clear().apply();
                Intent loginpage = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginpage);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    private void sendResponse(int notifid){
        final ProgressDialog dialog = new ProgressDialog(GameWaitingActivity.this, R.style.RedProgressDialog);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Joining Game");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        OkHttpClient client = new OkHttpClient();
        String url = "http://54.149.40.71/appsassins/api/index.php/sendNotificationResponse";
        RequestBody formBody = new FormEncodingBuilder()
                .add("notifID", String.valueOf(notifid))
                .add("accepted", "1")
                .build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        Call call = client.newCall(request);
        dialog.show();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                    }
                });

            }

            @Override
            public void onResponse(Response response) throws IOException {
                boolean good = false;
                try {
                    good = isValidJoin(response.body().string());
                } catch (JSONException e) {
                    Log.d(TAG, "JSON EXCEPTION OCCURRED WHEN JOINING GAME");
                    good = false;

                }
                if (good = false){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

            }
        });
    }

    private boolean isValidJoin(String jsonData) throws JSONException {
        JSONObject json = new JSONObject(jsonData);
        int status = json.getInt("status");
        if (status == 1){
            return true;
        }
        else
            return false;
    }

    public void getInvites(){
        final ProgressDialog dialog = new ProgressDialog(GameWaitingActivity.this, R.style.RedProgressDialog);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading Invites");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormEncodingBuilder()
                .add("email", email).build();
        Request request = new Request.Builder().url("http://54.149.40.71/appsassins/api/index.php/getNotifications").post(formBody).build();
        Call call = client.newCall(request);
        dialog.show();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    populateList(response.body().string()); //gets invites
                } catch (JSONException e) {
                    Log.d(TAG, "JSON EXCEPTION WHEN POPULATE LIST");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inviteListView.setAdapter(inviteAdapter);
                        inviteAdapter.notifyDataSetChanged();
                        dialog.hide();
                        //populate List View
                    }
                });
            }
        });
    }

    private void populateList(String jsondata) throws JSONException {
        Log.d(TAG, jsondata);
        invites = new ArrayList<>();
        notifIds = new ArrayList<>();
        JSONObject json = new JSONObject(jsondata);
        JSONArray notifs = json.getJSONArray("notifications");

        for(int i = 0; i < notifs.length(); i++){
            JSONObject item = notifs.getJSONObject(i);
            int type = item.getInt("type");
            if(type == 3){
                String temp = item.getString("user1");
                String temp2 = item.getString("gameName");
                String temp3 = temp + " has invited you to play in " + temp2;
                invites.add(temp3);
                notifIds.add(item.getInt("notifID"));
            }
        }
        inviteAdapter = new ArrayAdapter<>(this, R.layout.invitelistlayout, invites);
    }

    private boolean isNetWorkAvailable(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

}
