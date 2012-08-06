package com.dkaedv.aeroquadremote;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import com.dkaedv.aeroquadremote.AllFlightValuesMessage.AltitudeHoldStatus;
import com.dkaedv.aeroquadremote.AllFlightValuesMessage.Mode;
import com.whitebyte.wifihotspotutils.ClientScanResult;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private final static String TAG = MainActivity.class.getName();

	Socket socket;
	
	TextView telemetryTextView;
	
	private ProgressBar progressMotor1;
	private ProgressBar progressMotor2;
	private ProgressBar progressMotor3;
	private ProgressBar progressMotor4;
	
	private TextView textViewConnectionStatus;
	private TextView textViewMotorsArmed;
	private TextView textViewAltitudeHold;
	private TextView textViewAttitudeMode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ToggleButton connectionButton = (ToggleButton) findViewById(R.id.connectionButton);
		Log.d(TAG, "Connection Button type: " + connectionButton.getClass().getName());

		wireFieldsToUIElements();
		
		connectionButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				ToggleButton v = (ToggleButton) view;

				if (v.isChecked()) {
					// Connect
					new ConnectSocketTask(MainActivity.this).execute("");

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

	private void wireFieldsToUIElements() {
		telemetryTextView = (TextView) findViewById(R.id.telemetryTextView);
		progressMotor1 = (ProgressBar) findViewById(R.id.progressMotor1);
		progressMotor2 = (ProgressBar) findViewById(R.id.progressMotor2);
		progressMotor3 = (ProgressBar) findViewById(R.id.progressMotor3);
		progressMotor4 = (ProgressBar) findViewById(R.id.progressMotor4);
		textViewConnectionStatus = (TextView) findViewById(R.id.textViewConnectionStatus);
		textViewMotorsArmed = (TextView) findViewById(R.id.textViewMotorsArmedStatus);
		textViewAltitudeHold = (TextView) findViewById(R.id.textViewAltitudeHold);
		textViewAttitudeMode = (TextView) findViewById(R.id.textViewAttitudeMode);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	void processTelemetryUpdate(TelemetryUpdaterTask telemetryUpdaterTask, AllFlightValuesMessage... values) {
		AllFlightValuesMessage msg = values[0];
		
		telemetryTextView.setText(msg.toString());
		
		if (msg.motorsArmed) {
			textViewMotorsArmed.setBackgroundColor(getResources().getColor(R.color.green));
		} else {
			textViewMotorsArmed.setBackgroundColor(getResources().getColor(R.color.red));
		}
		
		if (msg.mode.equals(Mode.ATTITUDE)) {
			textViewAttitudeMode.setBackgroundColor(getResources().getColor(R.color.green));
		} else {
			textViewAttitudeMode.setBackgroundColor(getResources().getColor(R.color.red));
		}
		
		if (msg.altitudeHoldStatus.equals(AltitudeHoldStatus.ON)) {
			textViewAltitudeHold.setBackgroundColor(getResources().getColor(R.color.green));
		} else {
			textViewAltitudeHold.setBackgroundColor(getResources().getColor(R.color.red));
		}
		
	}
	
	void processConnectionOpened() {
		textViewConnectionStatus.setBackgroundColor(getResources().getColor(R.color.green));
	}

	void processConnectionClosed() {
		textViewConnectionStatus.setBackgroundColor(getResources().getColor(R.color.red));
	}
}
