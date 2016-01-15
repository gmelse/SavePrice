package com.geo.saveprice;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
 
@SuppressLint("ShowToast")
public class FindLocation extends Service implements LocationListener {
	
	private static final String TAG = "FindLocation";
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 1 meters
 
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; //1000 * 20 * 1; // 20sec
    
	private final int CLASS_CODE_41 = 41, CLASS_CODE_42 = 42;
	
    private Context myContext;
    private boolean locationDetected = false;
 
    private Location myLocation;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;
 
    public FindLocation(Context myContext) {
        this.myContext = myContext;
        locationManager = (LocationManager) myContext.getSystemService(LOCATION_SERVICE);
    }
    
    public void findMyLocation(boolean useNetwork) {
        try {
        	
        	String provider = "";
        	
        	if(checkInternet(myContext)){
        		if(!useNetwork){
        			if(isGpsAvailable()){
                		if(isGPSEnabled()){
                			provider = LocationManager.GPS_PROVIDER;
                		}else{
                			String message = "Please enable your GPS";
                			String title = "Warning";
                			MyAlertDialog mad = new MyAlertDialog(myContext, message, title, this.CLASS_CODE_41);
                			mad.showAlertDialog();
                		}
                	}else{
                		provider = LocationManager.NETWORK_PROVIDER;
                	}
        		}else{
        			provider = LocationManager.NETWORK_PROVIDER;
        		}

                locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if ((locationManager != null) && !(provider.equals(""))){
                    myLocation = locationManager.getLastKnownLocation(provider);
                    if (myLocation != null){
    	                latitude = getLatitude();
    	                longitude = getLongitude();
    	                Log.v(TAG, "lat:" + latitude + " and " + "long" + longitude);
    	                locationDetected = true;
                    }else{
                    	String message = "Your location haven't detected! Please try again or Use network provider";
            			String title = "Warning";
            			MyAlertDialog mad = new MyAlertDialog(myContext, message, title, this.CLASS_CODE_42);
            			mad.showAlertDialog();
    				}
                }
        	}else{
        		Toast toast = Toast.makeText(myContext, "Please open your Internet connection", 1000);
				toast.show();
        	}
        	   
              
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
  
    public boolean isGPSEnabled(){
    	return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    
    public boolean isNetworkEnabled() {
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
  
    public boolean checkInternet(Context ctx) {
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        return wifi.isConnected() || mobile.isConnected();
    }
    
    //Check if a device has GPS
    public boolean isGpsAvailable() {
        PackageManager pm = myContext.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            return true;
        } else {
            return false;
        }
        
        /*final List<String> providers = locationManager.getAllProviders();
        if (providers == null) return false;
        return providers.contains(LocationManager.GPS_PROVIDER);*/
    }
    
    public LocationManager getLocationManager() {
		return locationManager;
	}

	public double getLatitude(){
        return myLocation.getLatitude();
    }

    public double getLongitude(){
        return myLocation.getLongitude();
    }

	public boolean isLocationDetected() {
		return locationDetected;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
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
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}