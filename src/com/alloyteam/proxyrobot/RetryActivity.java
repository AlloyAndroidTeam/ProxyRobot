package com.alloyteam.proxyrobot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by pelli on 2015/12/23.
 */
public class RetryActivity extends Activity implements DialogInterface.OnClickListener {

    public static final String KEY_CONFIG = "config";
    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";

    private Dialog mDialog = null;
    private EditText mEt;
    private WifiConfiguration mConfig;
    private String mHost;
    private String mPort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mConfig = intent.getParcelableExtra(KEY_CONFIG);
        if (mConfig == null) {
            finish();
            return;
        }
        mHost = intent.getStringExtra(KEY_HOST);
        mPort = intent.getStringExtra(KEY_PORT);
        setContentView(R.layout.activity_retry);
        ((TextView)findViewById(R.id.ssid)).setText(mConfig.SSID);
        mEt = (EditText) findViewById(R.id.password);
        if (needPassword(mConfig)) {
            mEt.setVisibility(View.VISIBLE);
        }
        if (ProxyUtils.isNetworkExist(mConfig)) {
            deleteExistNetwork();
        } else {
            addNewNetwork();
        }
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ProxyUtils.isNetworkExist(mConfig)) {
            deleteExistNetwork();
        } else {
            addNewNetwork();
        }
    }

    private void deleteExistNetwork() {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.set_proxy_lollipop, mConfig.SSID, needPassword(mConfig) ? getString(R.string.set_proxy_need_password) : ""))
                    .setPositiveButton(R.string.ok, this)
                    .setNegativeButton(R.string.cancel, this)
                    .setCancelable(false)
                    .create();
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private static boolean needPassword(WifiConfiguration config) {
        return config.wepKeys[0] != null || config.preSharedKey != null;
    }

    private void addNewNetwork() {
        if (!needPassword(mConfig)) {
            if (ProxyUtils.connectToNewNetwork(mConfig, null) == ProxyUtils.RESULT_OK) {
                Toast.makeText(getApplicationContext(), getString(R.string.set_proxy_success, mHost, mPort), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.set_proxy_fail, Toast.LENGTH_LONG).show();
            }
            finish();
        } else {
            mEt.requestFocus();
            mEt.selectAll();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                String password = mEt.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mEt.requestFocus();
                    mEt.selectAll();
                } else {
                    if (ProxyUtils.connectToNewNetwork(mConfig, password) == ProxyUtils.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), getString(R.string.set_proxy_success, mHost, mPort), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.set_proxy_fail, Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                finish();
                break;
        }
    }
}