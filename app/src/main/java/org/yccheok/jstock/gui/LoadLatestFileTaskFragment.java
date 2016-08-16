package org.yccheok.jstock.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.api.GoogleApiClient;

public class LoadLatestFileTaskFragment extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this instance so it isn't destroyed when MainActivity and MainFragment change
        // configuration.
        setRetainInstance(true);

        loadLatestFileTask = new LoadLatestFileTask(this, googleApiClient);
        // Should we use executeOnExecutor? As if previous loadFromCloudTask was stuck, the new
        // upcoming loadFromCloudTask won't run by using execute.
        loadLatestFileTask.execute();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.progressDialog = new ProgressDialog(this.getActivity());
        this.progressDialog.setMessage(progressMessage);
        this.progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    // http://stackoverflow.com/questions/11307390/dialogfragment-disappears-on-rotation-despite-setretaininstancetrue
    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if ((dialog != null) && getRetainInstance())
            dialog.setDismissMessage(null);

        super.onDestroyView();
    }

    // This is called by the AsyncTask.
    public void onProgressUpdate(String progressMessage) {
        this.progressDialog.setMessage(progressMessage);
        // Store it to handle configuration change.
        this.progressMessage = progressMessage;
    }

    @Override
    public void onResume() {
        super.onResume();

        // This is a little hacky, but we will see if the task has finished while we weren't
        // in this activity, and then we can dismiss ourselves.
        if (loadLatestFileTask == null) {
            dismiss();
        }
    }

    // Also when we are dismissed we need to cancel the task.
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // If true, the thread is interrupted immediately, which may do bad things.
        // If false, it guarantees a result is never returned (onPostExecute() isn't called)
        // but you have to repeatedly call isCancelled() in your doInBackground()
        // function to check if it should exit. For some tasks that might not be feasible.
        if (loadLatestFileTask != null) {
            loadLatestFileTask.cancel(true);
        }

        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @SuppressLint("NewApi")
    public void onPause() {
        super.onPause();

        Activity activity = this.getActivity();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (activity != null && false == activity.isChangingConfigurations()) {
                // Stop the thread when home is pressed.
                dismiss();
            }
        } else {
            // I have no idea how to perform configuration checking in older API
            // Just do nothing, by letting the thread continue to run.
            //
            // http://stackoverflow.com/questions/15458540/calling-activity-ischangingconfigurations-in-fragment-for-api-level-lesser-than
        }
    }

    // This is also called by the AsyncTask.
    public void onPostExecute(Boolean result) {
        // Make sure we check if it is resumed because we will crash if trying to dismiss the dialog
        // after the user has switched to another app.
        if (isResumed()) {
            dismiss();
        }

        // If we aren't resumed, setting the task to null will allow us to dismiss
        // ourselves in onResume().
        loadLatestFileTask = null;
    }

    public static LoadLatestFileTaskFragment newInstance(GoogleApiClient googleApiClient) {
        LoadLatestFileTaskFragment laveFile123TaskFragment = new LoadLatestFileTaskFragment();

        // Seems dangerous in first place as we don't assign it to bundle. But it is OK as our
        // context due to setRetainInstance(true)
        laveFile123TaskFragment.googleApiClient = googleApiClient;

        return laveFile123TaskFragment;
    }

    private GoogleApiClient googleApiClient;
    private LoadLatestFileTask loadLatestFileTask;
    private ProgressDialog progressDialog;
    private String progressMessage = "";

    private static final String TAG = "SaveFile123TaskFragment";
}
