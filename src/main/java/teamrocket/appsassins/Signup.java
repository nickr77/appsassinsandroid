package teamrocket.appsassins;

import android.content.Context;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
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
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(confirmPasswordEntry.getWindowToken(), 0);
        String tempName = nameEntry.getText().toString();
        String email = emailEntry.getText().toString();
        String password = passwordEntry.getText().toString();
        String cPass = confirmPasswordEntry.getText().toString();

        String[] firstLastNames = tempName.split(" ");
        if (firstLastNames.length != 2) {
            Snackbar.make(view, getString(R.string.errorEnterFirstandLastName) ,Snackbar.LENGTH_SHORT).show();
            nameEntry.requestFocus();
            return;
        }
        firstLastNames[1] = removeTrailingSpaces(firstLastNames[1]);
        Log.d(TAG, "First name = " + firstLastNames[0] + " length: " + firstLastNames[0].length() + " and last name = " + firstLastNames[1] + " length: " + firstLastNames[1].length());
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



}
