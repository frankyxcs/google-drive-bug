package org.yccheok.jstock.gui;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by yccheok on 16/8/2016.
 */
public class SaveFile456Task extends AsyncTask<Void, String, Boolean> {
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
            this.saveFile456TaskFragment.onProgressUpdate(progressMessage);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        this.saveFile456TaskFragment.onPostExecute(result);
    }

    public SaveFile456Task(SaveFile456TaskFragment saveFile456TaskFragment, GoogleApiClient googleApiClient) {
        this.saveFile456TaskFragment = saveFile456TaskFragment;
        this.googleApiClient = googleApiClient;
    }

    private final SaveFile456TaskFragment saveFile456TaskFragment;
    private final GoogleApiClient googleApiClient;
}

