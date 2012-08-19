package com.dkaedv.aeroquadremote;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class CommandSenderTask extends AsyncTask<String, String, String> {
	private final MainActivity mainActivity;
	private final static String TAG = CommandSenderTask.class.getName();

	private final static boolean DEBUG = false;
	
	private Handler handler = new Handler();
	private BufferedWriter streamWriter;

	public CommandSenderTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	protected String doInBackground(String... params) {
		if (DEBUG)
			Log.d(TAG, "CommandSenderTask started");

		try {
			streamWriter = new BufferedWriter(new OutputStreamWriter(this.mainActivity.socket.getOutputStream()));
			sendCommands();
			
		} catch (IOException e) {
			Log.e(TAG, "Error while sending commands: " + e.getMessage(), e);
		}

		return null;
	}

	private void sendCommands() {
		try {
			if (this.mainActivity.isCommandTransmissionEnabled) {
				if (DEBUG) Log.d(TAG, "Sending commands");
				this.mainActivity.remoteControlMsg.throttle = this.mainActivity.seekBarThrottle.getProgress() + 1000;
		
				if (this.mainActivity.deviceOrientation.isListening()) {
					int[] commands = this.mainActivity.deviceOrientation.getRemoteControlCommands();
					this.mainActivity.remoteControlMsg.roll = commands[0];
					this.mainActivity.remoteControlMsg.pitch = commands[1];
					this.mainActivity.remoteControlMsg.yaw = commands[2];
				}
		
				streamWriter.write(this.mainActivity.remoteControlMsg.serialize());
				streamWriter.flush();
			}
			
			handler.postDelayed(new Runnable() {
				public void run() {
					sendCommands();
				}
			}, 100);
		} catch (IOException e) {
			Log.e(TAG, "Error while sending commands: " + e.getMessage(), e);
		} catch (NullPointerException e) {
			Log.e(TAG, "Error while sending commands: " + e.getMessage(), e);
		}
	}

}
