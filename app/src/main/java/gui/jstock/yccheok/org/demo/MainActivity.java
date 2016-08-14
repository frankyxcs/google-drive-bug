package gui.jstock.yccheok.org.demo;

import android.accounts.AccountManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button1 = (Button)this.findViewById(R.id.button1);
        Button button2 = (Button)this.findViewById(R.id.button2);
        Button button3 = (Button)this.findViewById(R.id.button3);

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
                        final Toast toast = Toast.makeText(this, accountName, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
        }
    }

    private static final int REQUEST_ACCOUNT_PICKER_SAVE_FILE_123 = 0;
    private static final int REQUEST_ACCOUNT_PICKER_SAVE_FILE_456 = 1;
    private static final int REQUEST_ACCOUNT_PICKER_LOAD_LATEST_FILE = 2;
}
