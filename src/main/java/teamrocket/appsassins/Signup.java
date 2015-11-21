package teamrocket.appsassins;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Signup extends ActionBarActivity {
    private static final String TAG = "SignupActivity";
    @Bind(R.id.enterFullName) EditText nameEntry;
    @Bind(R.id.enterEmailSignup) EditText emailEntry;
    @Bind(R.id.enterPasswordSignup) EditText passwordEntry;
    @Bind(R.id.confirmPasswordSignup) EditText confirmPasswordEntry;
    @Bind(R.id.signUpButton) Button signupButton;
    private String jsonData;
    private boolean isWorking;
    private SharedPreferences prefs;
    private String[] name;
    private String email;
    private String password;
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        prefs = this.getSharedPreferences("authUser", Context.MODE_PRIVATE);
        confirmPasswordEntry.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signupButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }
    @OnClick(R.id.signUpButton)
    public void attemptSignUp(View view) {
        v = view;
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(confirmPasswordEntry.getWindowToken(), 0);
        String tempName = nameEntry.getText().toString();
        email = emailEntry.getText().toString();
        password = passwordEntry.getText().toString();
        String cPass = confirmPasswordEntry.getText().toString();

        name = tempName.split(" ");
        if (name.length != 2) {
            Snackbar.make(view, getString(R.string.errorEnterFirstandLastName) ,Snackbar.LENGTH_SHORT).show();
            nameEntry.requestFocus();
            return;
        }
        name[1] = removeTrailingSpaces(name[1]);
        Log.d(TAG, "First name = " + name[0] + " length: " + name[0].length() + " and last name = " + name[1] + " length: " + name[1].length());
        if (!isEmailValid(email)) {
            Snackbar.make(view, getString(R.string.enterValidEmail) ,Snackbar.LENGTH_SHORT).show();
            emailEntry.requestFocus();
            Log.d(TAG, email + " is invalid.");
            return;
        }
        Log.d(TAG, email + " is valid!");
        if (!verifyPassword(password)) {
            Snackbar.make(view, getString(R.string.passwordAtLeast8) ,Snackbar.LENGTH_SHORT).show();
            passwordEntry.requestFocus();
            return;
        }
        if (!cPass.equals(password)) {
            Snackbar.make(view, getString(R.string.passDontMatch) ,Snackbar.LENGTH_SHORT).show();
            confirmPasswordEntry.requestFocus();
            return;
        }

        createAccount();

    }

    private boolean verifyPassword(String password) {
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        if(password.matches(regex)) {
            return true;
        }
        return false;
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private String removeTrailingSpaces(String word) {
        return word.replaceAll("\\s+$", "");
    }

    private void createAccount() {
        final ProgressDialog dialog = new ProgressDialog(Signup.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Logging In");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);



        if (isNetWorkAvailable()){
            OkHttpClient client = new OkHttpClient();



            RequestBody formBody = new FormEncodingBuilder()
                    .add("fName", name[0]).add("lName", name[1])
                    .add("email", email).add("password", password)
                    .build();

            Request request = new Request.Builder().url("http://private-f80ce-appsassins.apiary-mock.com/register").post(formBody).build();


            Call call = client.newCall(request);
            dialog.show();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                            Snackbar.make(v, "Could not connect to the server", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    isWorking = false;
                    try {
                        jsonData = response.body().string();
                        isWorking = verifyAccount();
                    } catch (JSONException e) {
                        //This ain't the ritz carlton, I'm not gonna handle this

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                            if (isWorking == true)
                            {
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {

                                Snackbar.make(v, "Could not connect to the server", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });





        }
        else {

        }

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
    private boolean verifyAccount() throws JSONException {
        JSONObject user = new JSONObject(jsonData);
        if(!(user.getInt("status") == 1)){
            Log.d(TAG, "FAILED, account exists");
            return false;
        }
        else{
            prefs.edit().putString("username", email).apply();
            prefs.edit().putString("password", password).apply();
            prefs.edit().putString("fName", name[0] ).apply();
            prefs.edit().putString("lName", name[1]).apply();
            return true;
        }
    }

}
