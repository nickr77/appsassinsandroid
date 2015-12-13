package teamrocket.appsassins;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeActivity extends AppCompatActivity{
    private SharedPreferences prefs;
    private static final int PICTURE_TAKING = 444;
    private static final String TAG = "HomeActivity";
    private User user;
    private int status;
    private CurrentGame currentGame;
    @Bind(R.id.homeLayout) RelativeLayout layout;
    @Bind(R.id.gameProgressBar) RoundCornerProgressBar progress;
    @Bind(R.id.remainingPlayers) TextView remainingPlayers;
    @Bind(R.id.gameTitleText) TextView gameTitle;
    @Bind(R.id.targetName) TextView targetName;
    @Bind(R.id.tagButton) Button tagButton;
    private Uri fileUri;
    private File photoFile;
    private SimpleLocation simpleLocation;
    private int test;


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
            simpleLocation = new SimpleLocation(this);
            if (!simpleLocation.hasLocationEnabled()) {
                // ask the user to enable location access
                SimpleLocation.openSettings(this);
            }

            getGameInformation();
        }


    }


    @OnClick(R.id.tagButton)
    public void takePicture(){
        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(pictureIntent.resolveActivity(getPackageManager()) != null){
                photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e){
                    Log.d(TAG, "COULD NOT CREATE THE FILE");
                }
                if (photoFile != null) {
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(pictureIntent, PICTURE_TAKING);
                }
            }
        }
    }

    private File createImageFile() throws IOException{
        String fileName = "temp";
        File storageDir = getExternalFilesDir(null);
        if (storageDir != null) {
            Log.d(TAG, "CREATING Directory " + storageDir.getAbsolutePath());
            storageDir.mkdir();
        }
        File image = File.createTempFile(fileName, ".jpg", storageDir);
        //File image = new File(storageDir.getAbsolutePath() + fileName + ".jpg");
        //File image = new File(getExternalFilesDir(null), fileName + ".jpg");
        Log.d(TAG, "Path of create image " + image.getAbsolutePath());

        fileUri = Uri.fromFile(image);
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PICTURE_TAKING){
            Log.d(TAG, "RESPONSE CODE IS: " + resultCode);
            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            Log.d(TAG, "INITIAL FILE SIZE: " + (photoFile.length() / 1024 / 1024));

            ///////////////////FILE COMPRESSION///////////////////////
            File pictureFile = new File(fileUri.getPath());
            File image;
            try {
                image = File.createTempFile("compressed", ".jpg", getExternalFilesDir(null));

            } catch (IOException e) {
                Log.d(TAG, "FAILED");
                return;
            }
            Bitmap temp = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
            try {
                FileOutputStream filecon = new FileOutputStream(image);
                temp.compress(Bitmap.CompressFormat.JPEG, 70, filecon);

            } catch (FileNotFoundException e) {
                Log.d(TAG, "FAILED2");
                return;
            }

            Log.d(TAG, "FILE-->: " + fileUri.getPath());
            Log.d(TAG, "FILE-->: " + Uri.fromFile(photoFile).getPath());
            Log.d(TAG, "FILEC-->: " + Uri.fromFile(image).getPath());
            photoFile = image;
            Log.d(TAG, "FILE-->: " + Uri.fromFile(photoFile).getPath());
            Log.d(TAG, "Compressed FILE SIZE: " + (photoFile.length() / 1024));
            ///////////////END FILE COMPRESSION////////////////////

            sendTagInfo(); // SUCK A DICK DANH
        }

    }

    private void sendTagInfo(){
        if(isNetWorkAvailable()) {
            final ProgressDialog dialog = new ProgressDialog(HomeActivity.this, R.style.RedProgressDialog);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Tagging Target");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            final double latitude = simpleLocation.getLatitude();
            final double longitude = simpleLocation.getLongitude();
            OkHttpClient client = new OkHttpClient();
            RequestBody tagBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart("email", currentGame.getTargetEmail()).addFormDataPart("location[lat]", String.valueOf(latitude))
                    .addFormDataPart("location[lng]", String.valueOf(longitude))
                    .addFormDataPart("gameName", currentGame.getGameName())
                    .addFormDataPart("thumbnail", fileUri.toString(), RequestBody.create(MediaType.parse("image/jpg"), photoFile)).build();
            //"location", "{\"lat\": 32.842539, \"lng\": -96.782461}"
            Request request = new Request.Builder().url("http://54.149.40.71/appsassins/api/index.php/killTarget").post(tagBody).build();
            Call call = client.newCall(request);
            dialog.show();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                            Snackbar.make(layout, "Error tagging target", Snackbar.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if(isTagSuccessful(response.body().string())){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.hide();
                                Snackbar.make(layout, "Target Tagged", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.hide();
                                Snackbar.make(layout, "Error tagging target", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });
        }
        else{
            Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean isTagSuccessful(String tagResponse){
        int status = 0;
        try {
            Log.d(TAG, "GOT HERE");
            Log.d(TAG, tagResponse);
            status = new JSONObject(tagResponse).getInt("status");
            Log.d(TAG, status + "");
        } catch (JSONException e) {
            return false;
        }
        if(status == 1){
            return true;
        }
        else{
            return false;
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

            Request request = new Request.Builder().url("http://54.149.40.71/appsassins/api/index.php/getCurrentGameStatus").post(formBody).build();

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
                            tagButton.setVisibility(View.INVISIBLE);
                            Snackbar.make(layout, "Could not fetch information", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        currentGame = new CurrentGame();
                        test = parseResponse(response.body().string());
                        if (test == 2) {
                            getTarget();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.hide();
                                if (test == -1) {
                                    Log.d(TAG, "SHIT IS FALSE");
                                    Intent intent = new Intent(getApplicationContext(), GameWaitingActivity.class);
                                    startActivity(intent);
                                    finish();
                                    //GO TO WAITING ACTIVIITY

                                } else if (test == 0 || test == 1) {
                                    progress.setMax(1);
                                    progress.setProgress(1);
                                    gameTitle.setText(currentGame.getGameName());
                                    targetName.setText("Game Hasn't Started Yet");
                                    tagButton.setVisibility(View.INVISIBLE);
                                } else {
                                    remainingPlayers.setText(getString(R.string.remainingplayers) + " " + (currentGame.getPlayerAmount() - currentGame.getRemainingPlayers()) + "/"
                                            + currentGame.getPlayerAmount());
                                    progress.setMax(currentGame.getPlayerAmount());
                                    progress.setProgress(currentGame.getPlayerAmount() - currentGame.getRemainingPlayers());
                                    gameTitle.setText(currentGame.getGameName());
                                    targetName.setText(currentGame.getTargetName());
                                    tagButton.setVisibility(View.VISIBLE);
                                }


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

    private void quitGame(){
        if(isNetWorkAvailable()){
            final ProgressDialog dialog = new ProgressDialog(HomeActivity.this, R.style.RedProgressDialog);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Leaving Game");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormEncodingBuilder()
                    .add("email", user.getUsername()).build();
            Request request = new Request.Builder()
                    .url("http://54.149.40.71/appsassins/api/index.php/quitGame").post(formBody).build();
            Call call = client.newCall(request);
            dialog.show();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Quit game failed");
                            dialog.hide();
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                            getGameInformation();
                        }
                    });
                }
            });
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
                quitGame();
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
    private int parseResponse(String json) throws JSONException{
        Log.d(TAG, json);
        Log.d(TAG,"HELLO0");
        JSONObject game = new JSONObject(json);
        Log.d(TAG,"HELLO1");
        try{
            status = game.getInt("gameStatus");
        } catch(JSONException e){
            status = -1;
        }
        Log.d(TAG,"HELLO2");
        if (status == 1 || status == 0){
            currentGame.setGameName(game.getString("gameName"));
        }
        if (status == 2){
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
            return status;
        }
        Log.d(TAG, "Returning False");
        return status;




    }


    public boolean getTarget() throws IOException { // this is synchronous, must be called from within runnable
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("email", user.getUsername()).build();
        Request request = new Request.Builder()
                .url("http://54.149.40.71/appsassins/api/index.php/getCurrentTarget").post(formBody).build();

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

}
