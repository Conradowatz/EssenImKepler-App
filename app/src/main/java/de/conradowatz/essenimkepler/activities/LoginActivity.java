package de.conradowatz.essenimkepler.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.conradowatz.essenimkepler.R;
import de.conradowatz.essenimkepler.tools.EssenAPI;
import de.conradowatz.essenimkepler.tools.PreferenceReader;
import de.greenrobot.event.EventBus;


public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameInput;
    private TextInputLayout passwordInput;
    private Button loginButton;
    private ProgressWheel loginProgressWheel;
    private TextView loginErrorText;

    private EventBus eventBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        usernameInput = (TextInputLayout) findViewById(R.id.usernameInput);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordInput);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginProgressWheel = (ProgressWheel) findViewById(R.id.loginProgressWheel);
        loginErrorText = (TextView) findViewById(R.id.loginErrorText);

        //Wenn Button gedrückt -> einlogen
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        //Wenn ENTER geklickt wird -> einloggen
        passwordInput.getEditText().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    login();
                }
                return false;
            }
        });
    }


    /**
     * Überprüft die eingegebenen Benutzerdaten
     */
    private void login() {
        final String email = usernameInput.getEditText().getText().toString();
        final String password = passwordInput.getEditText().getText().toString();

        loginButton.setVisibility(View.INVISIBLE);
        loginProgressWheel.setVisibility(View.VISIBLE);
        loginErrorText.setVisibility(View.INVISIBLE);
        passwordInput.getEditText().setEnabled(false);
        usernameInput.getEditText().setEnabled(false);

        final LoginActivity context = this;

        final EssenAPI essenAPI = new EssenAPI(email, password);
        essenAPI.downloadData(new EssenAPI.AsyncEssenResponseHandler() {

            @Override
            public void onSucess() {

                if (essenAPI.getEssenTagList().size() > 0) {

                    PreferenceReader.saveStringToPreferences(getApplicationContext(), EssenAPI.PREF_EMAIL, email);
                    PreferenceReader.saveStringToPreferences(getApplicationContext(), EssenAPI.PREF_PASSWORD, password);

                    eventBus.post(essenAPI);
                    finish();

                } else onOtherError(new Throwable("Konnte keine Informationen laden"));
            }

            @Override
            public void onNoConnection() {

                showErrorMessage("Keine Verbindung zum Server!");

            }

            @Override
            public void onNoAccess() {

                showErrorMessage("Fehler bei der Anmeldung!");

            }

            @Override
            public void onOtherError(Throwable throwable) {

                //Wenn es hier ein Error gibt, hat sich warscheinlich das Online System geändert
                Log.e("JKGDEBUG", "Error bei der Verarbeitung der Daten");
                throwable.printStackTrace();
                MainActivity.showErrorDialog(context, throwable.getMessage());

            }

        });

    }

    private void showErrorMessage(String message) {

        loginButton.setVisibility(View.VISIBLE);
        loginProgressWheel.setVisibility(View.INVISIBLE);
        passwordInput.getEditText().setEnabled(true);
        usernameInput.getEditText().setEnabled(true);
        loginErrorText.setVisibility(View.VISIBLE);
        loginErrorText.setText(message);

    }

    @Override
    public void onBackPressed() {

        //Anwendung schließen
        Intent backToMain = new Intent();
        backToMain.putExtra("ExitCode", "Exit");
        setResult(RESULT_OK, backToMain);
        finish();
    }

}
