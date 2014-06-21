package kr.khu.gps;

import java.net.URLEncoder;

import kr.khu.utils.Def;
import kr.khu.utils.HttpRequest;
import kr.khu.utils.SharePreferenceData;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener {
	 private final Context mContext;
	 
	    // flag for GPS status
	    boolean isGPSEnabled = false;
	 
	    // flag for network status
	    boolean isNetworkEnabled = false;
	 
	    // flag for GPS status
	    boolean canGetLocation = false;
	 
	    Location location; // location
	    double latitude; // latitude
	    double longitude; // longitude
	    int numberRequest = 0;
	    // The minimum distance to change Updates in meters
	    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
	 
	    // The minimum time between updates in milliseconds
	    private static final long MIN_TIME_BW_UPDATES = 50 * 60 * 1; // 1 minute

		protected static final String TAG = GPSTracker.class.getSimpleName();
	 
	    // Declaring a Location Manager
	    protected LocationManager locationManager;

		private Handler mHandler;
	    
	    /**
	     * 
	     * @param context
	     */
	    public GPSTracker(Context context) {
	        this.mContext = context;
	        getLocation();
	    }
	    @Override
	    public void onCreate() {
	    	mHandler = new Handler();
	    	super.onCreate();
	    }
	    /**
	     * get location of user
	     * @return
	     */
	    public Location getLocation() {
	        try {
	            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
	 
	            // getting GPS status
	            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	 
	            // getting network status
	            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	 
	            if (!isGPSEnabled && !isNetworkEnabled) {
	                // no network provider is enabled
	            } else {
	                this.canGetLocation = true;
	                // First get location from Network Provider
	                if (isNetworkEnabled) {
	                    locationManager.requestLocationUpdates(
	                            LocationManager.NETWORK_PROVIDER,
	                            MIN_TIME_BW_UPDATES,
	                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                    Log.d("Network", "Network");
	                    if (locationManager != null) {
	                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                        if (location != null) {
	                            latitude = location.getLatitude();
	                            longitude = location.getLongitude();
	                        }
	                    }
	                }
	                // if GPS Enabled get lat/long using GPS Services
	                if (isGPSEnabled) {
	                    if (location == null) {
	                        locationManager.requestLocationUpdates(
	                                LocationManager.GPS_PROVIDER,
	                                MIN_TIME_BW_UPDATES,
	                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                        Log.d("GPS Enabled", "GPS Enabled");
	                        if (locationManager != null) {
	                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                            if (location != null) {
	                                latitude = location.getLatitude();
	                                longitude = location.getLongitude();
	                            }
	                        }
	                    }
	                }
	            }
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	 
	        return location;
	    }
	     
	    /**
	     * Stop using GPS listener
	     * Calling this function will stop using GPS in your app
	     * */
	    public void stopUsingGPS(){
	        if(locationManager != null){
	            locationManager.removeUpdates(GPSTracker.this);
	        }       
	    }
	     
	    /**
	     * Function to get latitude
	     * */
	    public double getLatitude(){
	        if(location != null){
	            latitude = location.getLatitude();
	        }
	         
	        // return latitude
	        return latitude;
	    }
	     
	    /**
	     * Function to get longitude
	     * */
	    public double getLongitude(){
	        if(location != null){
	            longitude = location.getLongitude();
	        }
	         
	        // return longitude
	        return longitude;
	    }
	     
	    /**
	     * Function to check GPS/wifi enabled
	     * @return boolean
	     * */
	    public boolean canGetLocation() {
	        return this.canGetLocation;
	    }
	     
	    /**
	     * Function to show settings alert dialog
	     * On pressing Settings button will lauch Settings Options
	     * */
	    public void showSettingsAlert(){
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
	      
	        // Setting Dialog Title
	        alertDialog.setTitle("GPS is settings");
	  
	        // Setting Dialog Message
	        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
	  
	        // On pressing Settings button
	        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                mContext.startActivity(intent);
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
	    /**
	     * 
	     * @author QUOC NGUYEN
	     *
	     */
	    private class ToastMess extends AsyncTask<String, String, String> {
		String toastMessage;
		@Override
		protected String doInBackground(String... params) {
			toastMessage = params[0];
			return toastMessage;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
		}
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
		
	}
	@Override
	public void onLocationChanged(Location location) {
		//Log.d(TAG, "Your Location is - \nLat: " + latitude + "\nLong: " + longitude);  
		// update location here
		String regChildID = SharePreferenceData.getCheckedRegister(mContext);
	
			if ( regChildID.split(",")[0].equalsIgnoreCase("1")) {
						
				try {
					String locationData;
					String regID = regChildID.split(",")[2];
					locationData = URLEncoder.encode("reg_child_id", "UTF-8") + "=" + URLEncoder.encode(regID.substring(0, regID.length()-1), "UTF-8");
					locationData += "&" +  URLEncoder.encode("lat", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8");
					locationData += "&" +  URLEncoder.encode("long", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8");
					numberRequest = numberRequest +1;
					final String data = locationData;
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							String response = HttpRequest.sendData(Def.HTTP_METHOD_POST, Def.LOCATION_API, data);
							if (response != null) {
								//Log.d(TAG, "Response from server: " + data);
								Log.d(TAG, "Response from server: " + response);
								Log.d(TAG, "Number request update: " + numberRequest);
								if(response.equalsIgnoreCase("comeback\n")) {
									new Handler(Looper.getMainLooper()).post(new Runnable() {

							            @Override
							            public void run() {
							            	Toast.makeText(mContext, "Alert! Far away more than 200m, plz come back to the lab!", Toast.LENGTH_SHORT).show();
							            }
							        });
								}
							}else {
								Log.d(TAG, "Number request update: " + "No response from server");
							}
						} 
					}).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
