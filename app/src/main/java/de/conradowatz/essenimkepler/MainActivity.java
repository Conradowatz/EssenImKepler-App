package de.conradowatz.essenimkepler;

import android.support.v7.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private String html;
    public List<EssenTag> essenListe;
    private BestellungenFragment bestellungenFragment;
    private SpeiseplanFragment speiseplanFragment;
    private AlertDialog abmeldeDialog;

    private AccountHeader.Result accountHeaderResult;
    private Drawer.Result drawerResult;
    private ProfileDrawerItem profileDrawerItem;
    private android.support.v7.widget.Toolbar toolbar;

    private int currentlySelected = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String email = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "");
        profileDrawerItem = new ProfileDrawerItem().withName(email).withEmail("").withIcon(getResources().getDrawable(R.drawable.logo));

        accountHeaderResult = new AccountHeader()
                .withActivity(this)
                .withTextColorRes(R.color.primary_text)
                .withHeaderBackground(R.drawable.spaghetti)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        profileDrawerItem
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        ArrayList<IDrawerItem> footerList = new ArrayList<>();
        footerList.add( new PrimaryDrawerItem().withName("Infos").withIcon(R.drawable.ic_info).withIdentifier(1) );
        footerList.add( new PrimaryDrawerItem().withName("Abmelden").withIcon(R.drawable.ic_arrow_back).withIdentifier(2) );

        drawerResult = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeaderResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Meine Bestellungen").withIcon(R.drawable.ic_bestellungen),
                        new PrimaryDrawerItem().withName("Speiseplan").withIcon(R.drawable.ic_speiseplan)
                )
                .withStickyFooterDivider(true)
                .withStickyDrawerItems(footerList)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (position >= 0) {
                            if (currentlySelected!=position) {
                                setFragment(position);
                                currentlySelected = position;
                            }
                        } else {
                            drawerResult.setSelection(drawerResult.getCurrentSelection());
                            if (drawerItem.getIdentifier() == 1) {
                                showInfoDialog();
                            } else if (drawerItem.getIdentifier() == 2) {
                                logOutClicked();
                            }
                        }
                    }
                })
                .withSelectedItem(0)
                .build();

        if (savedInstanceState!=null) {
            //bei resume html aus bundle holen
            html = savedInstanceState.getString("essenListeHtml");
            essenListe = EssenAPI.parseHTML(html);
            currentlySelected = savedInstanceState.getInt("selectedSection");
            drawerResult.setSelection(currentlySelected);
        } else {
            logIn();
        }

    }

    public void logOutClicked() {

            String email = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "");

            AlertDialog.Builder abmeldeDialogB = new AlertDialog.Builder(this);
            abmeldeDialogB.setTitle("Abmelden");
            abmeldeDialogB.setMessage("Sicher, dass du deinen Account " + email + " abmelden möchtest?");
            abmeldeDialogB.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PreferenceReader.saveStringToPreferences(getApplicationContext(), "loginEMail", "FALSE");
                    PreferenceReader.saveStringToPreferences(getApplicationContext(), "loginPW", "");
                    speiseplanFragment = null;
                    bestellungenFragment = null;
                    logIn();
                }
            });
            abmeldeDialogB.setNegativeButton("Abbrechen", null);
            abmeldeDialog = abmeldeDialogB.show();

    }

    private void logIn() {

        if (PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "FALSE").equals("FALSE")) {

            Intent openLogin = new Intent(this, LoginActivity.class);
            final int result = 1;
            startActivityForResult(openLogin, result);

        } else {

            Intent openLoadingScreen = new Intent(this, LoadingDataActivity.class);
            final int result = 1;
            startActivityForResult(openLoadingScreen, result);
        }
    }

    private void showInfoDialog() {

        LayoutInflater inflater = getLayoutInflater();
        View scrollView = inflater.inflate(R.layout.infotext_dialog, null);
        TextView textView = (TextView) scrollView.findViewById(R.id.infoDialog_textView);
        textView.setText(Html.fromHtml(getString(R.string.infoDialog_text)));
        textView.setMovementMethod(LinkMovementMethod.getInstance()); //Link klickbar machen

        AlertDialog.Builder infoDialogB = new AlertDialog.Builder(this);
        infoDialogB.setView(scrollView);
        infoDialogB.setNeutralButton("Okay", null);
        infoDialogB.show();
    }

    private void showDrawerHelp() {

        boolean isDrawerKnown = Boolean.valueOf(PreferenceReader.readStringFromPreferences(getApplicationContext(), "drawerHelp", "false"));

        if (!isDrawerKnown) {
            PreferenceReader.saveStringToPreferences(getApplicationContext(), "drawerHelp", "true");
            drawerResult.openDrawer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void setFragment(int position) {

        FragmentManager mFragmentManager = getFragmentManager();
        if (position==0) {
            if (bestellungenFragment==null) {
                bestellungenFragment = new BestellungenFragment();
            }
            mFragmentManager.beginTransaction().replace(R.id.mainActivity_container, bestellungenFragment).commit();
            toolbar.setTitle("Meine Bestellungen");
        } else if (position==1) {
            if (speiseplanFragment==null) {
                speiseplanFragment = new SpeiseplanFragment();
            }
            mFragmentManager.beginTransaction().replace(R.id.mainActivity_container, speiseplanFragment).commit();
            toolbar.setTitle("Speiseplan");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {


        String response = data.getStringExtra("ExitCode");
        if (response.equals("Exit")) {                  //Anwendung schließen, falls von Activity gewünscht
            finish();
        } else if (response.equals("LoggedIn")) {       //Anzeige von Fragments starten wenn eingeloggt

            new VersionUpdater(this).start();

            showDrawerHelp();

            html = data.getStringExtra("html");
            essenListe = EssenAPI.parseHTML(html);
            setFragment(0);

            //E-Mail in Drawer anzeigen
            String email = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "");
            profileDrawerItem.setName(email);
            accountHeaderResult.updateProfileByIdentifier(profileDrawerItem);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        //Essenliste speichern
        outState.putString("essenListeHtml", html);
        outState.putInt("selectedSection", drawerResult.getCurrentSelection());
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
