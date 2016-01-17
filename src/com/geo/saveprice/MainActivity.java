package com.geo.saveprice;

import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.opencv.android.OpenCVLoader;

import com.geo.saveprice.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class MainActivity extends Activity implements OnClickListener{
	
	/** ISO 639-3 language code indicating the default recognition language. */
	public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng+ell";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int HELP_ACTIVITY_REQUEST_CODE = 101;
	private static final int MANUAL_ADD_ACTIVITY_REQUEST_CODE = 102;
	private final int CLASS_CODE = 0;
	
	/** Resource to use for data file downloads. */
	static final String DOWNLOAD_BASE_5 = "https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.";
	
	public static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";
	
	private Animation ocrAnimation, reocrAnimation;
	
	// TAGS
	private static final String TAG = "MainActivity";
	
	// Objects
	private Button ocrButton, reOcrButton, serverButton, deleteButton, manualAddButton;
	private boolean reOcrButtonPressed = false;
	private EditText ocrResult;
	private View progressBar;
	
	// Paths
	private String languagePath = Environment.getExternalStorageDirectory() + "/MyApp/";
	private String hashMap_Path = Environment.getExternalStorageDirectory() + "/MyApp/HashMap/";
	
	// Context menu
	private static final int SETTINGS_ID = Menu.FIRST;
	private static final int ABOUT_ID = Menu.FIRST + 1;
	
	private ProgressDialog dialog;
	private String languageCode;
	
	private SharedPreferences prefs;
	private OnSharedPreferenceChangeListener listener;
	
	private static boolean isFirstLaunch;
	
	private Intent captureIntent, manualAddIntent;
	private OcrAsyncTask aTask;
	private static int photoCounter;
	
	public static Handler myHandler;
	
	private WriteReadData wrd;
	private Map<String, String> map; //This map is used for: OcrResult --> name in the Dictionary
	private Map<String, String> parallelMap; //This map is used for: name in the Dictionary --> a Real Name
	
	/*For example if Tesseract produces:CHIPS LΑΥS PIFANH 130rv
	 *CHIPS LΑΥS PIFANH 130rv-->CHΙPS LAΥS ΡΙΓΑΝΗ 130ΓΡ
	 *CHΙPS LAΥS ΡΙΓΑΝΗ 130ΓΡ-->CHIPS LAYS ME ΡΙΓΑΝΗ (This the name will be saved to the database)
	 */
	
	//References for the languages data files
	private File testFile1, testFile2, tessdataDir;
	
	private String message = "In order the app to work properly needs Greek and English language data files. "
							 + "If you haven't already download them try from the Options Menu!";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		photoCounter = 0;
		
		checkFirstLaunch();
		
		if (isFirstLaunch) {
		      setDefaultPreferences();
		}

        ocrAnimation = AnimationUtils.loadAnimation(this, R.animator.ocr_but_anim);
        reocrAnimation = AnimationUtils.loadAnimation(this, R.animator.re_ocr_but_anim);

        ocrButton = (Button) findViewById(R.id.button1);
        ocrButton.setOnClickListener((android.view.View.OnClickListener) this);

        ocrResult = (EditText) findViewById(R.id.editText1);
        
        reOcrButton = (Button) findViewById(R.id.button2);
        reOcrButton.setOnClickListener((android.view.View.OnClickListener) this);
        reOcrButton.setEnabled(false);
        
        serverButton = (Button) findViewById(R.id.button3);
        serverButton.setOnClickListener((android.view.View.OnClickListener) this);
        progressBar = findViewById(R.id.progressBar1);
        progressBar.setActivated(false);
        
        deleteButton = (Button) findViewById(R.id.button4);
        deleteButton.setOnClickListener((android.view.View.OnClickListener) this);
        
        manualAddButton = (Button) findViewById(R.id.button5);
        manualAddButton.setOnClickListener((android.view.View.OnClickListener) this);
        
        dialog = new ProgressDialog(this);
        
        //initialization for Opencv
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        	Toast toast = Toast.makeText(MainActivity.this, "OpenCV Initialization error..Restart the application", 1000);
       		toast.show();
        }
        
        tessdataDir = new File(languagePath + "tessdata"); //+ File.separator 
    	testFile1 = new File(tessdataDir, "ell.traineddata");
    	testFile2 = new File(tessdataDir, "eng.traineddata");
    	
    	setMap(new HashMap<String, String>());
		setParallelMap(new HashMap<String, String>());
		
		wrd = new WriteReadData(this);
		
		File file = new File(hashMap_Path, "parallelHashMap_Values.ser");
		
		if(isFirstLaunch || (!file.exists()) ){ //In a possible update of the app the file will exist and then we 
												//need to re-create the file. Show the updated version open for 
												//first launch.
			for(int i=0; i<Dictionary.masoutisDict.length-3; i++){
				parallelMap.put(Dictionary.masoutisDict[i], Dictionary.masoutisParallelDict[i]);
				Log.v(TAG, "Masoutis ParallelDict created");
			}
			for(int i=0; i<Dictionary.lidlDict.length-5; i++){
				parallelMap.put(Dictionary.lidlDict[i], Dictionary.lidlParallelDict[i]);
				Log.v(TAG, "lidl ParallelDict created");
			}
			for(int i=0; i<Dictionary.marinopoulosDict.length-5; i++){
				parallelMap.put(Dictionary.marinopoulosDict[i], Dictionary.marinopoulosParallelDict[i]);
				Log.v(TAG, "Marinopoulos ParallelDict created");
			}
			//Save the file
			wrd.saveHashMap(parallelMap, "parallelHashMap_Values.ser");
		}
		
		wrd.loadHashMaps(); //load all the HashMaps saved in files
			
		//MONO GIA PRINT STO LOGCAT
		for (Map.Entry<String,String> entry : parallelMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			Log.v(TAG, "mapKey:"  + key + " mapvalue:" + value);
		}
			
		wrd.loadAndShowProductsFromFile();

    	Log.v(TAG, "start");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
	    menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
	    menu.add(0, ABOUT_ID, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    Intent intent;
	    switch (item.getItemId()) {
	    case SETTINGS_ID: {
	      intent = new Intent().setClass(this, PreferencesActivity.class);
	      startActivity(intent);
	      break;
	    }
	    case ABOUT_ID: {
	      intent = new Intent(this, HelpActivity.class);
	      intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, HelpActivity.ABOUT_PAGE);
	      startActivity(intent);
	      break;
	    }
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	  protected void onPause() {
	    super.onPause();
	  }
	
	@Override
	  protected void onResume() {
	    super.onResume();
	  }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
		    Log.d(this.getClass().getName(), "back button pressed");
		    
			wrd.saveHashMap(map, "HashMap_Values.ser");
			//Check if there are new products scanned in the last minutes
			//and copy them to oldProducts for save
			if(!MyProducts.getProducts().isEmpty()){
				MyProducts.addToOldProducts(MyProducts.getProducts());
				MyProducts.getProducts().clear();
			}
			if(!MyProducts.getOldProducts().isEmpty()){
				//Saves all products found in oldProducts ArrayList
				wrd.saveProducts(false);
			}
		    
		}
		return super.onKeyDown(keyCode, event);
	}

    public int getCaptureImageActivityRequestCode() {
		return CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
	}

	public Intent getCaptureIntent() {
		return captureIntent;
	}

	public void increasePhotoCounter(){
		photoCounter++;
	}

	public static int getPhotoCounter() {
		return photoCounter;
	}

	public void onClick(final View v) {
		
		if(v.getId() == R.id.button1){ //OCR Button
			ocrAnimation.setAnimationListener(new AnimationListener() {
	            @Override
	            public void onAnimationStart(Animation animation) {}

	            @Override
	            public void onAnimationRepeat(Animation animation) {}

	            @Override
	            public void onAnimationEnd(Animation animation) {

	            	//Check if language files exists else don't start OCR Task!!
	            	if (testFile1.exists() && testFile2.exists()) {
		    				photoCounter = 0;
		    				reOcrButtonPressed = false;
		    				ocrResult.setText("");
		    				captureIntent = new Intent(MainActivity.this, CameraActivity.class);
		    				startActivityForResult(captureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	                }else{
	                	
	        			showMessage();
	                	//showAlertDialog(message);
	                }
	            }
	        });
			
			v.startAnimation(ocrAnimation);
			
		}else if(v.getId() == R.id.button2){ //RE-OCR Button
			reocrAnimation.setAnimationListener(new AnimationListener() {
	            @Override
	            public void onAnimationStart(Animation animation) {}

	            @Override
	            public void onAnimationRepeat(Animation animation) {}

	            @Override
	            public void onAnimationEnd(Animation animation) {
	            	ocrResult.setText("");
	            	reOcrButtonPressed = true;
	            	startOCR();
	            }     	
	        });
			
			v.startAnimation(reocrAnimation);
			
		}else if(v.getId() == R.id.button3){ //SEND TO SERVER Button
			
			reOcrButton.setEnabled(false);
			boolean canStartServerAsync = false; //This boolean variable indicates that we can start SendToServerAsync
			
			if (checkIfText()){ //Check if there is data to send
				/*Check if the editext contains accurate data. This means that the
				 *names of the products which are shown in the screen are right and 
				 *their prices also.
				 *They can be wrong if the user insert a name which isn't in the 
				 *the Dictionary or has mistakes.
				 *This case can happen if a user edit the editext. In this case 
				 *the system will not allow the user to send wrong data to database.
				 */
				checkTheEditText();
				canStartServerAsync = true;
			}
			
			//Check Network and GPS Connectivity 
			if(canStartServerAsync){
				FindLocation myLocation = new FindLocation(this);
				myLocation.findMyLocation(false); //false means: don't use Network Provider to find myLocation
				if(myLocation.isLocationDetected()){
					String latitude = Double.toString(myLocation.getLatitude());
					String longitude = Double.toString(myLocation.getLongitude());
					new SendToServerAsync(this, dialog, ocrResult).execute(latitude, longitude);
				}
					
			}else{
				Toast toast = Toast.makeText(this, "No Data For Sending", 1000);
				toast.show();
			}
			
			
		}else if(v.getId() == R.id.button4){ //DELETE Button
			ocrResult.setText("");
			MyProducts.getProducts().clear();
			MyProducts.getOldProducts().clear();
		}else if(v.getId() == R.id.button5){ //MANUAL ADD Button
			manualAddIntent = new Intent(MainActivity.this, ManualAddActivity.class);
			startActivityForResult(manualAddIntent, MANUAL_ADD_ACTIVITY_REQUEST_CODE);
			//startActivity(manualAddIntent);
		}
	}
	
	//Check if there is text in the editext to send to server
	public boolean checkIfText(){
		if (ocrResult.getText().toString().equals("")){
			return false;
		}else return true;
	}
	
	private void checkTheEditText() {
		String[] lines;
		
		//Clear all ArrayLists because the products we want to send
		//are in the editText and they can be changed by the user.
		//This means that the products to send are the latest found 
		//in the ediText
		MyProducts.getProducts().clear();
		MyProducts.getOldProducts().clear();
		
		lines = ocrResult.getText().toString().split(System.getProperty("line.separator"));

		for(int i=0; i<lines.length; i++){
			
			Log.v(TAG, "Before if:" + i + lines[i].equals(""));
			if(!lines[i].equals("New Products:") && !lines[i].equals("Old Products:") && !lines[i].equals("")){
				Log.v(TAG, "Line:" + i + " " + lines[i]);
				String[] analyze = lines[i].split(" ");
				
				for(int k=0; k<analyze.length;k++){
					Log.v(TAG, "Words in line:" + analyze[k]);
				}
				
				String price = analyze[analyze.length-1];
				String name = "";
				for(int j=0; j<analyze.length-1; j++){
					if(j==0){
						name = analyze[j];
					}else{
						if(!analyze[j].equals("")){
							name = name + " " + analyze[j];
						}
					}
				}
				
				Product aProduct = new Product(name, price);
				MyProducts.getProducts().add(aProduct);
				
			}
		}
		  
	}

	public void showMessage(){
		String title = "Warning";
		MyAlertDialog mad = new MyAlertDialog(this, message, title);
		mad.showAlertDialog();
	}
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.v(TAG, "onActivityResult:" + requestCode);
		
    	if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
            	
            	String message = "Do you have more receipt for scanning?";
    			String title = "Scanner";
    			MyAlertDialog mad = new MyAlertDialog(this, message, title, this.CLASS_CODE);
    			mad.showAlertDialog();
            	
            } else if (resultCode == RESULT_CANCELED) {
            	Toast toast = Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
            	toast.show();
            } else {
            	Toast toast = Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
            	toast.show();
            }
    	}else if (requestCode == HELP_ACTIVITY_REQUEST_CODE){
    		
    		Log.v(TAG, "HelpActivityResult");
    		
    		if (resultCode == RESULT_OK) {
    			Log.v(TAG, "Result Ok");
    			new LanguageAsyncTask(dialog, "ell", "Greek", this).execute(languagePath); //getApplicationContext()
    	        //engTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, languagePath);

    	        Handler handler = new Handler(); 
    	        handler.postDelayed(new Runnable() { 
    	             public void run() { 
    	            	 new LanguageAsyncTask(dialog, "eng", "English", MainActivity.this).execute(languagePath);
    	                 //ellTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, languagePath);
    	             } 
    	        }, 3000); 
    			
    		} else if (resultCode == RESULT_CANCELED) {
            	Log.v(TAG, "Result Cancel");
            	Toast toast = Toast.makeText(this, "Help Activity Canceled", Toast.LENGTH_SHORT);
            	toast.show();
            } else {
            	Log.v(TAG, "Result other");
            	Toast toast = Toast.makeText(this, "Something else Happened", Toast.LENGTH_SHORT);
            	toast.show();
            }
    	
    	}
    }
	
	public void startOCR(){
		
		long startTime = System.currentTimeMillis();
		
		//We made an OCR and we check if we make a Re-OCR!
		if(!MyProducts.getProducts().isEmpty()){
			if(reOcrButtonPressed){
				reOcrButtonPressed = false;
			}else{
				//copy the products to oldProducts
				MyProducts.addToOldProducts(MyProducts.getProducts());
			}
			MyProducts.getProducts().clear();
		}
		
		//If oldProducts ArrayList isn't empty show the products in the editText
		if(!MyProducts.getOldProducts().isEmpty()){
			String oldProducts = "Old Products:\n\n";
			for(int i=0; i<MyProducts.getOldProducts().size(); i++){
				oldProducts = oldProducts + MyProducts.getOldProducts().get(i).getName() + "  "
										  + MyProducts.getOldProducts().get(i).getPrice() + "\n";
			}
			oldProducts = oldProducts + "\n";
			ocrResult.setText(oldProducts);
		}
		
		//Process the images for better accuracy.
		ImgProcessing imgProcess = new ImgProcessing();
		imgProcess.start();
		
		retrievePreferences();
		Log.v(TAG, "photoCounter:" + photoCounter);
		aTask = new OcrAsyncTask(this, dialog, languageCode, languagePath, ocrResult, photoCounter, startTime);
   	    aTask.execute();
   	    reOcrButton.setEnabled(true);
	
	}
	
    public void setLanguageCode(String languageCodeFromPref){
    	this.languageCode = languageCodeFromPref;
    }
    
    /**
     * Gets values from shared preferences and sets the corresponding data members in this activity.
     */
    private void retrievePreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Retrieve from preferences, and set in this Activity, the language preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setLanguageCode(prefs.getString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, DEFAULT_SOURCE_LANGUAGE_CODE));
        
        prefs.registerOnSharedPreferenceChangeListener(listener);
        
    }
    
    /**
     * Sets default values for preferences. To be called the first time this app is run.
     */
    private void setDefaultPreferences() {
      prefs = PreferenceManager.getDefaultSharedPreferences(this);

      // Recognition language
      prefs.edit().putString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, DEFAULT_SOURCE_LANGUAGE_CODE).commit();

    }
    
    /**
     * We want the help screen to be shown automatically the first time a new version of the app is
     * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
     * it to a value stored as a preference.
     */
    private boolean checkFirstLaunch() {
      try {
        PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
        int currentVersion = info.versionCode;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int lastVersion = prefs.getInt(KEY_HELP_VERSION_SHOWN, 0);
        if (lastVersion == 0) {
          isFirstLaunch = true;
        } else {
          isFirstLaunch = false;
        }
        if (currentVersion > lastVersion) {
          
          // Record the last version for which we last displayed the What's New (Help) page
          prefs.edit().putInt(KEY_HELP_VERSION_SHOWN, currentVersion).commit();
          Intent helpActivIntent = new Intent(this, HelpActivity.class);
          helpActivIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
          
          // Show the default page on a clean install, and the what's new page on an upgrade.
          String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
          helpActivIntent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
          startActivityForResult(helpActivIntent, HELP_ACTIVITY_REQUEST_CODE);
          return true;
        }
      } catch (PackageManager.NameNotFoundException e) {
        Log.w(TAG, e);
      }
      return false;
    }

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	

	public Map<String, String> getParallelMap() {
		return parallelMap;
	}

	public void setParallelMap(Map<String, String> parallelMap) {
		this.parallelMap = parallelMap;
	}

	public EditText getOcrResult() {
		return ocrResult;
	}

	public void setOcrResult(String s) {
		this.ocrResult.setText(s);
	}
    
}





