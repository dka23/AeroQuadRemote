package com.dkaedv.aeroquadremote;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.whitebyte.wifihotspotutils.ClientScanResult;

final class ConnectSocketTask extends AsyncTask<String, String, Boolean> {
	private final MainActivity mainActivity;
	private final static String TAG = ConnectSocketTask.class.getName();

	ConnectSocketTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		InetAddress in;
		try {
			List<ClientScanResult> clients = getClientList(true, 500);
			for (ClientScanResult client : clients) {
				Log.d(TAG, "Found client " + client.getHWAddr() + " " + client.getDevice() + " " + client.getIpAddr());
			}
			
			
			in = InetAddress.getByName(clients.get(0).getIpAddr());
			if (in.isReachable(500)) {
				Log.d(TAG, "AeroQuad reachable");
				this.mainActivity.socket = new Socket(in, 2000);
				this.mainActivity.socket.setTcpNoDelay(true);

				// Read *HELLO*
				byte[] buffer = new byte[7];
				this.mainActivity.socket.getInputStream().read(buffer);
				Log.d(TAG, "Received: " + new String(buffer));
				
				return true;
			} else {
				Log.d(TAG, "AeroQuad not reachable");
			}
		} catch (UnknownHostException e) {
			Log.e(TAG, "Could not connect: " + e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, "Could not connect: " + e.getMessage(), e);
		}
		
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if (result.booleanValue()) {
			// Start AsyncTask to get telemetry data
			new TelemetryUpdaterTask(this.mainActivity).execute("");			
		}
	}
	
	/**
     * Gets a list of the clients connected to the Hotspot
     * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @param reachableTimeout Reachable Timout in miliseconds
     * @return ArrayList of {@link ClientScanResult}
     */
	private ArrayList<ClientScanResult> getClientList(boolean onlyReachables, int reachableTimeout) {
		BufferedReader br = null;
		ArrayList<ClientScanResult> result = null;
 
		try {
			result = new ArrayList<ClientScanResult>();
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
 
				if ((splitted != null) && (splitted.length >= 4)) {
					// Basic sanity check
					String mac = splitted[3];
 
					if (mac.matches("..:..:..:..:..:..")) {
						boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);
 
						if (!onlyReachables || isReachable) {
							result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(this.getClass().toString(), e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				Log.e(this.getClass().toString(), e.getMessage());
			}
		}
 
		return result;
	}
}