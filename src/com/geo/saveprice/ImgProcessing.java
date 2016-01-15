package com.geo.saveprice;

import java.io.FileOutputStream;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

/**
 * 
 * This class makes a pre-processing of the images taken before sending them for OCR.
 * More specifically converts an image to gray-scale and applies a threshold with OTSU algorithm.
 *
 */

public class ImgProcessing {
	
	private MainActivity myActivity;
	private Bitmap bitmap;
	private String imagePath = Environment.getExternalStorageDirectory() + "/MyApp/Pictures";
	
	@SuppressWarnings("static-access")
	public void start(){
		for(int i=0; i<=myActivity.getPhotoCounter(); i++){
			
			bitmap = getBitmap(i);
			
			//Create mat image
			Mat img0 = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
			Utils.bitmapToMat(bitmap, img0);
			
			Imgproc.cvtColor(img0, img0, Imgproc.COLOR_RGB2GRAY);
		    
			Imgproc.threshold(img0, img0, 100, 255, Imgproc.THRESH_OTSU); //127
			Utils.matToBitmap(img0, bitmap);
			try {
		        FileOutputStream out = new FileOutputStream(imagePath + "/ocr" + i + "T.jpg");
		        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		  	} catch (Exception e) {
		        e.printStackTrace();
		  	}
			
			
		}
		
	}
	
	public Bitmap getBitmap(int picNum){
		bitmap = BitmapFactory.decodeFile(imagePath + "/ocr" + picNum + ".jpg");
		return bitmap;
	}


}
