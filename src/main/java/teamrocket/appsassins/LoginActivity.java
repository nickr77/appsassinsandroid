package teamrocket.appsassins;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends ActionBarActivity {

    @Bind(R.id.loginEmail) EditText emailEntry;
    @Bind(R.id.loginPassword) EditText passwordEntry;
    @Bind(R.id.loginButton) Button loginButton;
    @Bind(R.id.signUpTextView) TextView signupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.loginButton)
    public void attemptLogin(View view) {

    }
    @OnClick(R.id.signUpTextView)
    public void signUp(View view) {

    }



}
