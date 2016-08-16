package org.yccheok.demo;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by yccheok on 16/8/2016.
 */
public class LoadLatestFileTask extends AsyncTask<Void, String, Boolean> {
    @Override
    protected Boolean doInBackground(Void... voids) {
        Utils.CloudFile cloudFile = Utils.loadFromGoogleDrive(googleApiClient);

        String fileName = cloudFile.checksum + ".TXT";
        String content = readFile(cloudFile.file);

        Utils.showLongToast("LOAD " + fileName + " with content " + content);
        return true;
    }

    private String readFile(File file) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (IOException ex) {
        } finally {
            try {
                br.close();
            } catch (IOException ex) {}
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progressMessages) {
        for (String progressMessage : progressMessages) {
            this.loadLatestFileTaskFragment.onProgressUpdate(progressMessage);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        this.loadLatestFileTaskFragment.onPostExecute(result);
    }

    public LoadLatestFileTask(LoadLatestFileTaskFragment loadLatestFileTaskFragment, GoogleApiClient googleApiClient) {
        this.loadLatestFileTaskFragment = loadLatestFileTaskFragment;
        this.googleApiClient = googleApiClient;
    }

    private final LoadLatestFileTaskFragment loadLatestFileTaskFragment;
    private final GoogleApiClient googleApiClient;
}

