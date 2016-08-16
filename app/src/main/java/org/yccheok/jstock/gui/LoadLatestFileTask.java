package org.yccheok.jstock.gui;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by yccheok on 16/8/2016.
 */
public class LoadLatestFileTask extends AsyncTask<Void, String, Boolean> {
    @Override
    protected Boolean doInBackground(Void... voids) {
        for (int i = 0; i < 10; i++) {
            this.publishProgress("--> " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
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

