package de.conradowatz.essenimkepler.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;

import de.conradowatz.essenimkepler.MyApplication;
import de.conradowatz.essenimkepler.R;
import de.conradowatz.essenimkepler.fragments.BestellungenFragment;
import de.conradowatz.essenimkepler.fragments.SpeiseplanFragment;
import de.conradowatz.essenimkepler.tools.EssenAPI;
import de.conradowatz.essenimkepler.tools.PreferenceReader;
import de.greenrobot.event.EventBus;


public class MainActivity extends AppCompatActivity {

    public EssenAPI essenAPI;
    private Toolbar toolbar;
    private Drawer navigationDrawer;
    private Menu menu;
    private BestellungenFragment bestellungenFragment;
    private SpeiseplanFragment speiseplanFragment;
    private int currentlySelected;
    private Boolean isRefreshing = false;
    private boolean isInfoDialog = false;
    private boolean isLogoutDialog = false;

    private boolean loggedIn = false;

    private EventBus eventBus = EventBus.getDefault();

    public static void showErrorDialog(Context context, String error) {

        new AlertDialog.Builder(context)
                .setTitle("Unbekannter Fehler")
                .setMessage("Es trat ein unbekannter Fehler bei der Verarbeitung der Daten auf:\n" + error + "\nBitte kontaktiere den App Entwickler!")
                .setNeutralButton("Okay", null).create();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buildDrawer();

        if (getLastCustomNonConfigurationInstance() != null) {

            //App noch im Speicher, wiederherstellen
            essenAPI = (EssenAPI) getLastCustomNonConfigurationInstance();
            CharSequence title = savedInstanceState.getCharSequence("title");
            getSupportActionBar().setTitle(title);
            int selection = savedInstanceState.getInt("selection");
            if (selection >= 0) navigationDrawer.setSelection(selection, false);
            currentlySelected = savedInstanceState.getInt("currentlySelected");
            if (isRefreshing == null) isRefreshing = savedInstanceState.getBoolean("isRefreshing");
            isInfoDialog = savedInstanceState.getBoolean("isInfoDialog");
            isLogoutDialog = savedInstanceState.getBoolean("isLogoutDialog");

            if (isInfoDialog) showInfoDialog();
            if (isLogoutDialog) showLogoutDialog();

        } else {

            //App starten
            currentlySelected = -1;
            initializeLoadingData();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (loggedIn) {
            showStartScreen();
            loggedIn = false;
        }
    }

    private void buildDrawer() {

        String email = PreferenceReader.readStringFromPreferences(this, EssenAPI.PREF_EMAIL, "");

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withSelectionListEnabledForSingleProfile(false)
                .withHeaderBackground(getResources().getDrawable(R.drawable.spaghetti))
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(email)
                                .withIcon(getResources().getDrawable(R.drawable.logo))
                )
                .withTextColorRes(R.color.primary_text)
                .build();

        navigationDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Meine Bestellungen").withIcon(R.drawable.ic_check).withIconTintingEnabled(true).withIdentifier(1),
                        new PrimaryDrawerItem().withName("Speiseplan").withIcon(R.drawable.ic_restaurant).withIconTintingEnabled(true).withIdentifier(2),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Infos").withIcon(R.drawable.ic_info).withIconTintingEnabled(true).withIdentifier(11),
                        new PrimaryDrawerItem().withName("Abmelden").withIcon(R.drawable.ic_arrow_back).withIconTintingEnabled(true).withIdentifier(12)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long l, IDrawerItem drawerItem) {
                        int identifier = drawerItem.getIdentifier();
                        if (identifier < 10) {
                            if (currentlySelected != position) {
                                setFragment(identifier);
                                currentlySelected = position;
                                return false;
                            }
                        } else {
                            navigationDrawer.setSelection(currentlySelected, false);
                            switch (identifier) {
                                case 11:
                                    showInfoDialog();
                                    break;
                                case 12:
                                    showLogoutDialog();
                            }
                        }
                        return true;
                    }
                })
                .build();

    }

    /**
     * Zeigt den Info Dialog
     */
    private void showInfoDialog() {

        //Analytics
        MyApplication analytics = (MyApplication) getApplication();
        analytics.fireEvent("NavDrawer", "Infos");

        isInfoDialog = true;

        LayoutInflater inflater = getLayoutInflater();
        View scrollView = inflater.inflate(R.layout.infotext_dialog, null);
        TextView textView = (TextView) scrollView.findViewById(R.id.textView);
        textView.setText(Html.fromHtml(getString(R.string.infoDialog_text)));
        textView.setMovementMethod(LinkMovementMethod.getInstance()); //Link klickbar machen

        AlertDialog.Builder infoDialogB = new AlertDialog.Builder(this);
        infoDialogB.setView(scrollView);
        infoDialogB.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                isInfoDialog = false;
            }
        });
        infoDialogB.show();
    }

    /**
     * Zeigt den Logout Dialog
     */
    public void showLogoutDialog() {

        //Analytics
        MyApplication analytics = (MyApplication) getApplication();
        analytics.fireEvent("NavDrawer", "Abmelden");

        isLogoutDialog = true;

        String email = PreferenceReader.readStringFromPreferences(getApplicationContext(), EssenAPI.PREF_EMAIL, "");

        AlertDialog.Builder abmeldeDialogB = new AlertDialog.Builder(this);
        abmeldeDialogB.setTitle("Abmelden");
        abmeldeDialogB.setMessage("Sicher, dass du deinen Account " + email + " abmelden möchtest?");
        abmeldeDialogB.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PreferenceReader.saveStringToPreferences(getApplicationContext(), EssenAPI.PREF_EMAIL, EssenAPI.PREF_NULL);
                PreferenceReader.saveStringToPreferences(getApplicationContext(), EssenAPI.PREF_PASSWORD, EssenAPI.PREF_NULL);
                speiseplanFragment = null;
                bestellungenFragment = null;
                isLogoutDialog = false;

                initializeLoadingData();
            }
        });
        abmeldeDialogB.setNegativeButton("Abbrechen", null);
        abmeldeDialogB.show();

    }

    /**
     * Wechselt das angezeigte Fragment
     *
     * @param identifier der Fragment identifier
     */
    private void setFragment(int identifier) {

        FragmentManager mFragmentManager = getSupportFragmentManager();
        if (identifier == 1) {
            if (bestellungenFragment == null) {
                bestellungenFragment = new BestellungenFragment();
            }
            mFragmentManager.beginTransaction().replace(R.id.container, bestellungenFragment).commit();
            getSupportActionBar().setTitle("Meine Bestellungen");
        } else if (identifier == 2) {
            if (speiseplanFragment == null) {
                speiseplanFragment = new SpeiseplanFragment();
            }
            mFragmentManager.beginTransaction().replace(R.id.container, speiseplanFragment).commit();
            getSupportActionBar().setTitle("Speiseplan");
        }
    }

    /**
     * wechselt zum vom Nutzer als Startscreen festgelegten Fragment
     */
    private void showStartScreen() {

        //Ansonsten zum Stundenplan springen
        int startScreen = Integer.parseInt(PreferenceReader.readStringFromPreferences(this, "startScreen", "0"));
        navigationDrawer.setSelection(startScreen, false);
        currentlySelected = startScreen;
        setFragment(startScreen + 1);

    }

    /**
     * Leitet das Laden der Daten ein; Prüft ob Benutzerdaten vorhanden sind und öffnet falls nötig die LoginActivity
     */
    private void initializeLoadingData() {

        //Wenn nicht eingeloggt, LoginActivity starten
        if (PreferenceReader.readStringFromPreferences(this, EssenAPI.PREF_EMAIL, EssenAPI.PREF_NULL).equals(EssenAPI.PREF_NULL)) {

            Intent startLoginIntent = new Intent(this, LoginActivity.class);
            final int result = 1;
            startActivityForResult(startLoginIntent, result);

            eventBus.register(this);
        } else {
            //Ansonsten Daten laden
            loadData();
        }
    }

    /**
     * Leitet das Anzeigen der Daten ein
     * wird gecalled, wenn die LoadingActivity Daten sendet - z.B. beim ersten Start der App
     */
    public void onEvent(EssenAPI event) {

        essenAPI = event;
        eventBus.unregister(this);

        loggedIn = true;

        //Daten als Saved Session speichern
        essenAPI.saveToFile(this);

    }

    /**
     * Läd die Daten entweder aus dem Speicher oder mit der LoadingActivity
     * verfährt außerdem nach dem vom Nutzer in den Einstellungen festgelegten Regeln zum weiteren Laden
     */
    private void loadData() {

        //Schauen on eine SavedSession im Speicher ist
        final File savedSessionFile = new File(getFilesDir(), EssenAPI.SAVE_FILE_NAME);
        if (savedSessionFile.exists()) {

            String eMail = PreferenceReader.readStringFromPreferences(this, EssenAPI.PREF_EMAIL, "");
            String password = PreferenceReader.readStringFromPreferences(this, EssenAPI.PREF_PASSWORD, "");

            //Saved Session laden
            final MainActivity context = this;
            EssenAPI.createFromFile(
                    this, eMail, password, new EssenAPI.CreateFromFileHandler() {
                        @Override
                        public void onCreated(EssenAPI myEssenAPI) {

                            //Wenn die Tage nicht mehr aktuell sind, neue laden
                            if (myEssenAPI.getEssenTagList().size() == 0) {
                                savedSessionFile.delete();
                                loadData();
                                return;
                            }

                            //wenn es fertig ist, Fragment öffnen
                            essenAPI = myEssenAPI;
                            showStartScreen();

                            //Daten aktualisieren aus dem Interwebs
                            boolean doRefresh = PreferenceReader.readBooleanFromPreferences(context, "doRefreshAtStart", true);
                            if (doRefresh) {

                                isRefreshing = true;
                                showRefresh();
                                downloadData();
                            }

                        }

                        @Override
                        public void onError(Throwable throwable) {

                            //Falls es einnen Fehler gab (z.B. neue App Version nicht mit Saved Session kompatibel), neu herunterladen
                            Log.e("JKGV", "Error loading data from storage. Redownload it...");
                            throwable.printStackTrace();
                            savedSessionFile.delete();
                            loadData();
                        }
                    }
            );

        } else {

            //Daten aus dem Interwebs herunterladen, dazu LoadingActivity starten
            Intent startLoadingIntent = new Intent(this, LoadingActivity.class);
            final int result = 1;
            startActivityForResult(startLoadingIntent, result);

            eventBus.register(this);

        }

    }

    private void downloadData() {

        final MainActivity context = this;
        essenAPI.downloadData(new EssenAPI.AsyncEssenResponseHandler() {

            @Override
            public void onSucess() {
                if (essenAPI.getEssenTagList().size() > 0) {

                    Toast.makeText(context, "Daten erfolgreich aktualisiert", Toast.LENGTH_SHORT).show();
                    essenAPI.saveToFile(context);
                    onFinished();

                } else onOtherError(new Throwable("Konnte keine Informationen laden"));
            }

            @Override
            public void onNoConnection() {

                Toast.makeText(context, "Keine Verbindung zum Server!", Toast.LENGTH_LONG).show();
                onFinished();

            }

            @Override
            public void onNoAccess() {

                //TODO

            }

            @Override
            public void onOtherError(Throwable throwable) {

                //Wenn es hier ein Error gibt, hat sich warscheinlich das Online System geändert
                Log.e("JKGDEBUG", "Error bei der Verarbeitung der Daten");
                throwable.printStackTrace();
                showErrorDialog(context, throwable.getMessage());

            }

            private void onFinished() {

                stopRefresh();
                isRefreshing = false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        if (data == null) return;

        String response = data.getStringExtra("ExitCode");
        switch (response) {
            case "Exit":            //Anwendung schließen, falls von Activity gewünscht
                finish();
                break;
            case "ReLog":           //Falls die Loading Activity neu einloggen will
                relog();
                eventBus.unregister(this);
                break;
        }
    }

    /**
     * Löscht die Benutzerdaten und zeigt LoginActivity
     */
    private void relog() {

        //Benutzerdaten leeren und LoginActivity starten
        PreferenceReader.saveStringToPreferences(this, EssenAPI.PREF_EMAIL, EssenAPI.PREF_NULL);
        PreferenceReader.saveStringToPreferences(this, EssenAPI.PREF_PASSWORD, EssenAPI.PREF_NULL);
        initializeLoadingData();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {

        return essenAPI;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        this.menu = menu; //Menu für alle Methoden verfügbar machen

        if (isRefreshing) { //Falls noch Daten geladen werden, das auch zeigen
            showRefresh();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Wenn refresh geklickt wurde
        if (id == R.id.action_refresh) {
            refreshClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Wird gecallt, wenn auf refresh geklickt wurde
     */
    private void refreshClicked() {

        if (isRefreshing) {
            return;
        }

        //Analytics
        MyApplication analytics = (MyApplication) getApplication();
        //analytics.fireEvent("Toolbar", "Refresh");

        showRefresh();
        isRefreshing = true;

        //Daten im Hintergrund laden
        downloadData();

    }

    /**
     * Animiert das Refresh MenuItem
     */
    private void showRefresh() {

        if (menu == null) return;
        MenuItem item = menu.findItem(R.id.action_refresh);

        //Das Refresh Item durch ein ImageView, was sich dreht austauschen
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_icon, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        item.setActionView(iv);

    }

    /**
     * Stoppt die Animation des Refresh Menu Items
     */
    private void stopRefresh() {

        if (menu == null) return;
        MenuItem item = menu.findItem(R.id.action_refresh);

        //Das Refresh Item zurücktauschen
        item.getActionView().clearAnimation();
        item.setActionView(null);

    }

    @Override
    public void onBackPressed() {

        //Wenn der Drawer noch offen ist, erst ihn schließen, dann beenden
        if (navigationDrawer != null && navigationDrawer.isDrawerOpen()) {
            navigationDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt("selection", navigationDrawer.getCurrentSelection());
        outState.putInt("currentlySelected", currentlySelected);
        outState.putCharSequence("title", toolbar.getTitle());
        outState.putBoolean("isRefreshing", isRefreshing);
        outState.putBoolean("isInfoDialog", isInfoDialog);
        outState.putBoolean("isLogoutDialog", isLogoutDialog);
        super.onSaveInstanceState(outState);
    }
}
