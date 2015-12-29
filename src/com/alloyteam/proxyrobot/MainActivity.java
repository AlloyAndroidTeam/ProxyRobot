package com.alloyteam.proxyrobot;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	public static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public void onClick(View v) {
		boolean enabled = false;
		String host = null;
		String port = null;
		switch (v.getId()) {
		case R.id.submitProxy:
			enabled = true;
			host = ((EditText)findViewById(R.id.host)).getText().toString();
			port = ((EditText)findViewById(R.id.port)).getText().toString();
			break;
		case R.id.clearProxy:
			break;
		default:
			return;
		}
		if (enabled && (TextUtils.isEmpty(host) || TextUtils.isEmpty(port))) {
			Toast.makeText(getApplicationContext(), R.string.set_proxy_error_param, Toast.LENGTH_LONG).show();
			return;
		}
		int result = ProxyUtils.setWifiProxy(enabled, host, port);
		if (result == ProxyUtils.RESULT_OK) {
			Toast.makeText(getApplicationContext(), getString(R.string.set_proxy_success, host, port), Toast.LENGTH_LONG).show();
		} else if (result != ProxyUtils.RESULT_NEED_MORE_OPERATE) {
			Toast.makeText(getApplicationContext(), R.string.set_proxy_fail, Toast.LENGTH_LONG).show();
		}
	}

}
