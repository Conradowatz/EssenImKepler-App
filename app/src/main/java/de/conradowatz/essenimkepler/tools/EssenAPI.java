package de.conradowatz.essenimkepler.tools;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.conradowatz.essenimkepler.variables.Essen;
import de.conradowatz.essenimkepler.variables.EssenTag;

public class EssenAPI {

    public static final String PREF_EMAIL = "loginEmail";
    public static final String PREF_PASSWORD = "loginPassword";
    public static final String PREF_NULL = "null";
    public static String SAVE_FILE_NAME = "savedSession.json";
    private AsyncHttpClient client = new AsyncHttpClient(80, 443);
    private ArrayList<EssenTag> essenTagList;
    private String email;
    private String password;

    public EssenAPI(String email, String password) {

        this.email = email;
        this.password = password;
    }

    public static ArrayList<EssenTag> parseHTML(String html) {

        ArrayList<EssenTag> essenListe = new ArrayList<>();

        String[] dayParts = html.split("DAYSPLITTER");
        for (int j = 1; j < dayParts.length - 1; j++) {

            EssenTag thisDay = new EssenTag();

            String dayPart = dayParts[j];
            String datumString = dayPart.substring(dayPart.indexOf("etCoDat") + 10, dayPart.indexOf("</td>", dayPart.indexOf("etCoDat") + 10)).replaceAll("<br />", ", ");
            thisDay.setDatum(datumString);

            String[] mealParts = dayPart.split("MEALSPLITTER");
            for (int i = 0; i < mealParts.length; i++) {

                String mealPart = mealParts[i];
                Essen thisMeal = new Essen();

                if (mealPart.contains("checked")) {
                    thisDay.setSelected(i);
                }

                String description = mealPart.substring(mealPart.indexOf("etCoDe") + 10, mealPart.indexOf("</td>", mealPart.indexOf("etCoDe") + 10));
                thisMeal.setDesc(description);
                String price = mealPart.substring(mealPart.indexOf("etCoPri") + 10, mealPart.indexOf("</td>", mealPart.indexOf("etCoPri") + 10));
                thisMeal.setPrice(price);

                thisDay.getEssenList().add(thisMeal);

            }

            essenListe.add(thisDay);

        }

        return essenListe;
    }

    /**
     * Erstellt eine EssenAPI, wenn diese im AppSpeicher vorhanden ist; läuft in eigenem Thread
     *
     * @param context  die Activity für den handler und als Context für den Zugriff auf den AppSpeicher
     * @param email    E-Mail
     * @param password Passwort
     * @param handler  ein Handler, da die Aktion Zeit in anspruch nimmt
     */
    public static void createFromFile(final Context context, final String email, final String password, final CreateFromFileHandler handler) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Calendar heuteCalendar = Calendar.getInstance();

                try {

                    final EssenAPI essenAPI = new EssenAPI(email, password);

                    //Read Data
                    FileInputStream inputStream = context.openFileInput(SAVE_FILE_NAME);
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder total = new StringBuilder(inputStream.available());
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line);
                    }
                    JSONArray essenTagListArray = new JSONArray(total.toString());

                    ArrayList<EssenTag> essenTagList = new ArrayList<>();

                    for (int i = 0; i < essenTagListArray.length(); i++) {

                        JSONObject essenTagObject = essenTagListArray.getJSONObject(i);
                        EssenTag essenTag = new EssenTag();
                        essenTag.setSelected(essenTagObject.getInt("selected"));
                        String datumString = essenTagObject.getString("datum");
                        essenTag.setDatum(datumString);

                        //Wenn Tag älter als heute, überspringen
                        Date date = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.GERMAN).parse(datumString);
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(date);
                        if (dateCalendar.get(Calendar.DAY_OF_YEAR) < heuteCalendar.get(Calendar.DAY_OF_YEAR))
                            continue;

                        ArrayList<Essen> essenList = new ArrayList<>();
                        for (int j = 0; j < essenTagObject.getJSONArray("essenList").length(); j++) {

                            JSONObject essenObject = essenTagObject.getJSONArray("essenList").getJSONObject(j);
                            Essen essen = new Essen(
                                    essenObject.getString("price"),
                                    essenObject.getString("desc")
                            );
                            essenList.add(essen);
                        }

                        essenTag.setEssenList(essenList);

                        essenTagList.add(essenTag);

                    }

                    essenAPI.setEssenTagList(essenTagList);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handler.onCreated(essenAPI);
                        }
                    });

                } catch (final Exception e) {

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handler.onError(e);
                        }
                    });
                }

            }
        }).start();

    }

    public void downloadData(final AsyncEssenResponseHandler responseHandler) {

        if (email == null || password == null || email.equals("null")) {

            responseHandler.onOtherError(new Throwable("Keine Anmeldedaten gesetzt!"));
            return;
        }

        //Anmeldeseite laden
        client.get("https://essen-im-kepler.de/order.jsp", new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {

                error.printStackTrace();
                responseHandler.onNoConnection();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {

                double progress = bytesToProgress(bytesWritten, totalSize);
                responseHandler.onProgres(progress / 3);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                //falls noch angemeldet
                if (responseString.contains("Angemeldet")) {

                    essenTagList = parseHTML(responseString);
                    responseHandler.onSucess();
                    return;
                }

                //Anmeldedaten senden
                RequestParams requestParams = new RequestParams();
                requestParams.put("j_username", email);
                requestParams.put("j_password", password);

                client.post("https://essen-im-kepler.de/j_security_check", requestParams, new AsyncHttpResponseHandler() {

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        error.printStackTrace();
                        responseHandler.onNoConnection();
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {

                        double progress = bytesToProgress(bytesWritten, totalSize);
                        responseHandler.onProgres(1 / 3 + progress / 3);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        //Antwort verarbeiten
                        client.get("https://essen-im-kepler.de/order.jsp", new TextHttpResponseHandler() {

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                                throwable.printStackTrace();
                                responseHandler.onNoConnection();
                            }

                            @Override
                            public void onProgress(long bytesWritten, long totalSize) {

                                double progress = bytesToProgress(bytesWritten, totalSize);
                                responseHandler.onProgres(2 / 3 + progress / 3);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                                //Verbindung hergestellt

                                //Falsche Benutzerdaten
                                if (responseString.contains("Benutzeranmeldung")) {
                                    responseHandler.onNoAccess();
                                    return;
                                }
                                //Erfolgreich angemeldet
                                if (responseString.contains("Angemeldet")) {

                                    essenTagList = parseHTML(responseString);
                                    responseHandler.onSucess();
                                    return;
                                }

                                responseHandler.onOtherError(new Throwable("Unbekannter Anmeldefehler!"));

                            }
                        });
                    }
                });

            }

        });


    }

    public ArrayList<EssenTag> getEssenTagList() {
        return essenTagList;
    }

    public void setEssenTagList(ArrayList<EssenTag> essenTagList) {
        this.essenTagList = essenTagList;
    }

    /**
     * Wandelt die von HttpClient übergebenen Byte Werte in Progress um
     *
     * @param bytesWritten
     * @param totalSize
     * @return eine Prozent Zahl von 0-100; -1 bei Fehlern
     */
    private double bytesToProgress(long bytesWritten, long totalSize) {

        return (totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : 0;
    }

    /**
     * Speichert den derzeitigen Zustand der EssenAPI in den AppSpeicher
     *
     * @param context Context zum Zugriff auf den App-Speicher
     */
    public void saveToFile(final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    JSONArray essenTagListArray = new JSONArray();

                    for (int i = 0; i < essenTagList.size(); i++) {

                        EssenTag essenTag = essenTagList.get(i);

                        JSONObject essenTagObject = new JSONObject();
                        essenTagObject.put("datum", essenTag.getDatum());
                        essenTagObject.put("selected", essenTag.getSelected());
                        JSONArray essenListArray = new JSONArray();

                        for (int j = 0; j < essenTag.getEssenList().size(); j++) {

                            Essen essen = essenTag.getEssenList().get(j);

                            JSONObject essenObject = new JSONObject();
                            essenObject.put("desc", essen.getDesc());
                            essenObject.put("price", essen.getPrice());

                            essenListArray.put(essenObject);
                        }

                        essenTagObject.put("essenList", essenListArray);
                        essenTagListArray.put(essenTagObject);

                    }


                    //JSON speichern
                    String dataToSave = essenTagListArray.toString();

                    FileOutputStream outputStream = context.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
                    outputStream.write(dataToSave.getBytes(Charset.forName("UTF-8")));
                    outputStream.close();

                } catch (Exception e) {

                    Log.e("ESSENDEBUG", "Error saving session");
                    e.printStackTrace();

                }

            }
        }).start();

    }


    public static abstract class CreateFromFileHandler {

        public abstract void onCreated(EssenAPI essenAPI);

        public abstract void onError(Throwable throwable);

    }

    public static abstract class AsyncEssenResponseHandler {

        public abstract void onSucess();

        public abstract void onNoConnection();

        public abstract void onNoAccess();

        public abstract void onOtherError(Throwable e);

        public void onProgres(double progress) {

        }

    }

}
