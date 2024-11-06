package com.bumba27.demo_pheludar;

import com.bumba27.utils.ReusableClass;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigureApp extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.configure_app);
	}
	
	@Override
	public void onBackPressed() 
	{
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
		finish();
	}
	
	public void saveConfig(View v) 
	{
		if(((EditText)findViewById(R.id.editTextUrl)).getText().toString().length()>0 && ((EditText)findViewById(R.id.editTextIntervalTime)).getText().toString().length()>0)
		{
			ReusableClass.base_url = ((EditText)findViewById(R.id.editTextUrl)).getText().toString();
			saveInPreference("location_tracking_time_interval_min", ((EditText)findViewById(R.id.editTextIntervalTime)).getText().toString());
		
			Toast.makeText(this, "Thanks for saving.", Toast.LENGTH_LONG).show();
			onBackPressed();
		}
		else
		{
			Toast.makeText(this, "Please fill up all the details!!", Toast.LENGTH_LONG).show();
		}
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
