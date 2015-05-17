package de.conradowatz.essenimkepler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;


public class MainActivity extends MaterialNavigationDrawer implements MaterialSectionListener {

    public String html;
    private BestellungenFragment bestellungenFragment = new BestellungenFragment();
    private SpeiseplanFragment speiseplanFragment = new SpeiseplanFragment();
    private MaterialSection abmeldenSection;
    private  AlertDialog abmeldeDialog;

    @Override
    public void init(Bundle bundle) {

        //Sections zum NavDrawer hinzufügen
        addSection( newSection("Meine Bestellungen", bestellungenFragment) );
        addSection( newSection("Speiseplan", speiseplanFragment) );

        abmeldenSection = newSection("Abmelden", this);
        addBottomSection(abmeldenSection);

        //Drawer Bilder anzeigen
        String email = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "");
        MaterialAccount account = new MaterialAccount(this.getResources(),email,"",R.drawable.logo, R.drawable.spaghetti);
        this.addAccount(account);

        if (bundle!=null) {
            //bei resume html aus bundle holen
            html = bundle.getString("essenListeHtml");
        } else {
            checkForFirstStart();
        }

    }

    @Override
    public void onClick(MaterialSection section) {
        super.onClick(section);

        if (section==abmeldenSection) {

            if ((abmeldeDialog != null) && (abmeldeDialog.isShowing())) {
                return;
            }
            String email = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "");

            AlertDialog.Builder abmeldeDialogB = new AlertDialog.Builder(this);
            abmeldeDialogB.setTitle("Abmelden");
            abmeldeDialogB.setMessage("Sicher, dass du deinen Account " + email + " abmelden möchtest?");
            abmeldeDialogB.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PreferenceReader.saveStringToPreferences(getApplicationContext(), "loginEMail", "FALSE");
                    PreferenceReader.saveStringToPreferences(getApplicationContext(), "loginPW", "");
                    checkForFirstStart();
                }
            });
            abmeldeDialogB.setNegativeButton("Abbrechen", null);
            abmeldeDialog = abmeldeDialogB.show();
        }

    }

    private void checkForFirstStart() {

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {


        String response = data.getStringExtra("ExitCode");
        if (response.equals("Exit")) {                  //Anwendung schließen, falls von Activity gewünscht
            finish();
        } else if (response.equals("LoggedIn")) {       //Anzeige von Fragments starten wenn eingeloggt

            new VersionUpdater(this).start();

            html = data.getStringExtra("html");
            bestellungenFragment.displayInfo(html);

            //E-Mail in Drawer anzeigen
            String email = PreferenceReader.readStringFromPreferences(getApplicationContext(), "loginEMail", "");
            setUsername(email);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        //Essenliste speichern
        outState.putString("essenListeHtml", html);

        super.onSaveInstanceState(outState);
    }

    public String getHtml() {
        return html;
    }
}
