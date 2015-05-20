package de.conradowatz.essenimkepler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;


public class LoginActivity extends ActionBarActivity {

    private EditText loginEmailEdit;
    private EditText loginPasswordEdit;
    private Button loginButton;
    private ProgressBar loginProgressBar;
    private TextView loginErrorTextView;

    private EssenAPI essenAPI = new EssenAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.login_button);
        loginEmailEdit = (EditText) findViewById(R.id.login_email_editText);
        loginPasswordEdit = (EditText) findViewById(R.id.login_password_editText);
        loginProgressBar = (ProgressBar) findViewById(R.id.login_progressBar);
        loginErrorTextView = (TextView) findViewById(R.id.login_error_textView);

        //Wenn auf LOGIN geklickt wird -> einloggen
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(loginEmailEdit.getText().toString(), loginPasswordEdit.getText().toString());
            }
        });

        //Wenn ENTER gedrückt -> einloggen
        loginPasswordEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    login(loginEmailEdit.getText().toString(), loginPasswordEdit.getText().toString());
                }
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {

        //Anwendung schließen
        Intent backToMain = new Intent();
        backToMain.putExtra("ExitCode", "Exit");
        setResult(RESULT_OK, backToMain);
        finish();
    }

    private void login(String email, String password) {

        //Tastatur schließen
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromInputMethod(loginPasswordEdit.getWindowToken(), 0);

        //Demo Benutzer
        if (email.toLowerCase().startsWith("demo")) {
            loginSucess("demo", "", HTMLtest.html);
            return;
        }

        if (email.isEmpty() || (password.isEmpty())) {
            return;
        }

        //Progressbar zeigen, Error Text verbergen
        loginButton.setVisibility(View.INVISIBLE);
        loginProgressBar.setVisibility(View.VISIBLE);
        loginErrorTextView.setVisibility(View.INVISIBLE);

        //EditText unbearbeitbar machen
        loginPasswordEdit.setEnabled(false);
        loginEmailEdit.setEnabled(false);

        checkLogin(email, password);

    }

    public void checkLogin(final String email, final String password) {

        essenAPI.login(email, password, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //Keine Verbindung zum Server
                loginError(statusCode);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                //Verbindung hergestellt
                if (responseString.contains("Benutzeranmeldung")) {
                    //Falsche Benutzerdaten
                    //Fehler anzeigen
                    loginError(EssenAPI.LOGIN_FAILED);
                }

                if (responseString.contains("Angemeldet")) {
                    //Erfolgreich angemeldet
                    loginSucess(email, password, responseString);
                }

            }
        });

    }

    private void loginError(int errorCode) {

        //Progressbar verstecken
        loginButton.setVisibility(View.VISIBLE);
        loginProgressBar.setVisibility(View.INVISIBLE);

        //EditText bearbeitbar machen
        loginPasswordEdit.setEnabled(true);
        loginEmailEdit.setEnabled(true);

        if (errorCode==EssenAPI.LOGIN_FAILED){
            loginErrorTextView.setText("Login fehlgeschlagen: Falsche Benutzerdaten");
        } else {
            loginErrorTextView.setText("Login fehlgeschlagen: Keine Verbindung zum Server");
            essenAPI.reCreateClient();
        }

        loginErrorTextView.setVisibility(View.VISIBLE);

    }

    private void loginSucess(String email, String password, String html) {

        //Benutzerdaten speichern
        PreferenceReader.saveStringToPreferences(getApplicationContext(), "loginEMail", email);
        PreferenceReader.saveStringToPreferences(getApplicationContext(), "loginPW", password);

        //zur Hauptanwendung wechseln
        Intent backToMain = new Intent();
        backToMain.putExtra("ExitCode", "LoggedIn");
        backToMain.putExtra("html", html);
        setResult(RESULT_OK, backToMain);
        finish();

    }

}
