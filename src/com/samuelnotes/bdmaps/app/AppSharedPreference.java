package com.samuelnotes.bdmaps.app;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreference {

	private static SharedPreferences settings;

	private static SharedPreferences getSP(Context c) {
		return c.getSharedPreferences(AppContants.BDMapContants.SYS_CONF, 0);
	}


	
	
	public static double  getLastLocationLongitude(Context c) {
		settings = getSP(c);
		return  Double.valueOf(settings.getString(AppContants.BDMapContants.MLONGITUDE_STR, "116.327764"));
	}

	public static void setLastLocationLongitude(Context c, double longitude) {
		settings = getSP(c);
		settings.edit().putString(AppContants.BDMapContants.MLONGITUDE_STR, String.valueOf(longitude)).commit();
	}

	
	public static double  getLastLocationLatitude(Context c) {
		settings = getSP(c);
		return  Double.valueOf(settings.getString(AppContants.BDMapContants.MLATITUDE_STR, "39.904965"));
	}
	
	
	public static void setLastLocationLatitude(Context c, double latitude) {
		settings = getSP(c);
		settings.edit().putString(AppContants.BDMapContants.MLATITUDE_STR, String.valueOf(latitude)).commit();
	}

	
	
	/**
	 * 清空所有SharedPreference信息
	 * @param c
	 */
	public static void clearAllSharedPreference(Context c){
		settings = getSP(c);
		settings.edit().clear().commit();
	}
	
	
	
}
