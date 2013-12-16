package alloyteam.pel.proxyrobot;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;


public class ProxyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String host = intent.getStringExtra("host");
		String port = intent.getStringExtra("port");
		if (TextUtils.isEmpty(host) || TextUtils.isEmpty(port)) {
			Toast.makeText(context, R.string.set_proxy_error_param, Toast.LENGTH_LONG).show();
			return;
		}
		if (ProxyUtils.setWifiProxy(true, host, port) == R.result.RESULT_OK) {
			Toast.makeText(context, context.getString(R.string.set_proxy_success, host, port), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, R.string.set_proxy_fail, Toast.LENGTH_LONG).show();
		}
	}

}
