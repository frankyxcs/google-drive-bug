package org.yccheok.demo;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity implements GoogleApiClientFragment.ConnectionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.yccheok.jstock.gui.R.layout.activity_main);

        Button button1 = (Button)this.findViewById(org.yccheok.jstock.gui.R.id.button1);
        Button button2 = (Button)this.findViewById(org.yccheok.jstock.gui.R.id.button2);
        Button button3 = (Button)this.findViewById(org.yccheok.jstock.gui.R.id.button3);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFile123();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFile456();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLatestFile();
            }
        });
    }

    private void saveFile123() {
        startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                REQUEST_ACCOUNT_PICKER_SAVE_FILE_123);
    }

    private void saveFile456() {
        startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                REQUEST_ACCOUNT_PICKER_SAVE_FILE_456);
    }

    private void loadLatestFile() {
        startActivityForResult(AccountPicker.newChooseAccountIntent(null,
                null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                REQUEST_ACCOUNT_PICKER_LOAD_LATEST_FILE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER_SAVE_FILE_123:
            case REQUEST_ACCOUNT_PICKER_SAVE_FILE_456:
            case REQUEST_ACCOUNT_PICKER_LOAD_LATEST_FILE:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                    if (accountName != null && accountType != null) {
                        GoogleApiClientFragment googleApiClientFragment;

                        if (requestCode == REQUEST_ACCOUNT_PICKER_SAVE_FILE_123) {
                            googleApiClientFragment = GoogleApiClientFragment.newInstance(accountName, ACTION_SAVE_FILE_123);
                        } else if (requestCode == REQUEST_ACCOUNT_PICKER_SAVE_FILE_456) {
                            googleApiClientFragment = GoogleApiClientFragment.newInstance(accountName, ACTION_SAVE_FILE_456);
                        } else {
                            googleApiClientFragment = GoogleApiClientFragment.newInstance(accountName, ACTION_LOAD_LATEST_FILE);
                        }

                        FragmentManager fm = this.getSupportFragmentManager();
                        Fragment oldFragment = fm.findFragmentByTag(GOOGLE_API_CLIENT_FRAGMENT);
                        if (oldFragment != null) {
                            fm.beginTransaction().remove(oldFragment).commitAllowingStateLoss();
                        }
                        fm.beginTransaction().add(googleApiClientFragment, GOOGLE_API_CLIENT_FRAGMENT).commitAllowingStateLoss();
                    }
                }
        }
    }

    @Override
    public void onConnected(GoogleApiClient googleApiClient, int action) {
        // It is our responsible to call googleApiClient.disconnect, not GoogleApiClientFragment.

        if (action == ACTION_SAVE_FILE_123) {
            SaveFile123TaskFragment saveFile123TaskFragment = SaveFile123TaskFragment.newInstance(googleApiClient);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(saveFile123TaskFragment, SAVE_FILE_123_TASK_FRAGMENT).commitAllowingStateLoss();

        } else if (action == ACTION_SAVE_FILE_456) {
            SaveFile456TaskFragment saveFile456TaskFragment = SaveFile456TaskFragment.newInstance(googleApiClient);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(saveFile456TaskFragment, SAVE_FILE_456_TASK_FRAGMENT).commitAllowingStateLoss();

        } else {
            assert(action == ACTION_LOAD_LATEST_FILE);

            LoadLatestFileTaskFragment loadLatestFileTaskFragment = LoadLatestFileTaskFragment.newInstance(googleApiClient);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(loadLatestFileTaskFragment, LOAD_LATEST_FILE_TASK_FRAGMENT).commitAllowingStateLoss();
        }

        FragmentManager fm = this.getSupportFragmentManager();
        Fragment oldFragment = fm.findFragmentByTag(GOOGLE_API_CLIENT_FRAGMENT);
        if (oldFragment != null) {
            fm.beginTransaction().remove(oldFragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onCancel(int action) {
        FragmentManager fm = this.getSupportFragmentManager();
        Fragment oldFragment = fm.findFragmentByTag(GOOGLE_API_CLIENT_FRAGMENT);
        if (oldFragment != null) {
            fm.beginTransaction().remove(oldFragment).commitAllowingStateLoss();
        }
    }

    private static final int REQUEST_ACCOUNT_PICKER_SAVE_FILE_123 = 0;
    private static final int REQUEST_ACCOUNT_PICKER_SAVE_FILE_456 = 1;
    private static final int REQUEST_ACCOUNT_PICKER_LOAD_LATEST_FILE = 2;

    public static final int ACTION_SAVE_FILE_123 = 3;
    public static final int ACTION_SAVE_FILE_456 = 4;
    public static final int ACTION_LOAD_LATEST_FILE = 5;

    public static final int REQUEST_GOOGLE_API_CLIENT_CONNECT = 6;

    private static final String GOOGLE_API_CLIENT_FRAGMENT = "GOOGLE_API_CLIENT_FRAGMENT";

    private static final String SAVE_FILE_123_TASK_FRAGMENT = "SAVE_FILE_123_TASK_FRAGMENT";
    private static final String SAVE_FILE_456_TASK_FRAGMENT = "SAVE_FILE_456_TASK_FRAGMENT";
    private static final String LOAD_LATEST_FILE_TASK_FRAGMENT = "LOAD_LATEST_FILE_TASK_FRAGMENT";
}
