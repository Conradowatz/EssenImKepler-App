package de.conradowatz.essenimkepler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;


public class LoadingDataActivity extends ActionBarActivity implements View.OnClickListener {

    private boolean isnoConnection = false;
    private TextView loadingText;
    private TextView noconnectionText;
    private ProgressBar progressBar;
    private ImageView cloudImage;
    private ImageView logoImage;
    private RelativeLayout layout;

    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_data);

        loadingText = (TextView) findViewById(R.id.loadingData_laden_textView);
        noconnectionText = (TextView) findViewById(R.id.loadingData_noconnection_textView);
        progressBar = (ProgressBar) findViewById(R.id.loadingData_progressBar);
        cloudImage = (ImageView) findViewById(R.id.loadingData_cloud_imageView);
        logoImage = (ImageView) findViewById(R.id.loadingData_logo_imageView);
        layout = (RelativeLayout) findViewById(R.id.loadingData_layout);
        layout.setOnClickListener(this);

        email = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "");
        password = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginPW", "");

        if (email.startsWith("demo")) {
            useDemoInfo();
        } else {
            downloadInfo();
        }
    }

    @Override
    public void onBackPressed() {

        //Anwendung schließen
        Intent backToMain = new Intent();
        backToMain.putExtra("ExitCode", "Exit");
        setResult(RESULT_OK, backToMain);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void downloadInfo() {

        EssenAPI.login(email, password, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                noConnection();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                finishLoading(responseString);
            }
        });



    }

    public void useDemoInfo() {

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finishLoading(HTMLtest.html);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void noConnection() {

        loadingText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        logoImage.setVisibility(View.INVISIBLE);
        cloudImage.setVisibility(View.VISIBLE);
        noconnectionText.setVisibility(View.VISIBLE);

        isnoConnection = true;

    }

    @Override
    public void onClick(View v) {
        if (!isnoConnection) {
            return;
        }

        loadingText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        logoImage.setVisibility(View.VISIBLE);
        cloudImage.setVisibility(View.INVISIBLE);
        noconnectionText.setVisibility(View.INVISIBLE);

        isnoConnection = false;

        downloadInfo();


    }

    public void finishLoading(String html) {

        Intent backToMain = new Intent();
        backToMain.putExtra("ExitCode", "LoggedIn");
        backToMain.putExtra("html", html);
        setResult(RESULT_OK, backToMain);
        finish();

    }
}
