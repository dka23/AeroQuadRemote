package com.dkaedv.aeroquadremote;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class DeviceOrientation {
	private final static String TAG = DeviceOrientation.class.getName();
	
	private float accel[] = new float[3];
	private float magnet[] = new float[3];
	private float rMatrix[] = new float[9];
	private float orientation[] = new float[3];
	private float orientationZero[] = new float[3];
	private boolean zerosSet = false;
	private boolean magnetRead = false;
	private boolean accelRead = false;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private Handler handler = new Handler(); 
	private final Context context;
	
	private boolean isListening = false;
	
	private SensorEventListener accelListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			for (int i = 0; i < 3; i++) accel[i] = event.values[i];
			accelRead = true;
			
			if (!zerosSet && magnetRead) {
				calibrate();
			}
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
	private SensorEventListener magnetListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			for (int i = 0; i < 3; i++) magnet[i] = event.values[i];
			magnetRead = true;

			if (!zerosSet && accelRead) {
				calibrate();
			}
		}
		
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	public DeviceOrientation(Context context) {
		this.context = context;
	}

	public void startListening() {
		if (sensorManager == null) initialize();
		
		sensorManager.registerListener(accelListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(magnetListener, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
		
		/*
		handler.postDelayed(new Runnable() {
			public void run() {
				updateOrientation();
				handler.postDelayed(this, 300);
			}
		}, 300);
		*/

		resetCalibration();
		isListening = true;
	}
	
	private void initialize() {
		sensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
	}
	
	private void resetCalibration() {
		zerosSet = false;
		magnetRead = false;
		accelRead = false;
	}
	
	private void calibrate() {
		updateOrientation();
		for (int i = 0; i < 3; i++) orientationZero[i] = orientation[i];
		zerosSet = true;
		Log.d(TAG, "Zeros set");
	}

	public void stopListening() {
		sensorManager.unregisterListener(accelListener);
		sensorManager.unregisterListener(magnetListener);
		isListening = false;
	}
	
	private void updateOrientation() {		
		sensorManager.getRotationMatrix(rMatrix, null, accel, magnet);
		sensorManager.getOrientation(rMatrix, orientation);

		// Roll, Pitch, Azimuth in Android notation (different for AeroQuad)
		Log.d(TAG, "Azimuth Z=" + String.format("%.2f", Math.toDegrees(normalRelativeAngle((orientation[0]-orientationZero[0])))) 
				+ ", Pitch X=" + String.format("%.2f", Math.toDegrees(normalRelativeAngle((orientation[1]-orientationZero[1])))) 
				+ ", Roll Y=" + String.format("%.2f", Math.toDegrees(normalRelativeAngle((orientation[2]-orientationZero[2])))));
	}
	
	/*
	 * Calculates remote control commands based on the sensor data.
	 * 
	 * Roll, Pitch, Yaw
	 */
	public int[] getRemoteControlCommands() {
		// if we are not listening return default values
		if (!isListening || !zerosSet) {
			return new int[] {1500,1500,1500};
		}
		
		// Calculate orientation
		updateOrientation();
		
		// All names in AeroQuad notation
		double rollAngleDeg = Math.toDegrees(normalRelativeAngle(orientation[0]-orientationZero[0]));
		double pitchAngleDeg = Math.toDegrees(normalRelativeAngle(orientation[2]-orientationZero[2]));
		double yawAngleDeg = Math.toDegrees(normalRelativeAngle(orientation[1]-orientationZero[1]));
		
		double[] anglesDeg = {rollAngleDeg, pitchAngleDeg, yawAngleDeg};
		
		// Clamp
		double[] limitsDeg = { 70, 70, 90 };
		for (int i = 0; i < 3; i++) {
			if (anglesDeg[i] < -limitsDeg[i]) {
				anglesDeg[i] = -limitsDeg[i];
			}
			if (anglesDeg[i] > limitsDeg[i]) {
				anglesDeg[i] = limitsDeg[i];
			}
		}
		
		// Calculate command values
		int[] commandValues = {0, 0, 0};
		int[] inversion = {-1, 1, 1};
		
		for (int i = 0; i < 3; i++) {
			commandValues[i] = (int) ((anglesDeg[i] / limitsDeg[i] * 500 * inversion[i]) + 1500);
		}
		
		Log.d(TAG, "Commands: Roll=" + commandValues[0] + ", Pitch=" + commandValues[1] + ", Yaw=" + commandValues[2]);
		return commandValues;
	}
	
	private final static double TWO_PI = 2 * Math.PI;
	private double normalRelativeAngle(double angle) {
	    return (angle %= TWO_PI) >= 0 ? (angle < Math.PI) ? angle : angle - TWO_PI : (angle >= -Math.PI) ? angle : angle + TWO_PI;
	  }

	public boolean isListening() {
		return isListening;
	}

}
