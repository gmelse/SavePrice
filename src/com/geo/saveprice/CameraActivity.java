package com.geo.saveprice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import com.geo.saveprice.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * 
 * This activity opens the camera, draws the green bounds and it's operation is to take
 * a picture of a receipt and store it in the internal memory for processing. 
 * 
 */

public class CameraActivity extends Activity{
	
	public static final int MEDIA_TYPE_IMAGE = 1;

	private static final String TAG = "CameraActivity";
	
	private static String imagePath = Environment.getExternalStorageDirectory() + "/MyApp/Pictures";

    private Camera mCamera;
    private CameraPreview mPreview;
    private DrawView dView;
    private FrameLayout mlayout;
    private LayoutInflater controlInflater;
    private View viewControl;
    private Button captureButton;
    private static File mediaStorageDir;
    private Bitmap bitmap;
    private static int pictureNum;
    
    @SuppressLint("InflateParams")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        					 WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.camera_layout);
	
        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.getParameters();
        
        /* Create our layout in order to layer the 
    	 * draw view on top of the camera preview. 
    	 */
        mlayout = (FrameLayout) findViewById(R.id.camera_preview);
        
        // Create a new camera view and add it to the layout
        mPreview = new CameraPreview(this, mCamera);
        mlayout.addView(mPreview);
        
        // Create a new draw view and add it to the layout
        dView = new DrawView(this);
        mlayout.addView(dView);
        
        // Create a LayoutInflater in order to be able to add the button and the button to be visible
        controlInflater =  LayoutInflater.from(getBaseContext());
		viewControl = controlInflater.inflate(R.layout.button_capture, null);
        mlayout.addView(viewControl);
        
        captureButton = (Button) findViewById(R.id.button_capture);
        
        captureButton.setOnClickListener(new OnClickListener(){
    		public void onClick(View v) {
		    		mCamera.takePicture(null, null, mPicture);
    		}
		    		
		});
        
    	captureButton.setOnLongClickListener(new OnLongClickListener(){
	    		@Override
	    		public boolean onLongClick(View arg0) {
		    		mCamera.autoFocus(new AutoFocusCallback(){
			    		@Override
			    		public void onAutoFocus(boolean arg0, Camera arg1) {
			    			
			    		}
		    		});
	    		return true;
	    		}
    		});

  	    pictureNum = MainActivity.getPhotoCounter();
        
    }
    
  	/** A safe way to get an instance of the Camera object. */
  	public static Camera getCameraInstance(){
  	    Camera c = null;
  	    try {
  	        c = Camera.open(); // attempt to get a Camera instance
  	    }
  	    catch (Exception e){
  	        // Camera is not available (in use or does not exist)
  	    }
  	    return c; // returns null if camera is unavailable
  	}
  	
  	/** Release the camera. */
  	private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }
  	
  	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
		    Log.d(this.getClass().getName(), "back button pressed");
		    setResult(CameraActivity.RESULT_CANCELED);
		    mCamera.stopPreview();
	   		releaseCamera();
	   		finish();
		}
		return super.onKeyDown(keyCode, event);
	}

  	/** Create a File for saving an image or video */
  	private static File getOutputMediaFile(int type){

  	    mediaStorageDir = new File(imagePath);

  	    // Create the storage directory if it does not exist
  	    if (! mediaStorageDir.exists()){
  	        if (! mediaStorageDir.mkdirs()){
  	            Log.d(TAG, "failed to create directory");
  	            return null;
  	        }
  	    }

  	    // Create a media file name
  	    File mediaFile;
  	    if (type == MEDIA_TYPE_IMAGE){
  	        mediaFile = new File(imagePath+ "/ocr" + pictureNum + ".jpg");
  	    } else {
  	        return null;
  	    }

  	    return mediaFile;
  	}

  	//Callback interface to take a picture
  	PictureCallback mPicture = new PictureCallback() {
		
	    @SuppressWarnings("null")
		public void onPictureTaken(byte[] data, Camera camera) {
	    	
	    	//Create the file where the picture will be saved
	        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	       
			if (pictureFile == null){
				Throwable e = null;
				Log.d(TAG, "Error creating media file, check storage permissions: " + e.getMessage());
	            return;
	        }

	        try {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            fos.write(data);
	            fos.close();
	            
	            Handler handler = new Handler(); 
    	        handler.postDelayed(new Runnable() { 
    	             public void run() { 
    	            	 bitmap = BitmapFactory.decodeFile(imagePath+ "/ocr" + pictureNum + ".jpg");

              				// Getting width & height of the given image.
              				int w = bitmap.getWidth();
              				int h = bitmap.getHeight();
              				Log.v(TAG, "wImage:" + w + " hImage:" + h + "PreviewWidth:" + mPreview.getWidth() + "PreviewHeight:" + mPreview.getHeight());
              				
              				//After rotation 90 width-->height, height-->width but now the rotation haven't applied yet
              				double bitmapWidth = bitmap.getHeight(); //3096
              				double bitmapHeight = bitmap.getWidth(); //4128
              				double previewWidth = mPreview.getWidth(); //1080
              				double previewHeight = mPreview.getHeight(); //1920
              				
              				Log.v(TAG, "" + bitmapWidth +" "+ bitmapHeight + " "+ previewWidth +" "+ previewHeight);
              				
              				double tempX = bitmapWidth / previewWidth;
              				double tempY = bitmapHeight / previewHeight;
              				
              				Log.v(TAG, "" + tempX +" "+ tempY);
              				
              				BigDecimal bd1 = new BigDecimal(tempX);
              				BigDecimal bd2 = new BigDecimal(tempY);
              				
              			    bd1 = bd1.setScale(2, BigDecimal.ROUND_HALF_UP);
            			    bd2 = bd2.setScale(2, BigDecimal.ROUND_HALF_UP);
              			    
              			    double scaleX = Double.parseDouble(bd1.toString());
              			    double scaleY = Double.parseDouble(bd2.toString());
              			    
              				Log.v(TAG, "" + scaleX +" "+ scaleY);
              				
              				// Setting pre rotate
              				Matrix mtx = new Matrix();
              				mtx.preRotate(90);
              				
              				/**mPreview Size is 1920x1080
              				 * picture saved in storage is 4128x3096
              				 * so we want a scale multiplier for the top-left 
              				 * and bottom-right corners coordinates of the rectangle 
              				 * where the receipt is. The coordinates in the case are
              				 * TL(200, 200) and BR(880, 1700)
              				 * So 4128/1920 = 2.15
              				 * 	  3096/1080 = 2.86
              				 * So the default numbers for Cropping the region of receipt
              				 * are declared below 
              				 */
              				/*
              				int x = 430; //(int) (200*2.86);
              				int y = 572; //(int) (200*2.15);
              				int width = 3655 - x; //(int) (880*2.86);
              				int height = 2517 - y; //(int) (1700*2.15);
              				*/
              				
              				//Compute the tl and br corners
              				//tl.x = tl.y = 200 in a 1080x1920 analysis
              				//The numbers 0.1851, 0.8148, 0.8854 are percents of screen analysis 
              				//which correspond to 200, 880, 1700 in a 1080*1920 analysis
              				//to compute the right points of the rectangle
              				int tl = (int) Math.round(0.1851*previewWidth);
              				int brx = (int) Math.round(0.8148*previewWidth);
              				int bry = (int) Math.round(0.8854*previewHeight);
              				
              				Log.v(TAG, "tl:" + tl +" brx:"+ brx + " bry:"+ bry);
              				
              				int x = (int) (tl*scaleY); //(int) (200*2.15);
              				int y = (int) (tl*scaleX); //(int) (200*2.86);
              				int width = (int) (bry*scaleY - x); //(int) (1700*2.15 - x);
              				int height = (int) (brx*scaleX - y); //(int) (880*2.86 - y);
              				
              				Log.v(TAG, x +" "+ y + " "+ width +" "+ height);
              				
              				// Take picture inside the bounds
              				bitmap = Bitmap.createBitmap(bitmap, x, y, width, height, mtx, false);
              				
		              		try {
		              	       FileOutputStream out = new FileOutputStream(imagePath+ "/ocr" + pictureNum + ".jpg");
		              	       bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		 	             	} catch (Exception e) {
		 	             	       e.printStackTrace();
		 	             	}
		              		
		              		setResult(CameraActivity.RESULT_OK);
		              		mCamera.stopPreview();
		              		releaseCamera();
		              		finish();
    	             } 
    	        }, 5000);
	        } catch (FileNotFoundException e) {
	            Log.d(TAG, "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        }
	    }
	};

	public int getmPreviewWidth() {
		return mPreview.getWidth();
	}
	
	public int getmPreviewHeight() {
		return mPreview.getHeight();
	}
	
}