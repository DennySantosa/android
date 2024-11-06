package com.bumba27.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.bumba27.demo_pheludar.MainActivity;

public class ReusableClass 
{
	public static String base_url = "http://www.foodguru.ie/tracking/";
//	public static String base_url = "http://anirban27.byethost9.com/crm_admin/Services/location/";
	
	public static boolean isConnectingToInternet(Activity activity)
	{
		ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) 
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) 
				for (int i = 0; i < info.length; i++) 
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}

		}
		return false;
	}
}
