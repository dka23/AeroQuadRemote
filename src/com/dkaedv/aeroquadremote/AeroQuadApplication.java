package com.dkaedv.aeroquadremote;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "dGlHYXdfbEN3aW9GY19ValotNGVEMlE6MQ")
public class AeroQuadApplication extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}
