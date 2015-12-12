package teamrocket.appsassins;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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


public class LoginActivity extends ActionBarActivity {
    private static final String TAG = "LoginActivity";
    @Bind(R.id.loginEmail) EditText emailEntry;
    @Bind(R.id.loginPassword) EditText passwordEntry;
    @Bind(R.id.loginButton) Button loginButton;
    @Bind(R.id.signUpTextView) TextView signupButton;
    private String jsonData;
    private SharedPreferences prefs;
    private boolean isValid;
    private View v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        prefs = getSharedPreferences("authUser", Context.MODE_PRIVATE);
        passwordEntry.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.loginButton)
    public void attemptLogin(View view) {
        v = view;
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(passwordEntry.getWindowToken(), 0);
        String email = emailEntry.getText().toString();
        String password = passwordEntry.getText().toString();
        if (!isEmailValid(email)) {
            Snackbar.make(view, getString(R.string.enterValidEmail), Snackbar.LENGTH_SHORT).show();
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
        Log.d(TAG, "Everything is valid");
        loginUser(email, password);



    }
    @OnClick(R.id.signUpTextView)
    public void signUp(View view) {
        Intent signupPage = new Intent(getApplicationContext(), Signup.class);
        startActivity(signupPage);
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

    private boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void loginUser(final String emailText, final String pwText) {


        if (isNetWorkAvailable()) {
            final ProgressDialog dialog = new ProgressDialog(LoginActivity.this, R.style.RedProgressDialog);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Logging In");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);


            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormEncodingBuilder()
                    .add("email", emailText).add("password", pwText)
                    .build();

            Request request = new Request.Builder().url("http://54.149.40.71/appsassins/api/index.php/loginUser").post(formBody).build();


            Call call = client.newCall(request);
            dialog.show();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Snackbar.make(this, getString(R.string.passwordAtLeast8) ,Snackbar.LENGTH_SHORT).show();
                            dialog.hide();
                            Snackbar.make(v, "Could not contact the server", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {

                        jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        isValid = verifyUser(emailText, pwText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.hide();
                                if (isValid == true) {
                                    Log.d(TAG, "Starting Home Activity from LoginActivity");
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    Snackbar.make(v, "Account Details Incorrect", Snackbar.LENGTH_SHORT).show();
                                }
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
            //Toast.makeText(this, getString(R.string.no_net_toast), Toast.LENGTH_LONG).show();
        }




    }
    private boolean verifyUser(String email, String password) throws JSONException{
        JSONObject user = new JSONObject(jsonData);
        if( user.getInt("status") != 1){
            Log.d(TAG, "FAILED, incorrect account info");
            return false;
        }
        else{
            prefs.edit().putString("username", email).apply();
            prefs.edit().putString("password", password).apply();
            prefs.edit().putString("fName", user.getString("fName")).apply();
            prefs.edit().putString("lName", user.getString("lName")).apply();
            return true;
        }
    }



}
