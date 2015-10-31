package teamrocket.appsassins;

import android.content.Context;
import android.content.Intent;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends ActionBarActivity {
    private static final String TAG = "LoginActivity";
    @Bind(R.id.loginEmail) EditText emailEntry;
    @Bind(R.id.loginPassword) EditText passwordEntry;
    @Bind(R.id.loginButton) Button loginButton;
    @Bind(R.id.signUpTextView) TextView signupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
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

}
