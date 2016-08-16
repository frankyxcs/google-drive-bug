package org.yccheok.jstock.gui;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by yccheok on 16/8/2016.
 */
public class SaveFile123Task extends AsyncTask<Void, String, Boolean> {
    @Override
    protected Boolean doInBackground(Void... voids) {
        if (Utils.saveToGoogleDrive(googleApiClient, "123")) {
            Utils.showLongToast("Save 123 success :)");
        } else {
            Utils.showLongToast("Save 123 fail :(");
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(String... progressMessages) {
        for (String progressMessage : progressMessages) {
            this.saveFile123TaskFragment.onProgressUpdate(progressMessage);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        this.saveFile123TaskFragment.onPostExecute(result);
    }

    public SaveFile123Task(SaveFile123TaskFragment saveFile123TaskFragment, GoogleApiClient googleApiClient) {
        this.saveFile123TaskFragment = saveFile123TaskFragment;
        this.googleApiClient = googleApiClient;
    }

    private final SaveFile123TaskFragment saveFile123TaskFragment;
    private final GoogleApiClient googleApiClient;
}

