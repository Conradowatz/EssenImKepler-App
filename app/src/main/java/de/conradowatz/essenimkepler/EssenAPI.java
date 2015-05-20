package de.conradowatz.essenimkepler;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

public class EssenAPI {

    public static int LOGIN_SUCESS = 90;
    public static int LOGIN_FAILED = 91;
    public static int NO_CONNECTION = 92;

    private AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

    public void login(final String email, final String password, final AsyncHttpResponseHandler responseHandler) {

        //Verbindung aufbauen
        client.get("https://essen-im-kepler.de/order.jsp", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                //Anmeldedaten senden
                RequestParams requestParams = new RequestParams();
                requestParams.put("j_username", email);
                requestParams.put("j_password", password);
                client.post("https://essen-im-kepler.de/j_security_check", requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        //Antwort verarbeiten
                        client.get("https://essen-im-kepler.de/order.jsp", responseHandler);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        error.printStackTrace();
                        responseHandler.onFailure(NO_CONNECTION, null, null, new Throwable("No connection, Security Check!"));
                    }
                });

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                responseHandler.onFailure(NO_CONNECTION, null, null, new Throwable("No connection, Main Server!"));
            }
        });


    }

    public static List<EssenTag> parseHTML(String html) {

        List<EssenTag> essenListe = new ArrayList<>();

        String[] dayParts = html.split("DAYSPLITTER");
        for (int j = 1; j<dayParts.length-1; j++) {

            EssenTag thisDay = new EssenTag();

            String dayPart = dayParts[j];
            thisDay.datum = dayPart.substring(dayPart.indexOf("etCoDat")+10, dayPart.indexOf("</td>", dayPart.indexOf("etCoDat")+10)).replaceAll("<br />", ", ");

            String[] mealParts = dayPart.split("MEALSPLITTER");
            for (int i = 0; i<mealParts.length; i++) {

                String mealPart = mealParts[i];
                Essen thisMeal = new Essen();

                if (mealPart.contains("checked")) { thisDay.selected = i; }
                thisMeal.desc = mealPart.substring(mealPart.indexOf("etCoDe")+10, mealPart.indexOf("</td>", mealPart.indexOf("etCoDe")+10));
                thisMeal.price = mealPart.substring(mealPart.indexOf("etCoPri")+10, mealPart.indexOf("</td>", mealPart.indexOf("etCoPri")+10));

                thisDay.essens.add(thisMeal);

            }

            essenListe.add(thisDay);

        }

        return essenListe;
    }

    public void reCreateClient() {
        client = new AsyncHttpClient(true, 80, 443);
    }

}
