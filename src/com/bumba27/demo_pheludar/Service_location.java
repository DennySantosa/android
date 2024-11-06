package com.bumba27.demo_pheludar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.bumba27.utils.ReusableClass;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class Service_location extends Service {

	double nlat = 0;
	double nlng = 0;
	double glat = 0;
	double glng = 0;
	String gAddress = "No address found";
	String nAddress = "No address found";

	LocationManager glocManager;
	LocationListener glocListener;
	LocationManager nlocManager;
	LocationListener nlocListener;

	int localDbRowCount = 0;
	int minutes = 0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) 
	{
		try 
		{
			Log.w("Service_location","Inside Location Service");

			nlocManager   = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			nlocListener = new MyLocationListenerNetWork();
			nlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0,          
					0,            
					nlocListener);

			glocManager  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			glocListener = new MyLocationListenerGPS();
			glocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					0,          
					0,            
					glocListener);

			Handler handler = new Handler();
			handler.postDelayed(new Runnable(){
				@Override
				public void run()
				{
					try {
						if((nlat!=0 && nlng!=0) || (glat!=0 && glng!=0))
						{
							// setting current date
							Calendar c = Calendar.getInstance();
							SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
							String formattedDate = df.format(c.getTime());

							if(!getFromPreference("LastInsertedDateTime").equalsIgnoreCase(""))
							{
								SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

								Date d1 = (Date) format.parse(getFromPreference("LastInsertedDateTime"));
								Date d2 = (Date) format.parse(formattedDate);

								long diff = d2.getTime() - d1.getTime();

								minutes = (int) ((diff / (1000*60)) % 60);
								Log.e("Location minutes Diff","Location last updated in Local DB: " + minutes);
							}
//							Log.e("Location","Sending location  to server");
//   					    new MyAsyncTask().execute(""+glat, ""+glng, ""+nlat, ""+nlng, getFromPreference("mb_code_perf"), formattedDate, gAddress, nAddress);	

							DataBaseAdapter db = new DataBaseAdapter(Service_location.this);

							//===========================================================================================================
							//---insert a Records---
							//===========================================================================================================
							db.open();        

							if(minutes == 0 || minutes >= Integer.parseInt(getFromPreference("location_tracking_time_interval_min")))
							{
								Log.w("database","inserting Location to local database");
								TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
								String imei = telephonyManager.getDeviceId();
								long id = db.insertRecord(""+glat, ""+glng, ""+nlat, ""+nlng, imei, formattedDate, gAddress, nAddress);        
							}
							else
							{
								Log.e("Location Database","Location Database insertion time diff not matched");
							}
							db.close();
							saveInPreference("LastInsertedDateTime", formattedDate);
							//===========================================================================================================
							//---END insert a Records---
							//===========================================================================================================
							if(isOnline())
							{
								getRecordAndUpload();
							}
						}
						else
						{
							Log.e("Location"," "+nlat+" "+nlng+" "+glat+" "+glng);
							//Toast.makeText(getApplicationContext(), "Getting 0", Toast.LENGTH_LONG).show();
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, 10000);
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onStart(intent, startId);
	}

	//===========================================================================================
	//Get Rec from local database
	//===========================================================================================
	protected void getRecordAndUpload() 
	{
		try 
		{
			//---get all Records---
			DataBaseAdapter db = new DataBaseAdapter(this);
			db.open();
			Cursor c = db.getAllRecords();
			localDbRowCount = c.getCount();
			if (c.moveToFirst())
			{
				String glatAllValue = "";
				String glngAllValue = "";
				String nlatAllValue = "";
				String nlngAllValue = "";
				String mb_code_perfAllValue = "";
				String formattedDateAllValue = "";
				String gAddressAllValue = "";
				String nAddressAllValue = "";
				do 
				{   
					glatAllValue 		  = glatAllValue + "@#@" +c.getString(1);
					glngAllValue 		  = glngAllValue + "@#@" +c.getString(2);
					nlatAllValue 		  = nlatAllValue + "@#@" +c.getString(3);
					nlngAllValue 		  = nlngAllValue + "@#@" +c.getString(4);
					mb_code_perfAllValue  = mb_code_perfAllValue + "@#@" +c.getString(5);
					formattedDateAllValue = formattedDateAllValue + "@#@" +c.getString(6);
					gAddressAllValue 	  = gAddressAllValue + "@#@" +c.getString(7);
					nAddressAllValue 	  = nAddressAllValue + "@#@" +c.getString(8);
				} while (c.moveToNext());
				c.close();

				Log.e("Location","Sending location  to server");
				//		    Log.w("posting",glatAllValue+" --- "+ glngAllValue+" --- "+ nlatAllValue+" --- "+ nlngAllValue+" --- "+ mb_code_perfAllValue+" --- "+ formattedDateAllValue+" --- "+ gAddressAllValue +" --- "+ nAddressAllValue);
				new MyAsyncTask().execute(glatAllValue, glngAllValue, nlatAllValue, nlngAllValue, mb_code_perfAllValue, formattedDateAllValue, gAddressAllValue, nAddressAllValue);	
			}
			db.close();
		}
		catch (Exception e) 
		{
			// do something clever with the exception
			System.out.println(e.getMessage());
		}
	}
	//===========================================================================================
	//Get Rec from local database
	//===========================================================================================


	@Override
	public void onDestroy() 
	{
		try 
		{
			if(glocManager != null){
				glocManager.removeUpdates(glocListener);
				Log.d("ServiceForLatLng", "GPS Update Released");
			}
			if(nlocManager != null){
				nlocManager.removeUpdates(nlocListener);
				Log.d("ServiceForLatLng", "Network Update Released");
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}

	public class MyLocationListenerNetWork implements LocationListener	
	{
		@Override
		public void onLocationChanged(Location loc)
		{
			nlat = loc.getLatitude();
			nlng = loc.getLongitude();

			Log.d("LAT & LNG Network:", nlat + " " + nlng);

			if (!(nlat+"").equalsIgnoreCase("0.0") && !(nlng+"").equalsIgnoreCase("0.0"))
			{
				nAddress = showAddress(nlat, nlng);
				Log.d("Service_location","Network address "+nAddress);
			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Log.d("LOG", "Network is OFF!");
		}
		@Override
		public void onProviderEnabled(String provider)
		{
			Log.d("LOG", "Thanks for enabling Network !");
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	}

	public class MyLocationListenerGPS implements LocationListener	
	{
		@Override
		public void onLocationChanged(Location loc)
		{
			glat = loc.getLatitude();
			glng = loc.getLongitude();

			Log.d("LAT & LNG GPS:", glat + " " + glng);

			if (!(glat+"").equalsIgnoreCase("0.0") && !(glng+"").equalsIgnoreCase("0.0"))
			{
				gAddress = showAddress(glat, glng);
				Log.d("Service_location","GPS address"+gAddress);
			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Log.d("LOG", "GPS is OFF!");
		}
		@Override
		public void onProviderEnabled(String provider)
		{
			Log.d("LOG", "Thanks for enabling GPS !");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	}
	//===================================================================================================================================
	//sending Location to server
	//===================================================================================================================================
	private class MyAsyncTask extends AsyncTask<String, Integer, Double>{

		String responseBody = null;
		@Override
		protected Double doInBackground(String... params) 
		{
			postData(params[0],params[1],params[2],params[3],params[4],params[5],params[6],params[7]);
			return null;
		}

		protected void onPostExecute(Double result)
		{
			//			Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_LONG).show();

			if(responseBody!=null)
			{

				Log.e("Service Location:","Local Location table row: " + localDbRowCount);
				Log.e("Location result:","Server Location table row: " + responseBody);

				if(localDbRowCount == Integer.parseInt(responseBody.substring(0, responseBody.length()-1)))
				{
					Log.d("Location result:","All Location data from local table inserted to server");
					Toast.makeText(Service_location.this, "Location updated to server!! Please check your data base.", Toast.LENGTH_LONG).show();
					deleteLocalDbTAble();
				}
				else
				{
					Log.i("Location DB","Inserted rec in server != Local Db row");
				}

				
				processResponce(responseBody);
				onDestroy();
			}
			else
			{
				//		Toast.makeText(getApplicationContext(), "Location: Empty Responce.", Toast.LENGTH_LONG).show();
			}

		}
		protected void onProgressUpdate(Integer... progress){
		}

		public void postData(String gLatstr,String gLngstr, String nLatstr, String nLngstr, String imei, String dateTimestr, String gAddressstr, String nAddressstr) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(ReusableClass.base_url + "save_all_loaction.php");

			try 
			{
				// Data that I am sending
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("gLat", gLatstr));
				nameValuePairs.add(new BasicNameValuePair("gLng", gLngstr));
				nameValuePairs.add(new BasicNameValuePair("nLat", nLatstr));
				nameValuePairs.add(new BasicNameValuePair("nLng", nLngstr));
				nameValuePairs.add(new BasicNameValuePair("imei", imei));
				nameValuePairs.add(new BasicNameValuePair("dateTime", dateTimestr));
				nameValuePairs.add(new BasicNameValuePair("gAddress", gAddressstr));
				nameValuePairs.add(new BasicNameValuePair("nAddress", nAddressstr));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				responseBody = EntityUtils.toString(response.getEntity());
			} 
			catch (Throwable t ) {
				//Toast.makeText( getApplicationContext(),""+t,Toast.LENGTH_LONG).show();
				Log.d("Error Time of Login",t+"");
			} 
		}

		private void deleteLocalDbTAble() 
		{
			DataBaseAdapter db = new DataBaseAdapter(Service_location.this);
			//===========================================================================================================
			//---delete a Records---
			//===========================================================================================================
			db.open();        
			boolean id = db.deleteRecord(0);  
			if(id)
			{
				Log.i("Location DB","Local database deleted");
			}
			db.close();
			//===========================================================================================================
			//---END delete a Records---
			//===========================================================================================================
		}
	}
	//===================================================================================================================================
	//END sending EmailAddress and Password to server 
	//===================================================================================================================================


	//===================================================================================================================================
	//processing the XML got from server
	//===================================================================================================================================
	private void processResponce(String responceFromServer) 
	{
		if(responceFromServer.equalsIgnoreCase("YES"))
		{

		}
		//Do what ever with your server response
	}
	//===================================================================================================================================
	//processing the XML got from server
	//===================================================================================================================================


	//==========================================================================================================================================
	//getting address from lat lng 
	//========================================================================================================================================== 
	protected String showAddress(double lat, double lng) 
	{
		String address_name = "No address found (You can see the address by clicking map button)";
		try
		{
			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(Service_location.this, Locale.getDefault());
			addresses = geocoder.getFromLocation(lat, lng, 1);

			String address = addresses.get(0).getAddressLine(0);
			String city = addresses.get(0).getAddressLine(1);
			String country = addresses.get(0).getAddressLine(2);

			address_name = address+", "+city+", "+country;
			//Toast.makeText( MyServiceForLatLng.this,address+" , "+city+" , "+country,Toast.LENGTH_SHORT ).show();

		}
		catch (Throwable e) 
		{
			Log.d("address error: \n", ""+e);
			//Toast.makeText( Service_location.this,"Sorry could get your address.",Toast.LENGTH_SHORT ).show();
		}
		return address_name;
	}
	//==========================================================================================================================================
	//END getting address from lat lng
	//==========================================================================================================================================

	// --------------------------------------------
	// method to check Internet connectivity
	// --------------------------------------------
	public boolean isOnline() 
	{
		boolean isConnected;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) 
		{
			isConnected = true;
		} 
		else 
		{
			isConnected = false;
		}
		return isConnected;
	}
	// --------------------------------------------
	// END method to check Internet connectivity
	// --------------------------------------------

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
