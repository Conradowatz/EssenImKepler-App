package de.conradowatz.essenimkepler;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;

public class VersionUpdater {

    private Context context;
    private long downloadID = -1L;
    private String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);

    private static AsyncHttpClient client = new AsyncHttpClient();

    public void start() {
        client.get("http://conradowatz.de/apps/essenimkepler/versioninfo.php", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int latestVersion = response.getInt("latest");
                    String download = response.getString("download");

                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    int versionNumber = pInfo.versionCode;

                    if (latestVersion>versionNumber) {
                        showUpdateDialog(download);
                    };

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public VersionUpdater(Context context) {
        this.context = context;
    }

    private void showUpdateDialog(final String downloadPath) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Versions Update");
        String updateMessage = "Es gibt eine neue Version der Essen im Kepler App. \nMöchtest du sie jetzt herunterladen?";
        builder.setMessage(updateMessage);
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                startDownload(downloadPath);

            }
        });


        builder.setNegativeButton("Nein, später", null);
        builder.show();


    }

    private void startDownload(String downloadLink) {
        Uri uri = Uri.parse(downloadLink);

        Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();

        File file = new File(Environment.getExternalStorageDirectory() + "/download/" + "eikupdate.apk");
        file.delete();

        if (file.exists()) {
            startDownload(downloadLink);
            return;
        }

        context.registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        downloadID = downloadManager.enqueue(new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("Versions Update")
                .setDescription("Essen im Kepler wird heruntergeladen...")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        "eikupdate.apk"));


    }

    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != downloadID) {
                return;
            }
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (!cursor.moveToFirst()) {
                return;
            }

            File downloadFile = new File(Environment.getExternalStorageDirectory() + "/download/" + "eikupdate.apk");
            if (downloadFile.exists()) {
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                openIntent.setDataAndType(Uri.fromFile(downloadFile), "application/vnd.android.package-archive");
                openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(openIntent);
            } else {
                Toast.makeText(context, "Download Fehler!", Toast.LENGTH_LONG).show();
            }


        }
    };

}