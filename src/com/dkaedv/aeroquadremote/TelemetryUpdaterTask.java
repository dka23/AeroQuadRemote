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
	
	private final static boolean DEBUG = false;
	
	TelemetryUpdaterTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	private AllFlightValuesMessage flightValuesMsg = new AllFlightValuesMessage();
	
	@Override
	protected String doInBackground(String... params) {
		if (DEBUG) Log.d(TAG, "TelemetryUpdaterTask started");
		
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

			if (DEBUG) Log.d(TAG, "Requesting data");
			streamWriter.write('s');
			streamWriter.flush();
			
			printSocketState(this.mainActivity.socket);

			while (this.mainActivity.socket != null && this.mainActivity.socket.isConnected()) {
				if (DEBUG) Log.d(TAG, "Reading line");
				line = streamReader.readLine();
				if (DEBUG) Log.d(TAG, "Received line: " + line);
				
				if (countOccurrences(line, ',') == 24) {
					flightValuesMsg.parse(line);
					if (DEBUG) Log.d(TAG, "Parsed Message: " + flightValuesMsg);
					publishProgress(flightValuesMsg);
				} else {
					Log.w(TAG, "Invalid line received: " + line);
				}
				
				
			}
		} catch (IOException e) {
			Log.e(TAG, "Error while updating telemetry: " + e.getMessage(), e);
		}
		
		
		return null;
	}
	
	private static int countOccurrences(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	    {
	        if (haystack.charAt(i) == needle)
	        {
	             count++;
	        }
	    }
	    return count;
	}
	
	private void printSocketState(Socket socket) {
		try {
			if (DEBUG) Log.d(TAG, "Socket state: keepAlive: " + socket.getKeepAlive() + ", soLinger: " + socket.getSoLinger() + ", SendBuffer: " + socket.getSendBufferSize() + ", ReceiveBuffer: " + socket.getReceiveBufferSize() + ", tcpNoDelay: " + socket.getTcpNoDelay());
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