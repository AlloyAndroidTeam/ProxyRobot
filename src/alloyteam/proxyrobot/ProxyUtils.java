package alloyteam.proxyrobot;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;


public class ProxyUtils {
    public static final int RESULT_OK = 0;
    public static final int RESULT_NOT_CONNECTED = 1;
    public static final int RESULT_ERROR = 2;

	private static boolean isParameterTypesMatch(Class<?>[] params1, Class<?>[] params2) {
		if (params1.length != params2.length) {
			return false;
		}
		for (int i = 0, len = params1.length; i < len; ++i) {
			if (params1[i] != params2[i]) {
				return false;
			}
		}
		return true;
	}
	private static Method getDeclaredMethod (Class<?> cz, String name, Class ...parameterTypes) throws NoSuchMethodException {
		if (Build.VERSION.SDK_INT >= 9) {
			return cz.getDeclaredMethod(name, parameterTypes);
		}
		Method[] methods = cz.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equals(name) && isParameterTypesMatch(parameterTypes, method.getParameterTypes())) {
				return method;
			}
		}
		throw new NoSuchMethodException();
	}
	private static Constructor<?> getDeclaredConstructor(Class<?> cz, Class ...parameterTypes) throws NoSuchMethodException {
		if (Build.VERSION.SDK_INT >= 9) {
			return cz.getConstructor(parameterTypes);
		}
		Constructor<?>[] cs = cz.getDeclaredConstructors();
		for (Constructor<?> c : cs) {
			if (isParameterTypesMatch(parameterTypes, c.getParameterTypes())) {
				return c;
			}
		}
		throw new NoSuchMethodException();
	}
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
                Method setHttpProxy = getDeclaredMethod(linkProp.getClass(), "setHttpProxy", ProxyProperties);
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
                    settings = getDeclaredConstructor(ProxyProperties,
                            String.class, int.class, String.class).newInstance(host, port, null);
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
			if (wm.updateNetwork(config) != -1) {
				//wm.saveConfiguration();
				wm.disconnect();
				wm.reconnect();
				return RESULT_OK;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return RESULT_ERROR;
	}
}
