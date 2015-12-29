package com.alloyteam.proxyrobot;
import android.app.Application;
import android.content.Context;


public class MyApp extends Application {
	private static Context instance;
	public static Context getApp() {
		return instance;
	}

	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
	}

}
