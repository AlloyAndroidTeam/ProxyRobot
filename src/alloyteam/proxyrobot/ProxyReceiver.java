package alloyteam.proxyrobot;
import alloyteam.pel.proxyrobot.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;


public class ProxyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (isOrderedBroadcast()) {
			abortBroadcast();
		}
        boolean enabled = intent.getBooleanExtra("enabled", true);
		String host = intent.getStringExtra("host");
		String port = intent.getStringExtra("port");
		if (enabled && (TextUtils.isEmpty(host) || TextUtils.isEmpty(port))) {
			Toast.makeText(context, R.string.set_proxy_error_param, Toast.LENGTH_LONG).show();
			return;
		}
		if (ProxyUtils.setWifiProxy(enabled, host, port) == ProxyUtils.RESULT_OK) {
			Toast.makeText(context, context.getString(R.string.set_proxy_success, host, port), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, R.string.set_proxy_fail, Toast.LENGTH_LONG).show();
		}
	}

}
