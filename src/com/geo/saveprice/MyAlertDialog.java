package com.geo.saveprice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

/**
 * 
 * This class shows AlertDialogs.
 * CLASS CODE correspondences
 * 0-->MainActivity
 * 1-->SendToServerAsync
 * 2-->LanguageAsyncTask
 * 3-->Default
 * 41,42-->FindLocation
 *
 */

@SuppressLint("ShowToast")
public class MyAlertDialog {
	
	private final int DEFAULT_CODE = 3; //Is used for standard messages with a title, message and an OK Button!
	private String languagePath = Environment.getExternalStorageDirectory() + "/MyApp/";
	
	private AlertDialog.Builder builder;
	private Context myContext;
	private String languageName, languageCode, message, title;
	private int classCode;
	
	//Constructor's-----------------------------------------------------------------------------------
	public MyAlertDialog(Context myContext, String message, String title){
		this.myContext = myContext;
		this.message = message;
		this.title = title;
		this.classCode = DEFAULT_CODE;
	}
	
	public MyAlertDialog(Context myContext, String message, String title, int classCode){
		this.myContext = myContext;
		this.message = message;
		this.title = title;
		this.classCode = classCode;
	}
	
	public MyAlertDialog(Context myContext, String message, String title, String languageName, 
			String languageCode, int classCode){
		this.myContext = myContext;
		this.message = message;
		this.title = title;
		this.languageName = languageName;
		this.languageCode = languageCode;
		this.classCode = classCode;
	}
	//-----------------------------------------------------------------------------------------------
	
	public void showAlertDialog(){
	    builder = new AlertDialog.Builder(myContext);
	    setButtons(); 
	    AlertDialog icDialog = builder.create();
	    icDialog.show();
	}
	
	public void setButtons(){
		setMessageToBuilder(message, title);
	    setPositiveButton();
	    setNegativeButton();
		setNeutralButtonToSettings();
	}
	
	public void setMessageToBuilder(String message, String title){
		builder.setMessage(message)
	    	   .setTitle(title);
	}
	
	/**
	  * 0-->MainActivity
	  * 1-->SendToServerAsync
	  * 2-->LanguageAsyncTask
	  * 3-->Default
	  * 41,42-->FindLocation
	  */
	
	public void setNeutralButtonToSettings(){
		if ((classCode == 1) || (classCode == 2) || (classCode == 41)){
			builder.setNeutralButton("Settings", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		        	if(classCode == 41){
		        		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		        		myContext.startActivity(intent);
		        	}else{
		        		Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
		        		myContext.startActivity(intent);
		        	}
		          AlertDialog icDialog = builder.create();
		          icDialog.show();
		        }
		    });
		}
	}
	
	/**
	  * 0-->MainActivity
	  * 1-->SendToServerAsync
	  * 2-->LanguageAsyncTask
	  * 3-->Default
	  * 4-->FindLocation
	  */
	
	public void setPositiveButton(){
		
		if(classCode == 0){
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		     	   ((MainActivity) myContext).increasePhotoCounter();
		     	   ((Activity) myContext).startActivityForResult(((MainActivity) myContext).getCaptureIntent(), ((MainActivity) myContext).getCaptureImageActivityRequestCode());
		        }
		    });
		}else if(classCode == 1){
			builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		     	   startServerAsync(false);
		        }
		    });
		}else if(classCode == 2){
			builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		     	   ProgressDialog pDialog = new ProgressDialog(myContext);
		     	   new LanguageAsyncTask(pDialog, languageCode, languageName, myContext).execute(languagePath);
		        }
		    });
		
		}else if(classCode == 3){
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		     	   
		        }
		    });
		}else if(classCode == 41){
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		        	FindLocation ref = new FindLocation(myContext);
		        	boolean isGPSEnabled = ref.isGPSEnabled();
		        	if(!isGPSEnabled){
		        		AlertDialog icDialog = builder.create();
				        icDialog.show();
		        	}else{
		        		startServerAsync(false);
		        	}
		        }
		    });
		}else if(classCode == 42){
			builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		        	FindLocation ref = new FindLocation(myContext);
		        	boolean isGPSEnabled = ref.isGPSEnabled();
		        	if(!isGPSEnabled){
		        		AlertDialog icDialog = builder.create();
				        icDialog.show();
		        	}else{
		        		startServerAsync(false);
		        	}
		        }
		    });
		}
		
	}
	
	/**
	  * 0-->MainActivity
	  * 1-->SendToServerAsync
	  * 2-->LanguageAsyncTask
	  * 3-->Default
	  * 41,42-->FindLocation
	  */
	
	public void setNegativeButton(){
		
		if(classCode == 0){
   		 builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
   		        public void onClick(DialogInterface dialog, int id) {
   		     	   ((MainActivity) myContext).startOCR();
   		        }
   		    });
   		 
		}else if(classCode == 41 || classCode == 42){
			builder.setNegativeButton("Use Network", new DialogInterface.OnClickListener() {
   		        public void onClick(DialogInterface dialog, int id) {
   		        	startServerAsync(true);
   		        }
   		    });
   		 
   	 	}else{
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
			    	if(classCode == 1){
			    		((MainActivity) myContext).setOcrResult("");
			    		String temp = "Old Products:\n\n";
			    		for(int i=0; i<MyProducts.getProducts().size(); i++){
			    			temp = temp + MyProducts.getProducts().get(i).getName() + "  "
										+ MyProducts.getProducts().get(i).getPrice() + "\n";
						}
			    		temp = temp + "\n";
			    		((MainActivity) myContext).setOcrResult(temp);
			    		
			    	}else if(classCode == 2){
			    		 Toast toast = Toast.makeText(myContext, "Try to download from Options Menu else you can't use this app", 2000);
			    		 toast.show();
			        }
				}
			
			});
   	 	}
		
	}
	
	public void startServerAsync(boolean useNetwork){
		FindLocation myLocation = new FindLocation(myContext);
		myLocation.findMyLocation(useNetwork);
		if(myLocation.isLocationDetected()){
			String latitude = Double.toString(myLocation.getLatitude());
			String longitude = Double.toString(myLocation.getLongitude());
			ProgressDialog pDialog = new ProgressDialog(myContext);
			new SendToServerAsync(myContext, pDialog, ((MainActivity) myContext).getOcrResult()).execute(latitude, longitude);
		}
	}
	
	
}
