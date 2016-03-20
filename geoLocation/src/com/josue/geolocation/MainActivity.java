package com.josue.geolocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener, OnClickListener {

	private String latData, longData, altData, bearingData;
	
	//create objects public inside of the MainActivity class
	private TextView myTxtLat; //latitude
	private TextView myTxtLong; //longitude
	private TextView myTxtAlt; //altitude
	private TextView myTxtBearing; //bearing
	private ImageView myArrow; //compass arrow
	private Button recordBtn;
	
	private SensorManager mySensorManager; //sensor for the compass
	private float currentDegree = 0f; //the compass picture angle
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		recordBtn = (Button)findViewById(R.id.record_button);
		recordBtn.setOnClickListener(this);
		
		//connect the created objects to the elements in the activity_main.xml
		myTxtLat = (TextView)findViewById(R.id.textLat);
		myTxtLong = (TextView)findViewById(R.id.textLong);
		myTxtAlt = (TextView)findViewById(R.id.textAlt);
		myTxtBearing = (TextView)findViewById(R.id.textBearing);
		myArrow = (ImageView)findViewById(R.id.compass);
		
		//sensor manager for the compass
		mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		
		
		//location manager for the gps coordinates
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener myLocLis = new myLocationListener();
		
		//update the location
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocLis);
		
	}
	
	
	//CODE concerning the COMPASS
	//while the app is continually being used
	@Override
	protected void onResume(){
		super.onResume();
		
		//system's orientation sensor registered listeners
		mySensorManager.registerListener(this, mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);				
	}
	
	//when the app is not being used
	@Override
	protected void onPause(){
		super.onPause();
		//stop the listener and save battery
		mySensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		//get the angle around the z-axis rotated
		float degree = Math.round(event.values[0]);		
		String degreeSymbol =  "\u00b0";

		bearingData = Float.toString(degree);
		myTxtBearing.setText(bearingData + degreeSymbol);
		
		//rotation animation (reverse turn degree degrees)
		RotateAnimation rotAnim = new RotateAnimation(
				currentDegree,
				-degree,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF,
				0.5f);
		
		//how long the animation will take place
		rotAnim.setDuration(210);
		
		//set the animation after the end of the reservation status
		rotAnim.setFillAfter(true);
		
		//start the animation
		myArrow.startAnimation(rotAnim);
		currentDegree = -degree;
	}
	//END of code concerning COMPASS
	
	//this inner class is the location listener
	private class myLocationListener implements LocationListener{	
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			//this runs every time our location changes			
			
			if(location != null){
				double curLong = location.getLongitude();
				double curLat = location.getLatitude();
				double curAlt = location.getAltitude();				
				
				latData = Double.toString(curLat);
				longData = Double.toString(curLong);
				altData = Double.toString(curAlt);
				myTxtLat.setText(latData);
				myTxtLong.setText(longData);
				myTxtAlt.setText(altData);				
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	//=============================Functions for writing data=====================
	public void onClick(View v){
		switch(v.getId()){
		case(R.id.record_button):
			recordTheData();
		}
	}
	
	public void recordTheData(){		
		String prevContent = null;
		String curContent = null;
		
		//file directory
		File externalStorageDir = Environment.getExternalStorageDirectory();
		File myFile = new File(externalStorageDir, "geo_data.txt");
		
		//consolidate data into "curContent"
		//latData, longData, altData, bearingData are all global variables
		curContent = "Latitude: " + latData + "\n";
		curContent += "Longitude: " + longData + "\n";
		curContent += "Altitude: " + altData + "\n";
		curContent += "Bearing: " + bearingData + "\u00b0"  + "\n";
		
		//==================================Writing data in external storage below====================================================
		if(myFile.exists()){				
			try{
				//read previous written data in file
				StringBuilder mySB = new StringBuilder();
				FileInputStream myIS = new FileInputStream(myFile);
				BufferedReader myReader = new BufferedReader(new InputStreamReader(myIS, "UTF-8"));					
				String curLine = null;
				
				while((curLine = myReader.readLine()) != null ){
					mySB.append(curLine).append("\n");					
				}
				myIS.close();
				prevContent = mySB.toString();
				
				//write into the file
				FileOutputStream fileOut = new FileOutputStream(myFile);
				OutputStreamWriter myWriter = new OutputStreamWriter(fileOut);
				myWriter.append(prevContent);
				myWriter.append("\n" + curContent); //add in new scanned code
				myWriter.close();
			} catch(Exception e){
				
			}
		}else{ //file does not exist			
			try {
				myFile.createNewFile();
				FileOutputStream fileOut = new FileOutputStream(myFile);
				OutputStreamWriter myWriter = new OutputStreamWriter(fileOut);
				myWriter.append(curContent);
				myWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
}



