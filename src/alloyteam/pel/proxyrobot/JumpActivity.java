package alloyteam.pel.proxyrobot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public class JumpActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        String url = getIntent().getDataString();
        if (TextUtils.isEmpty(url)) {
        	Toast.makeText(this, R.string.set_proxy_error_param, Toast.LENGTH_LONG).show();
        	return;
        }
        Pattern pattern = Pattern.compile("\\?host=([^&\\s]+)&port=(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
        	Toast.makeText(this, R.string.set_proxy_error_param, Toast.LENGTH_LONG).show();
        	return;
        }
    	String host = matcher.group(1);
    	String port = matcher.group(2);
		if (ProxyUtils.setWifiProxy(true, host, port) == ProxyUtils.RESULT_OK) {
			Toast.makeText(this, getString(R.string.set_proxy_success, host, port), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.set_proxy_fail, Toast.LENGTH_LONG).show();
		}
    }
}