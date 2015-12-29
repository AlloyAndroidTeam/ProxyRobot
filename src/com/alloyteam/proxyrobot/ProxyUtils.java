package com.alloyteam.proxyrobot;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;


public class ProxyUtils {
    public static final int RESULT_OK = 0;
    public static final int RESULT_NOT_CONNECTED = 1;
    public static final int RESULT_ERROR = 2;
	public static final int RESULT_NEED_PASSWORD = 3;
	public static final int RESULT_ALREADY_EXIST = 4;
	public static final int RESULT_NEED_MORE_OPERATE = 5;

	/**
	 * 从wifi配置列表中取当前连接的配置
	 * @param wm
	 * @return 当前配置，或者null
	 */
	private static WifiConfiguration GetCurrentWifiConfiguration(WifiManager wm) {
		if (!wm.isWifiEnabled()) {
			return null;
		}
		int networkId = wm.getConnectionInfo().getNetworkId();
		if (networkId == -1) {
			return null;
		}
		List<WifiConfiguration> configList = wm.getConfiguredNetworks();
		if (configList == null) {
			return null;
		}
		for (int i = 0, len = configList.size(); i < len; ++i) {
			WifiConfiguration config = configList.get(i);
			if (config.networkId == networkId) {
				return config;
			}
		}
		return null;
	}
	
	/**
	 * 修改当前wifi连接的代理
	 * @param enabled
	 * @param host
	 * @param strPort
	 * @return
	 */
	static int setWifiProxy(boolean enabled, String host, String strPort) {
		// get the current wifi configuration
		WifiManager wm = (WifiManager) MyApp.getApp().getSystemService(Context.WIFI_SERVICE);
		WifiConfiguration config = GetCurrentWifiConfiguration(wm);
		if (config == null) {
			return RESULT_NOT_CONNECTED;
		}
		try {
            if (Build.VERSION.SDK_INT < 21) {
                // get the link properties from the wifi configuration
                Object linkProp = WifiConfiguration.class.getField("linkProperties").get(config);
                if (linkProp == null) {
                    return RESULT_ERROR;
                }
                // get the setHttpProxy method for LinkProperties
                Class<?> ProxyProperties = Class.forName("android.net.ProxyProperties");
                Method setHttpProxy = linkProp.getClass().getDeclaredMethod("setHttpProxy", ProxyProperties);
                setHttpProxy.setAccessible(true);
                String assign;
                Object settings = null;
                if (enabled) {
                    int port = Integer.parseInt(strPort, 10);
                    if (port <= 0) {
                        throw new IllegalArgumentException("port error");
                    }
                    assign = "STATIC";
                    // new ProxyProperties
                    settings = ProxyProperties.getDeclaredConstructor(String.class, int.class, String.class).newInstance(host, port, null);
                } else {
                    assign = "NONE";
                }
                setHttpProxy.invoke(linkProp, settings);
                Field proxySettings = WifiConfiguration.class.getField("proxySettings");
                proxySettings.set(config, Enum.valueOf((Class<Enum>) proxySettings.getType(), assign));
            } else {
                String assign;
                ProxyInfo settings = null;
                if (enabled) {
                    int port = Integer.parseInt(strPort, 10);
                    if (port <= 0) {
                        throw new IllegalArgumentException("port error");
                    }
                    assign = "STATIC";
                    // new ProxyProperties
                    settings = ProxyInfo.buildDirectProxy(host, port);
                } else {
                    assign = "NONE";
                }
                Class proxySettings = Class.forName("android.net.IpConfiguration$ProxySettings");
                Method setProxy = WifiConfiguration.class.getDeclaredMethod("setProxy", proxySettings, ProxyInfo.class);
                setProxy.invoke(config, Enum.valueOf((Class<Enum>) proxySettings, assign), settings);
            }
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | NoSuchFieldException | InstantiationException | InvocationTargetException | ClassNotFoundException | SecurityException e) {
			e.printStackTrace();
			return RESULT_ERROR;
		}
		int networkId = wm.updateNetwork(config);
		if (networkId != -1) {
			wm.disconnect();
			wm.enableNetwork(networkId, true);
			return RESULT_OK;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			MyApp.getApp().startActivity(
					new Intent(MyApp.getApp(), RetryActivity.class)
							.putExtra(RetryActivity.KEY_CONFIG, config)
							.putExtra(RetryActivity.KEY_HOST, host)
							.putExtra(RetryActivity.KEY_PORT, strPort)
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			);
			return RESULT_NEED_MORE_OPERATE;
		}
		return RESULT_ERROR;
	}

	static boolean isNetworkExist(WifiConfiguration config) {
		WifiManager wm = (WifiManager) MyApp.getApp().getSystemService(Context.WIFI_SERVICE);
		Iterable<WifiConfiguration> existingConfigs = wm.getConfiguredNetworks();
		if (existingConfigs != null) {
			String ssid = config.SSID;
			for (WifiConfiguration existingConfig : existingConfigs) {
				if (existingConfig.SSID.equals(ssid)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 添加并连接到新的wifi热点
	 * @param config 新热点（带proxy信息）
	 * @param password 当config为wep或wpa类型时需要
     * @return
     */
	static int connectToNewNetwork(WifiConfiguration config, String password) {
		WifiManager wm = (WifiManager) MyApp.getApp().getSystemService(Context.WIFI_SERVICE);
		if (config.wepKeys[0] != null) { // wep
			if (TextUtils.isEmpty(password)) {
				return RESULT_NEED_PASSWORD;
			}
			config.wepKeys[0] = '\"' + password + '\"';
		} else if (config.preSharedKey != null) { // wpa
			if (TextUtils.isEmpty(password)) {
				return RESULT_NEED_PASSWORD;
			}
			config.preSharedKey = '\"' + password + '\"';
		}
		int networkId = wm.addNetwork(config);
		if (networkId != -1) {
			if (wm.enableNetwork(networkId, true)) {
				return RESULT_OK;
			}
		}
		return RESULT_ERROR;
	}
}
