package com.bumba27.demo_pheludar;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumba27.utils.ReusableClass;

public class AddJobActivity extends Activity{
	
	EditText editTextJobId;
	private ProgressDialog pgLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addjobactivity);

		editTextJobId = (EditText)findViewById(R.id.editTextJobId);
	}
	
	public void savingJobId(View v) 
	{
		String jobId = editTextJobId.getText().toString();
		
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String job_start = sdf.format(new Date());
		
		if(jobId.length()>0)
		{
			if(ReusableClass.isConnectingToInternet(AddJobActivity.this))
			{
				//Showing progress dialog 
				pgLogin = new ProgressDialog(AddJobActivity.this);
				pgLogin.setMessage("Please wait a min ...");
				pgLogin.setIndeterminate(true);
				pgLogin.setCancelable(true);
				pgLogin.setCanceledOnTouchOutside(false);

				pgLogin.show();

				new MyAsyncAddJob().execute(jobId, imei, job_start);	

			}
			else
			{
				Toast.makeText(this, "Sorry!! No internet connection.", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			Toast.makeText(this, "Please type a job id !!", Toast.LENGTH_LONG).show();
		}
	}
	
	//===================================================================================================================================
	//Creating a job
	//===================================================================================================================================

	private class MyAsyncAddJob extends AsyncTask<String, Integer, Double>{

		String responseBody = null;
		@Override
		protected Double doInBackground(String... params) 
		{
			postData(params[0],params[1],params[2]);
			return null;
		}

		protected void onPostExecute(Double result)
		{
			if("YES\n".equalsIgnoreCase(responseBody))
			{
				Toast.makeText(getApplicationContext(), "Your job added. Thank you.", Toast.LENGTH_LONG).show();
				
				Intent i = new Intent(AddJobActivity.this, MainActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Sorry! Job not added.", Toast.LENGTH_LONG).show();
			}
			
			if (pgLogin.isShowing()) 
			{
				pgLogin.cancel();
				pgLogin.dismiss();
			}
			Log.i("TAG", "Server Responce:" + responseBody);
		}
		protected void onProgressUpdate(Integer... progress){
		}

		public void postData(String jobId, String imei, String job_start) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(ReusableClass.base_url + "create_job.php");

			try 
			{
				// Data that I am sending
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("jobId", jobId));
				nameValuePairs.add(new BasicNameValuePair("imei", imei));
				nameValuePairs.add(new BasicNameValuePair("job_start", job_start));

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
	}
	//===================================================================================================================================
	//END Creating a job
	//===================================================================================================================================
}
