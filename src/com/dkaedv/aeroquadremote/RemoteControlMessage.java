package com.dkaedv.aeroquadremote;

public class RemoteControlMessage {
	public int roll = 1500;
	public int pitch = 1500;
	public int yaw = 1500;
	public int throttle = 1000;
	public int mode = 2000;
	public int aux1 = 2000;
	
	public String serialize() {
		StringBuffer out = new StringBuffer();
		out.append("T");
		out.append(roll);
		out.append(";");
		out.append(pitch);
		out.append(";");
		out.append(yaw);
		out.append(";");
		out.append(throttle);
		out.append(";");
		out.append(mode);
		out.append(";");
		out.append(aux1);
		out.append(";");
		
		return out.toString();
	}
	
	public void resetAxes() {
		roll = 1500;
		pitch = 1500;
		yaw = 1500;
	}
}
