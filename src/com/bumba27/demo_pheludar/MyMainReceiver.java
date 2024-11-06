package com.bumba27.demo_pheludar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyMainReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		context.startService(new Intent(context, Service_location.class));
		//context.startService(new Intent(context, Service_recorded_call.class));
		Log.d("broadcast", "Inside broadcast location service");
	}
}