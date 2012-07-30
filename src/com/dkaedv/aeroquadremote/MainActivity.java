package com.dkaedv.aeroquadremote;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.whitebyte.wifihotspotutils.ClientScanResult;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private final static String AEROQUAD_HOSTNAME = "WiFly-EZX";
	private final static String TAG = MainActivity.class.getName();

	private Socket socket;
	
	private TextView telemetryTextView;
	
	private ProgressBar progressMotor1;
	private ProgressBar progressMotor2;
	private ProgressBar progressMotor3;
	private ProgressBar progressMotor4;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ToggleButton connectionButton = (ToggleButton) findViewById(R.id.connectionButton);
		Log.d(TAG, "Connection Button type: " + connectionButton.getClass().getName());

		telemetryTextView = (TextView) findViewById(R.id.telemetryTextView);
		progressMotor1 = (ProgressBar) findViewById(R.id.progressMotor1);
		progressMotor2 = (ProgressBar) findViewById(R.id.progressMotor2);
		progressMotor3 = (ProgressBar) findViewById(R.id.progressMotor3);
		progressMotor4 = (ProgressBar) findViewById(R.id.progressMotor4);

		connectionButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				ToggleButton v = (ToggleButton) view;

				if (v.isChecked()) {
					// Connect
					new AsyncTask<String, String, String>() {
						
						@Override
						protected String doInBackground(String... params) {
							InetAddress in;
							try {
								List<ClientScanResult> clients = getClientList(true, 500);
								for (ClientScanResult client : clients) {
									Log.d(TAG, "Found client " + client.getHWAddr() + " " + client.getDevice() + " " + client.getIpAddr());
								}
								
								
								in = InetAddress.getByName(clients.get(0).getIpAddr());
								if (in.isReachable(500)) {
									Log.d(TAG, "AeroQuad reachable");
									socket = new Socket(in, 2000);
									for (int i = 0; i < 7; i++) {
										// Read *HELLO*
										socket.getInputStream().read();
									}
									
									// Start AsyncTask to get telemetry data
									new TelemetryUpdaterTask().execute("");
								} else {
									Log.d(TAG, "AeroQuad not reachable");
								}
							} catch (UnknownHostException e) {
								Log.e(TAG, "Could not connect: " + e.getMessage(), e);
							} catch (IOException e) {
								Log.e(TAG, "Could not connect: " + e.getMessage(), e);
							}
							
							return null;
						}
					}.execute("");

				} else {
					// Disconnect
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							Log.e(TAG, "Could not close socket: " + e.getMessage(), e);
						}
						socket = null;
					}
				}

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/**
     * Gets a list of the clients connected to the Hotspot
     * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @param reachableTimeout Reachable Timout in miliseconds
     * @return ArrayList of {@link ClientScanResult}
     */
	public ArrayList<ClientScanResult> getClientList(boolean onlyReachables, int reachableTimeout) {
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
	
	
	class TelemetryUpdaterTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				BufferedReader streamReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line;
				
				while (socket.isConnected()) {
					socket.getOutputStream().write('6');
					line = streamReader.readLine();
					publishProgress(line);
				}
			} catch (IOException e) {
				Log.e(TAG, "Error while updating telemetry: " + e.getMessage(), e);
			}
			
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			telemetryTextView.setText(values[0]);
		}
		
	}
}
