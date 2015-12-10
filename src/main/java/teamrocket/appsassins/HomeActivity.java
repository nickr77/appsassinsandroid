package teamrocket.appsassins;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private static final String TAG = "HomeActivity";
    private User user;
    private int status;
    private CurrentGame currentGame;
    @Bind(R.id.homeLayout) RelativeLayout layout;
    @Bind(R.id.gameProgressBar) RoundCornerProgressBar progress;
    @Bind(R.id.remainingPlayers) TextView remainingPlayers;
    @Bind(R.id.gameTitleText) TextView gameTitle;
    @Bind(R.id.targetName) TextView targetName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("authUser", Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        user = new User();



        if (checkIfLoggedIn() == false){
            Intent loginpage = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginpage);
            finish();
        }
        else{
            getGameInformation();
        }


    }

    private void getGameInformation() {
        if (isNetWorkAvailable()) {
            final ProgressDialog dialog = new ProgressDialog(HomeActivity.this, R.style.RedProgressDialog);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading Game Information");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormEncodingBuilder()
                    .add("email", user.getUsername())
                    .build();

            Request request = new Request.Builder().url("http://private-f80ce-appsassins.apiary-mock.com/getCurrentGameStatus").post(formBody).build();

            Log.d(TAG, "ABOUT TO CALL NETWORK");
            Call call = client.newCall(request);
            dialog.show();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                            Snackbar.make(layout, "Could not fetch information", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        currentGame = new CurrentGame();
                        parseResponse(response.body().string());
                        getTarget();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                remainingPlayers.setText(getString(R.string.remainingplayers) + " " + (currentGame.getPlayerAmount() - currentGame.getRemainingPlayers()) + "/"
                                        + currentGame.getPlayerAmount());
                                progress.setMax(currentGame.getPlayerAmount());
                                progress.setProgress(currentGame.getPlayerAmount() - currentGame.getRemainingPlayers());
                                gameTitle.setText(currentGame.getGameName());
                                targetName.setText(currentGame.getTargetName());
                                dialog.hide();

                            }
                        });

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {

                    }
                }
            });
            Log.d(TAG, "MAIN UI code is running!");
        }
        else {
            //no internet message
        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.refreshGameInfo:
                getGameInformation();
                return true;


            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_bar_notification:
                Intent notifPage = new Intent(getApplicationContext(), Notifications.class);
                notifPage.putExtra("email", user.getUsername());
                startActivity(notifPage);
                return true;

            case R.id.logoutButton:
                prefs.edit().clear().commit();
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
    private boolean checkIfLoggedIn() {
        String infoTest = "";
        infoTest = prefs.getString("username", "NO USER LOGGED IN");
        Log.d(TAG, infoTest);
        if (infoTest.equals("NO USER LOGGED IN")) {
            return false;
        }
        user.setUsername(prefs.getString("username", "failure"));
        user.setPassword(prefs.getString("password", "failure"));
        user.setFirstName(prefs.getString("fName", "failure"));
        user.setLastName(prefs.getString("lName", "failure"));

        return true;
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
    private void parseResponse(String json) throws JSONException{
        Log.d(TAG, json);
        JSONObject game = new JSONObject(json);
        status = game.getInt("gameStatus");
        if (status != -1){
            int gm = game.getJSONObject("user").getInt("gameMaster");
            if (gm == 1){
                currentGame.isGameMaster = true;
            }
            else{
                currentGame.isGameMaster = false;
            }
            currentGame.setGameName(game.getString("gameName"));
            JSONArray playerJson = game.getJSONArray("players");
            for(int i = 0; i < playerJson.length(); i++){
                String name = playerJson.getJSONObject(i).getString("Player");
                String email = playerJson.getJSONObject(i).getString("email");
                int alive = playerJson.getJSONObject(i).getInt("Alive");
                currentGame.addPlayer(name, email, alive);
            }
        }



    }


    public boolean getTarget() throws IOException { // this is synchronous, must be called from within runnable
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("email", user.getUsername()).build();
        Request request = new Request.Builder()
                .url("http://private-f80ce-appsassins.apiary-mock.com/playerTarget").post(formBody).build();

        Response response = client.newCall(request).execute();
        if(!response.isSuccessful()){
            Log.d(TAG, "No target response");
            return false;
        }
        String jsonData = response.body().string();
        try {
            JSONObject result = new JSONObject(jsonData);
            String tName = result.getString("email"); //key may change
            currentGame.setTargetEmail(result.getString("email"));
            currentGame.setTargetName(result.getString("target"));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }
//    @OnClick(R.id.tempbutton)
//    public void startNotif(View view) {
//        Log.d(TAG, "here");
//        Intent notifPage = new Intent(getApplicationContext(), Notifications.class);
//        startActivity(notifPage);
//    }
}
