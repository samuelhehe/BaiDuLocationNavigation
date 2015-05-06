package com.samuelnotes.bdmaps.app;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);

	}

	
	
	
	
	
}
