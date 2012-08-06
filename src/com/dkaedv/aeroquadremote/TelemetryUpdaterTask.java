package com.dkaedv.aeroquadremote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import android.os.AsyncTask;
import android.util.Log;

final class TelemetryUpdaterTask extends AsyncTask<String, AllFlightValuesMessage, String> {
	private final MainActivity mainActivity;
	private final static String TAG = TelemetryUpdaterTask.class.getName();
	
	TelemetryUpdaterTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	private AllFlightValuesMessage flightValuesMsg = new AllFlightValuesMessage();
	private RemoteControlMessage remoteControlMsg = new RemoteControlMessage();

	@Override
	protected String doInBackground(String... params) {
		Log.d(TAG, "TelemetryUpdaterTask started");
		
		try {
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(this.mainActivity.socket.getInputStream()));
			BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(this.mainActivity.socket.getOutputStream()));
			String line;
			
			try {
				// Required to give Arduino enough time to open the connection
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// Do nothing, just continue
			}

			Log.d(TAG, "Requesting data");
			streamWriter.write('s');
			streamWriter.flush();
			
			printSocketState(this.mainActivity.socket);

			while (this.mainActivity.socket != null && this.mainActivity.socket.isConnected()) {
				Log.d(TAG, "Sending commands");
				streamWriter.write(remoteControlMsg.serialize());
				streamWriter.write('s');
				streamWriter.flush();
				
				Log.d(TAG, "Reading line");
				line = streamReader.readLine();
				Log.d(TAG, "Received line: " + line);
				
				
				flightValuesMsg.parse(line);
				Log.d(TAG, "Parsed Message: " + flightValuesMsg);
				
				publishProgress(flightValuesMsg);
			}
		} catch (IOException e) {
			Log.e(TAG, "Error while updating telemetry: " + e.getMessage(), e);
		}
		
		
		return null;
	}
	
	private void printSocketState(Socket socket) {
		try {
			Log.d(TAG, "Socket state: keepAlive: " + socket.getKeepAlive() + ", soLinger: " + socket.getSoLinger() + ", SendBuffer: " + socket.getSendBufferSize() + ", ReceiveBuffer: " + socket.getReceiveBufferSize() + ", tcpNoDelay: " + socket.getTcpNoDelay());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onProgressUpdate(AllFlightValuesMessage... values) {
		mainActivity.processTelemetryUpdate(this, values);
	}
	
	@Override
	protected void onPostExecute(String result) {
		mainActivity.processConnectionClosed();
	}
	
	@Override
	protected void onPreExecute() {
		mainActivity.processConnectionOpened();
	}
	
}