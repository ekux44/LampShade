package com.kuxhausen.huemore;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.kuxhausen.huemore.DatabaseDefinitions.PreferencesKeys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class RegisterWithHubDialogFragment extends DialogFragment {

	public final long length_in_milliseconds = 15000;
	public final long period_in_milliseconds = 1000;
	public ProgressBar progressBar;
	public CountDownTimer countDownTimer;
	public Register networkRegister = new Register();
	public Context parrentActivity;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		parrentActivity = this.getActivity();
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View registerWithHubView = inflater.inflate(R.layout.register_with_hub,
				null);
		builder.setView(registerWithHubView);
		progressBar = (ProgressBar) registerWithHubView.findViewById(R.id.timerProgressBar);
		
		
		countDownTimer = new CountDownTimer(length_in_milliseconds,period_in_milliseconds) {
	        private boolean warned = false;
	        @Override
	        public void onTick(long millisUntilFinished) {
	        	if(isAdded()){
	        		progressBar.setProgress( (int) (((length_in_milliseconds-millisUntilFinished)*100.0)/length_in_milliseconds));
	        		networkRegister = new Register();
	        		networkRegister.execute(parrentActivity);
	        	}
	        }

	        @Override
	        public void onFinish() {
	        	if(isAdded()){
		        	//try one last time
		        	networkRegister = new Register();
			        networkRegister.execute(parrentActivity);
			        
			        //launch the failed registration dialog
			        RegistrationFailDialogFragment rfdf = new RegistrationFailDialogFragment();
					rfdf.show(getFragmentManager(), "dialog");
					
					dismiss();
	        	}
	        }
	    };
	    countDownTimer.start();
		
	    
		// Create the AlertDialog object and return it
		return builder.create();
	}
	public class Register extends AsyncTask<Object, Void, Boolean> {

		Context cont;

		public String getBridge(){
			return "192.168.1.100"; //TODO replace with proper network abstraction
		}
		public String getUserName(){
			return "728e44cf55cd29a0ae0fa801bc8b0bb9"; //TODO replace with device specific MD5 hash
		}
		public String getDeviceType(){
			return getString(R.string.app_name);
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			if(isAdded()){
				// Get session ID
				cont = (Context) params[0];
				Log.i("asyncTask", "doing");
				
				// Create a new HttpClient and Post Header
			    HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost("http://"+getBridge()+"/api/");
	
			    try {
			        RegistrationRequest request = new RegistrationRequest();
			        request.username = getUserName();
			        request.devicetype = getDeviceType();
			    	Gson gson = new Gson();
			    	String registrationRequest = gson.toJson(request);
			    	
			        StringEntity se = new StringEntity(registrationRequest);
	
			        //sets the post request as the resulting string
			        httppost.setEntity(se);
			        //sets a request header so the page receiving the request will know what to do with it
			        httppost.setHeader("Accept", "application/json");
			        httppost.setHeader("Content-type", "application/json"); 
			        
			        // execute HTTP post request
			        HttpResponse response = httpclient.execute(httppost);
			        
			        // analyze the response
			        String responseString = EntityUtils.toString(response.getEntity());
			        responseString = responseString.substring(1, responseString.length()-1);//pull off the outer brackets
			        RegistrationResponse responseObject = gson.fromJson(responseString, RegistrationResponse.class);
			        if (responseObject.success!=null)
			        	return true;
			        
			    } catch (ClientProtocolException e) {
			    	Log.e("asdf","ClientProtocolException: " +e.getMessage());
			    	// TODO Auto-generated catch block
			    } catch (IOException e) {
			    	Log.e("asdf","IOException: "+e.getMessage());
			    	// TODO Auto-generated catch block
			    }
			}
			Log.i("asyncTask", "finishing");
			return false;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			Log.i("asyncTask", "finished");
			if(success && isAdded()){
				countDownTimer.cancel();
				
				//Show the success dialog
				RegistrationSuccessDialogFragment rsdf = new RegistrationSuccessDialogFragment();
				rsdf.show(getFragmentManager(), "dialog");
				
				//Add username and IP to preferences cache
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(parrentActivity);

				Editor edit = settings.edit();
				edit.putString(PreferencesKeys.Bridge_IP_Address, getBridge());
				edit.putString(PreferencesKeys.Hashed_Username, getUserName());
				edit.commit();
				
				
				
				//done with registration dialog
				dismiss();
			}
		}
	}
}