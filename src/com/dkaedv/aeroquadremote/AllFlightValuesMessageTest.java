package com.dkaedv.aeroquadremote;

import junit.framework.TestCase;

public class AllFlightValuesMessageTest extends TestCase {

	public void testParse() {
		String line = "0,0.00,0.00,-2.26,1.69,1,0,0,0,0,0,0,0,0,1000,1000,1000,1000,0,0,0,0,0,0,";
		AllFlightValuesMessage message = new AllFlightValuesMessage();
		
		message.parse(line);
		
		

		
	}
}
