package com.samuelnoes.bdmaps.model;

import java.io.Serializable;

public class NaviSDInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -367693890662798895L;

	private double sdLongitude;
	
	public NaviSDInfo(){}

	public NaviSDInfo(double sdLongitude, double sdLatitude, String sdAddress) {
		this.sdLongitude = sdLongitude;
		this.sdLatitude = sdLatitude;
		this.sdAddress = sdAddress;
	}

	private double sdLatitude;

	private String sdAddress;

	@Override
	public String toString() {
		return "NaviSDInfo [sdLongitude=" + sdLongitude + ", sdLatitude="
				+ sdLatitude + ", sdAddress=" + sdAddress + "]";
	}

	public double getSdLongitude() {
		return sdLongitude;
	}

	public void setSdLongitude(double sdLongitude) {
		this.sdLongitude = sdLongitude;
	}

	public double getSdLatitude() {
		return sdLatitude;
	}

	public void setSdLatitude(double sdLatitude) {
		this.sdLatitude = sdLatitude;
	}

	public String getSdAddress() {
		return sdAddress;
	}

	public void setSdAddress(String sdAddress) {
		this.sdAddress = sdAddress;
	}

}
