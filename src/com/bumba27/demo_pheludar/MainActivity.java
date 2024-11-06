package com.bumba27.demo_pheludar;

import java.util.Calendar;

import com.bumba27.utils.ReusableClass;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity{

	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	PendingIntent pendingIntent;
	AlarmManager alarmManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
	}


	//===================================================================================================
	//===================================================================================================
	// Job related buttons
	//===================================================================================================
	//===================================================================================================

	public void addingjob(View v) 
	{
		Intent i = new Intent(this, AddJobActivity.class);
		startActivity(i);
		finish();
	}

	public void editingjob(View v) 
	{
		Intent i = new Intent(this, JobListForEdit.class);
		startActivity(i);
		finish();
	}
	
	public void deleteingjob(View v) 
	{
		Intent i = new Intent(this, JobListForDelete.class);
		startActivity(i);
		finish();
	}
	
	public void incompleteJob(View v) 
	{
		Intent i = new Intent(this, IncompleteJob.class);
		startActivity(i);
		finish();
	}
	
	public void configure(View v) 
	{
		Intent i = new Intent(this, ConfigureApp.class);
		startActivity(i);
		finish();
	}
	
	//===================================================================================================
	//===================================================================================================
	// END Job related buttons
	//===================================================================================================
	//===================================================================================================

	public void StartingService(View v) 
	{
		try 
		{
			LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled || !isNetworkEnabled) 
			{
				showSettingsAlert();
			} 
			else 
			{
				if(ReusableClass.isConnectingToInternet(this))
				{
					saveInPreference("mb_code_perf", "100002");
					saveInPreference("location_tracking_time_interval_min", "1");  // in min

					//========================================================================================
					// ++++++++++++++++++++++++++++++ Start Tracker service ++++++++++++++++++++++++++++++++++
					//========================================================================================

					Intent myIntent1 = new Intent(MainActivity.this, MyMainReceiver.class);
					pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent1,0);

					alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
					long recurring = (1 * 60000);  // in milliseconds
					alarmManager.setRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), recurring, pendingIntent);

					//========================================================================================
					// ++++++++++++++++++++++++++++++ Start Tracker service ++++++++++++++++++++++++++++++++++
					//========================================================================================
				}
				else
				{
					Toast.makeText(this, "Sorry no Internet connection available!!", Toast.LENGTH_LONG).show();
				}
			}
		}
		catch(Throwable t)
		{
			Toast.makeText(this, "GPS checking time error. " + t, Toast.LENGTH_LONG).show();
		}
	}

	public void StopingService(View v) 
	{
		stopService(new Intent(this, Service_location.class));
		alarmManager.cancel(pendingIntent);
	}

	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				MainActivity.this.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Preference Variable
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	// --------------------------------------------
	// method to save variable in preference
	// --------------------------------------------
	public void saveInPreference(String name, String content) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, content);
		editor.commit();
	}

	// --------------------------------------------
	// getting content from preferences
	// --------------------------------------------
	public String getFromPreference(String variable_name) {
		String preference_return;
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		preference_return = preferences.getString(variable_name, "");

		return preference_return;
	}

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Preference Variable
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}


