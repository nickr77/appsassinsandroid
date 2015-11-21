package teamrocket.appsassins;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;


public class HomeActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private static final String TAG = "HomeActivity";
    private User user;
    private String jsonData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("authUser", Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        user = new User();
        if (checkIfLoggedIn() == false){
            Intent loginpage = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginpage);
            finish();
        }
        //getGameInformation();
    }

    private void getGameInformation() {
        if (isNetWorkAvailable()) {

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormEncodingBuilder()
                    .add("email", user.getUsername())
                    .build();

            Request request = new Request.Builder().url("http://private-f80ce-appsassins.apiary-mock.com/currentGame").post(formBody).build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // couldn't connect message
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {

                        jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        parseResponse();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });

                    }
                    catch (IOException e) {
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private boolean checkIfLoggedIn() {
        String infoTest = "";
        infoTest = prefs.getString("username", "NO USER LOGGED IN");
        Log.d(TAG, infoTest);
        if (infoTest == "NO USER LOGGED IN") {
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
    private void parseResponse() throws JSONException{

    }
}
