package com.bumba27.demo_pheludar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.bumba27.utils.ReusableClass;

public class JobListForDelete extends ListActivity{
	
	private ProgressDialog pgLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(ReusableClass.isConnectingToInternet(JobListForDelete.this))
		{
			//Showing progress dialog 
			pgLogin = new ProgressDialog(JobListForDelete.this);
			pgLogin.setMessage("Please wait a min ...");
			pgLogin.setIndeterminate(true);
			pgLogin.setCancelable(true);
			pgLogin.setCanceledOnTouchOutside(false);

			pgLogin.show();

			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			String imei = telephonyManager.getDeviceId();

			new MyAsyncGetJobs().execute(imei);	

		}
		else
		{
			Toast.makeText(this, "Sorry!! No internet connection.", Toast.LENGTH_LONG).show();
		}
	}


	//===================================================================================================================================
	//Getting all job
	//===================================================================================================================================

	private class MyAsyncGetJobs extends AsyncTask<String, Integer, Double>{

		String responseBody = null;
		@Override
		protected Double doInBackground(String... params) 
		{
			postData(params[0]);
			return null;
		}

		protected void onPostExecute(Double result)
		{
			if (pgLogin.isShowing()) 
			{
				pgLogin.cancel();
				pgLogin.dismiss();
			}
			Log.i("TAG", "Server Responce:" + responseBody);
			
			if(responseBody!= null)
				processJob(responseBody);
			else
				Toast.makeText(JobListForDelete.this, "No responce from server.", Toast.LENGTH_LONG).show();
		}
		protected void onProgressUpdate(Integer... progress){
		}

		public void postData(String imei) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(ReusableClass.base_url + "get_all_jobs.php");

			try 
			{
				// Data that I am sending
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("imei", imei));

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


	//=========================Process xml===========================
	private void processJob(String responceFromServer) 
	{
		String loginStatus = null;
		try 
		{
			String readString = new String(responceFromServer);

			// getting the xml Value as per child node form the saved xml
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(readString.getBytes("UTF-8"));
			Document doc = db.parse(is);

			NodeList root = doc.getElementsByTagName("root");

			for (int i = 0; i < root.getLength(); i++) 
			{
				loginStatus = "" + ((Element) root.item(i)).getAttribute("status");
				Log.d("Status: ", loginStatus);
			}

			if (loginStatus.equalsIgnoreCase("Y")) 
			{
				NodeList mb = doc.getElementsByTagName("JobList");
				 String[] job_id = new String[mb.getLength()];

				for (int i = 0; i < mb.getLength(); i++) 
				{
					String id 							= "" + ((Element) mb.item(i)).getAttribute("id");
					String driver_id 					= "" + ((Element) mb.item(i)).getAttribute("driver_id");
					String imei_code 					= "" + ((Element) mb.item(i)).getAttribute("driver_phone");
					String driver_phone 				= "" + ((Element) mb.item(i)).getAttribute("call_record_tracking_to");
					String customer_phone 				= "" + ((Element) mb.item(i)).getAttribute("customer_phone");
					String job_start 					= "" + ((Element) mb.item(i)).getAttribute("job_start");
					String job_end 					 	= "" + ((Element) mb.item(i)).getAttribute("job_end");
					String job_status 					= "" + ((Element) mb.item(i)).getAttribute("job_status");
					String job_payment 					= "" + ((Element) mb.item(i)).getAttribute("job_payment");
					
					job_id[i] = "Job id: " + customer_phone + " (Status: " + job_status + ")";
				}
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, job_id);
				setListAdapter(adapter); 
			} 
			else if (loginStatus.equalsIgnoreCase("N")) 
			{
				NodeList mb = doc.getElementsByTagName("JobList");

				for (int i = 0; i < mb.getLength(); i++) 
				{
					Log.d("Error","" + ((Element) mb.item(i)).getAttribute("Message"));
				}
			}
		} 
		catch (Throwable t) 
		{
			Log.d("Error On Saving and reading", t + "");
		}
	}
	
	  
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {
          super.onListItemClick(l, v, position, id);
  
             // ListView Clicked item index
             int itemPosition     = position;
             
             // ListView Clicked item value
             String  itemValue    = (String) l.getItemAtPosition(position);
                
            //Toast.makeText(this, "Click : \n  Position :"+itemPosition+"  \n  ListItem : " +itemValue, Toast.LENGTH_LONG).show();
             
//             Intent i = new Intent(this, EditJobPopUp.class);
//             i.putExtra("JobId", itemValue.substring(8, itemValue.indexOf("(")-1));
//             startActivity(i);
             
             AlertDialog diaBox = AskOption(itemValue.substring(8, itemValue.indexOf("(")-1));
             diaBox.show();
    }

    
    private AlertDialog AskOption(final String jobId)
    {
       AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this) 
           //set message, title, and icon
           .setTitle("Delete") 
           .setMessage("Are you sure?You want to delete this Job Id?") 
           .setIcon(android.R.drawable.ic_input_delete)

           .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

               public void onClick(DialogInterface dialog, int whichButton) { 
            	   
            	 //Showing progress dialog 
       			pgLogin = new ProgressDialog(JobListForDelete.this);
       			pgLogin.setMessage("Please wait a min ...");
       			pgLogin.setIndeterminate(true);
       			pgLogin.setCancelable(true);
       			pgLogin.setCanceledOnTouchOutside(false);

       			pgLogin.show();
       			
       			new MyAsyncDeleteJob().execute(jobId);	
                dialog.dismiss();
               }   
           })

           .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {

                   dialog.dismiss();

               }
           })
           .create();
           return myQuittingDialogBox;

       }

	@Override
	public void onBackPressed() 
	{
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
		finish();
	}
	
	
	//===================================================================================================================================
	//Creating a job
	//===================================================================================================================================

	private class MyAsyncDeleteJob extends AsyncTask<String, Integer, Double>{

		String responseBody = null;
		@Override
		protected Double doInBackground(String... params) 
		{
			postData(params[0]);
			return null;
		}

		protected void onPostExecute(Double result)
		{
			if (pgLogin.isShowing()) 
			{
				pgLogin.cancel();
				pgLogin.dismiss();
			}
			Log.i("TAG", "Server Responce:" + responseBody);
			
			if("YES\n".equalsIgnoreCase(responseBody))
			{
				Toast.makeText(getApplicationContext(), "Your job deleted. Thank you.", Toast.LENGTH_LONG).show();
				onResume();
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Sorry! Job not deleted.", Toast.LENGTH_LONG).show();
			}
		}
		protected void onProgressUpdate(Integer... progress){
		}

		public void postData(String jobId) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(ReusableClass.base_url + "delete_job_id.php");

			try 
			{
				// Data that I am sending
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("job_id", jobId));

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