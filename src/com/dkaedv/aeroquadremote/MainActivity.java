package com.dkaedv.aeroquadremote;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dkaedv.aeroquadremote.AllFlightValuesMessage.AltitudeHoldStatus;
import com.dkaedv.aeroquadremote.AllFlightValuesMessage.Mode;

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
	private TextView textViewBattVoltage;
	
	private TextView textViewKinematicsX;
	private TextView textViewKinematicsY;
	private TextView textViewKinematicsZ;
	private TextView textViewRoll;
	private TextView textViewPitch;
	private TextView textViewYaw;
	private TextView textViewThrottle;
	
	protected Button buttonArmMotors;
	
	protected SeekBar seekBarThrottle;

	protected RemoteControlMessage remoteControlMsg = new RemoteControlMessage();
	private AllFlightValuesMessage flightValuesMsg;

	private Button connectionButton;
	private Button controlButton;
	
	private boolean battIsAlarm = false;
	private boolean isConnected = false;
	
	protected DeviceOrientation deviceOrientation;
	
	private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		
		// Only defines wake lock, does not acquire
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AeroQuad connection open");

		wireFieldsToUIElements();

		deviceOrientation = new DeviceOrientation(this);
		
		connectionButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				if (!isConnected) {
					// Connect
					resetState();
					new ConnectSocketTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

				} else {
					// Disconnect
					try {
						socket.close();
					} catch (IOException e) {
						Log.e(TAG, "Could not close socket: " + e.getMessage(), e);
					} catch (NullPointerException e) {
						// Do nothing
					}
					socket = null;
				}

			}

		});
		
		buttonArmMotors.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (flightValuesMsg != null && flightValuesMsg.motorsArmed == false) {
					new ArmMotorsTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				} else {
					new DisarmMotorsTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				}
			}
		});
		
		controlButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					deviceOrientation.startListening();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					deviceOrientation.stopListening();
					remoteControlMsg.resetAxes();
				}
				return false;
			}
		});
	}
		
	private void resetState() {
		battIsAlarm = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		try {
			socket.close();
			processConnectionClosed();
		} catch (IOException e) {
			Log.e(TAG, "Could not close socket: " + e.getMessage(), e);
		} catch (NullPointerException e) {
			// Do nothing
		}
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
		textViewBattVoltage = (TextView) findViewById(R.id.textViewBattVoltage);
		
		textViewKinematicsX = (TextView) findViewById(R.id.textViewKinematicsX);
		textViewKinematicsY = (TextView) findViewById(R.id.textViewKinematicsY);
		textViewKinematicsZ = (TextView) findViewById(R.id.textViewKinematicsZ);
		textViewRoll = (TextView) findViewById(R.id.textViewRoll);
		textViewPitch = (TextView) findViewById(R.id.textViewPitch);
		textViewYaw = (TextView) findViewById(R.id.textViewYaw);
		textViewThrottle = (TextView) findViewById(R.id.textViewThrottle);
		
		connectionButton = (Button) findViewById(R.id.connectionButton);
		controlButton = (Button) findViewById(R.id.buttonControl);
		buttonArmMotors = (Button) findViewById(R.id.buttonArmMotors);
		seekBarThrottle = (SeekBar) findViewById(R.id.seekBarThrottle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	void processTelemetryUpdate(TelemetryUpdaterTask telemetryUpdaterTask, AllFlightValuesMessage... values) {
		
		AllFlightValuesMessage msg = values[0];
		flightValuesMsg = msg;
		
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

		setMotorCommand(progressMotor1, msg.motors[0]);
		setMotorCommand(progressMotor2, msg.motors[1]);
		setMotorCommand(progressMotor3, msg.motors[2]);
		setMotorCommand(progressMotor4, msg.motors[3]);
		
		textViewKinematicsX.setText(String.valueOf(msg.kinematicsXAngle));
		textViewKinematicsY.setText(String.valueOf(msg.kinematicsYAngle));
		textViewKinematicsZ.setText(String.valueOf(msg.kinematicsZAngle));
		textViewRoll.setText(String.valueOf(msg.receiverRoll));
		textViewPitch.setText(String.valueOf(msg.receiverPitch));
		textViewYaw.setText(String.valueOf(msg.receiverYaw));
		textViewThrottle.setText(String.valueOf(msg.receiverThrottle));
		
		textViewBattVoltage.setText(msg.batteryVoltage + " V");
		if (msg.batteryVoltage < (3.33 * 4.0)) {
			if (!battIsAlarm) {	
				// Alarm
				final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
			    animation.setDuration(500); // duration - half a second
			    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
			    animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
			    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
			    textViewBattVoltage.startAnimation(animation);
			    textViewBattVoltage.setBackgroundColor(getResources().getColor(R.color.red));
			}
			
			battIsAlarm = true;
		} else {
			textViewBattVoltage.clearAnimation();
			battIsAlarm = false;
		}
		
	}

	private void setMotorCommand(ProgressBar motorProgress, int motorValue) {
		motorProgress.setProgress( (int) ((motorValue - 1000.0) / 1000.0 * motorProgress.getMax()) );
	}
	
	void processConnectionOpened() {
		textViewConnectionStatus.setBackgroundColor(getResources().getColor(R.color.green));
		isConnected = true;
		connectionButton.setText("Disconnect");
		wakeLock.acquire();
	}

	void processConnectionClosed() {
		textViewConnectionStatus.setBackgroundColor(getResources().getColor(R.color.red));
		isConnected = false;
		connectionButton.setText("Connect");
		wakeLock.release();
	}
}
