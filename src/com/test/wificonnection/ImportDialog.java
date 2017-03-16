package com.test.wificonnection;

import android.app.Activity;
import android.app.AlertDialog;

public class ImportDialog {
	final CharSequence[] items = { "Take Photo From Gallery",
			"Take Photo From Camera" };
	Activity activity;
	AlertDialog dialog;
	AlertDialog.Builder builder;
	String mWifiDetails;

	public ImportDialog(Activity a, String mWifiDetails) {
		this.activity = a;
		this.mWifiDetails = mWifiDetails;
		builder = new AlertDialog.Builder(a);
	}

	public void showDialog() {

		builder.setTitle("wifi Provider Details");
		builder.setMessage(mWifiDetails);

		AlertDialog alert = builder.create();
		alert.show();
	}
}
