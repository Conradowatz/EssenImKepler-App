package de.conradowatz.essenimkepler;


import android.os.AsyncTask;

import java.util.List;

public abstract class EssenStorage {

    List<EssenTag> essenListe;

    public List<EssenTag> getEssenListe() {
        return essenListe;
    }

    public void passHtml(String html) {
        essenListe = EssenAPI.parseHTML(html);
        onInfo();
    }

    public void downloadInfo(String email, String password) {

        /*EssenAPI.login(email, password, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                onNoConnection();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                essenListe = EssenAPI.parseHTML(responseString);
                onInfo();
            }
        });*/

        new AsyncTask<String, String, List<EssenTag>>() {

            @Override
            protected List<EssenTag> doInBackground(String... params) {
                essenListe = EssenAPI.parseHTML(HTMLtest.html);
                return essenListe;
            }

            @Override
            protected void onPostExecute(List<EssenTag> essenTags) {
                onInfo();
            }
        }.execute();

    }

    protected abstract void onInfo();

    protected abstract void onNoConnection();


}
