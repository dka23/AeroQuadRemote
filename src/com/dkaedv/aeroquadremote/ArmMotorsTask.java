package com.dkaedv.aeroquadremote;

import android.os.AsyncTask;
import android.util.Log;

public class ArmMotorsTask extends AsyncTask<String,String,String> {
	private final MainActivity mainActivity;
	private final static String TAG = ArmMotorsTask.class.getName();

	public ArmMotorsTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "Arming motors");
		mainActivity.remoteControlMsg.yaw = 2000;
		mainActivity.seekBarThrottle.setProgress(0);
	}
	
	@Override
	protected String doInBackground(String... params) {
		Log.d(TAG, "Sleeping");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		Log.d(TAG, "Sleep done");
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		mainActivity.remoteControlMsg.yaw = 1500;
		mainActivity.buttonArmMotors.setText("Disarm Motors");
		Log.d(TAG, "Motors armed");
	}

}
