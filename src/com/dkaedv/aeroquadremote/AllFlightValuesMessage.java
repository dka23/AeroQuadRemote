package com.dkaedv.aeroquadremote;

public class AllFlightValuesMessage {
	public boolean motorsArmed;
	public float kinematicsXAngle;
	public float kinematicsYAngle;
	public float kinematicsZAngle;
	public float altitude;
	public AltitudeHoldStatus altitudeHoldStatus;
	public int receiverRoll;
	public int receiverPitch;
	public int receiverYaw;
	public int receiverThrottle;
	public int receiverCommandMode;
	public int receiverAux1;
	public int receiverAux2;
	public int receiverAux3;
	public int[] motors = new int[8];
	public float batteryVoltage;
	public Mode mode;

	enum AltitudeHoldStatus {
		OFF("0"), ON("1"), ALTPANIC("2");

		private String text;

		private AltitudeHoldStatus(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}

		public static AltitudeHoldStatus fromString(String text) {
			if (text != null) {
				for (AltitudeHoldStatus b : AltitudeHoldStatus.values()) {
					if (text.equalsIgnoreCase(b.text)) {
						return b;
					}
				}
			}
			return null;
		}
	}

	enum Mode {
		RATE("0"), ATTITUDE("1");
		

		private String text;

		private Mode(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}

		public static Mode fromString(String text) {
			if (text != null) {
				for (Mode b : Mode.values()) {
					if (text.equalsIgnoreCase(b.text)) {
						return b;
					}
				}
			}
			return null;
		}
	}

	public void parse(String line) {
		String[] fields = line.split(",");
		motorsArmed = (fields[0].equals("1"));
		kinematicsXAngle = Float.parseFloat(fields[1]);
		kinematicsYAngle = Float.parseFloat(fields[2]);
		kinematicsZAngle = Float.parseFloat(fields[3]);
		altitude = Float.parseFloat(fields[4]);
		altitudeHoldStatus = AltitudeHoldStatus.fromString(fields[5]);
		receiverRoll = Integer.parseInt(fields[6]);
		receiverPitch = Integer.parseInt(fields[7]);
		receiverYaw = Integer.parseInt(fields[8]);
		receiverThrottle = Integer.parseInt(fields[9]);
		receiverCommandMode = Integer.parseInt(fields[10]);
		receiverAux1 = Integer.parseInt(fields[11]);
		receiverAux2 = Integer.parseInt(fields[12]);
		receiverAux3 = Integer.parseInt(fields[13]);

		for (int i = 0; i < 8; i++) {
			motors[i] = Integer.parseInt(fields[14+i]);
		}
		
		batteryVoltage = Float.parseFloat(fields[22]);
		mode = Mode.fromString(fields[23]);
		
	}

	@Override
	public String toString() {
		return "AllFlightValuesMessage [motorsArmed=" + motorsArmed + ", kinematicsXAngle=" + kinematicsXAngle + ", kinematicsYAngle=" + kinematicsYAngle + ", kinematicsZAngle=" + kinematicsZAngle + ", altitude=" + altitude + ", altitudeHoldStatus=" + altitudeHoldStatus + ", receiverRoll="
				+ receiverRoll + ", receiverPitch=" + receiverPitch + ", receiverYaw=" + receiverYaw + ", receiverThrottle=" + receiverThrottle + ", receiverCommandMode=" + receiverCommandMode + ", receiverAux1=" + receiverAux1 + ", receiverAux2=" + receiverAux2 + ", receiverAux3=" + receiverAux3
				+ ", motor1=" + motors[0] + ", motor2=" + motors[1] + ", motor3=" + motors[2] + ", motor4=" + motors[3] + ", motor5=" + motors[4] + ", motor6=" + motors[5] + ", motor7=" + motors[6] + ", motor8=" + motors[7] + ", batteryVoltage=" + batteryVoltage + ", mode=" + mode + "]";
	}

}
