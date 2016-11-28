package com.peaceb.getfreebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private CheckBox _checkNotify = null;
    private CheckBox _checkAutoClaim = null;
    private CheckBox _checkUseVibrate = null;
    private CheckBox _checkUseSound = null;
    private TextView _textLoginInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _checkNotify = (CheckBox)findViewById(R.id.checkNoti);
        _checkAutoClaim = (CheckBox)findViewById(R.id.checkAutoClaim);
        _checkUseVibrate = (CheckBox)findViewById(R.id.checkUseVibrate);
        _checkUseSound = (CheckBox)findViewById(R.id.checkUseSound);
        _textLoginInfo = (TextView)findViewById(R.id.textLoginInfo);

        _checkNotify.setOnClickListener(this);
        _checkAutoClaim.setOnClickListener(this);
        _checkUseVibrate.setOnClickListener(this);
        _checkUseSound.setOnClickListener(this);

        loadSettings();

        findViewById(R.id.textLinkHome).setOnClickListener(this);
        findViewById(R.id.textLinkFreeBook).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkNoti: {
                boolean checked = _checkNotify.isChecked();
                Setting.set(this, Setting.KEY_USE_NOTIFY_NEWBOOK, checked);
                if (checked) {
                    Setting.startCheckNewBook(this);
                }
                else {
                    Setting.stopCheckNewBook(this);
                    clearUserAccount();
                }
                break;
            }
            case R.id.checkAutoClaim: {
                boolean checked = _checkAutoClaim.isChecked();
                if (checked) {
                    startLoginDialog();
                }
                else {
                    clearUserAccount();
                }
                break;
            }

            case R.id.checkUseVibrate:
                Setting.set(this, Setting.KEY_USE_VIBRATE, _checkUseVibrate.isChecked());
                break;

            case R.id.checkUseSound:
                Setting.set(this, Setting.KEY_USE_SOUND, _checkUseSound.isChecked());
                break;

            case R.id.textLinkHome:
            case R.id.textLinkFreeBook:
                String url = ((TextView)findViewById(v.getId())).getText().toString();
                startBrowser(url);
                break;
        }
    }

    private void loadSettings() {
        boolean checkFirst = Setting.getBoolean(this, Setting.KEY_CHECK_FIRST);
        if (!checkFirst) {
            Log.i(TAG, "first run!");

            Setting.set(this, Setting.KEY_USE_NOTIFY_NEWBOOK, true);
            Setting.startCheckNewBook(this);
            Setting.set(this, Setting.KEY_CHECK_FIRST, true);
        }

        boolean notifyNewbook = Setting.getBoolean(this, Setting.KEY_USE_NOTIFY_NEWBOOK);
        boolean autoClaim = Setting.getBoolean(this, Setting.KEY_USE_AUTO_CLAIM);

        _checkNotify.setChecked(notifyNewbook);
        _checkAutoClaim.setChecked(autoClaim);
        _checkUseVibrate.setChecked(Setting.getBoolean(this, Setting.KEY_USE_VIBRATE));
        _checkUseSound.setChecked(Setting.getBoolean(this, Setting.KEY_USE_SOUND));

        if (autoClaim) {
            String email = Setting.get(this, Setting.KEY_EMAIL);
            _textLoginInfo.setText("LOGIN: " + email);
        }
        else {
            _textLoginInfo.setText("");
        }
    }

    private boolean saveUserAccount(String email, String pass) {
        if (email.length() == 0 || pass.length() == 0) {
            Toast.makeText(this, R.string.check_input, Toast.LENGTH_SHORT).show();
            return false;
        }

        Setting.set(this, Setting.KEY_EMAIL, email);
        Setting.set(this, Setting.KEY_PASS, Setting.encrypt(this, pass));

        Setting.set(MainActivity.this, Setting.KEY_USE_AUTO_CLAIM, true);

        // clear saved book title, you can claim the book on the next attempt.
        Setting.remove(this,  Setting.KEY_LAST_BOOK_TITLE);

        _checkAutoClaim.setChecked(true);
        _textLoginInfo.setText("LOGIN: " + email);

        Setting.stopCheckNewBook(this);
        Setting.startCheckNewBook(this);

        return true;
    }

    private void clearUserAccount() {
        Setting.remove(this, Setting.KEY_EMAIL);
        Setting.remove(this, Setting.KEY_PASS);

        Setting.set(MainActivity.this, Setting.KEY_USE_AUTO_CLAIM, false);
        _checkAutoClaim.setChecked(false);
        _textLoginInfo.setText("");
    }

    private void startLoginDialog() {
        if (!_checkNotify.isChecked()) {
            clearUserAccount();
            return;
        }

        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_login_info);
        d.setTitle(R.string.title_login_setting);

        final EditText editEmail = (EditText)d.findViewById(R.id.editEmail);
        final EditText editPass = (EditText)d.findViewById(R.id.editPass);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.buttonOk:
                        String email = editEmail.getText().toString().trim();
                        String pass = editPass.getText().toString().trim();
                        if (saveUserAccount(email, pass)) {
                            d.dismiss();
                        }
                        break;

                    case R.id.buttonCancel:
                        clearUserAccount();
                        d.dismiss();
                        break;
                }
            }
        };

        d.findViewById(R.id.buttonOk).setOnClickListener(clickListener);
        d.findViewById(R.id.buttonCancel).setOnClickListener(clickListener);
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                clearUserAccount();
            }
        });

        d.show();
    }

    private void startBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}



