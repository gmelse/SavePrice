/* 
 * Copyright 2014 George Melidis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geo.saveprice;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

/**
 * 
 * This class takes a bitmap file and makes OCR.
 *  
 */

 class OcrAsyncTask extends AsyncTask<String, String, Boolean> {
	
  private static final String TAG = "OcrAsyncTask";
  
  private Context myContext;
  private ProgressDialog dialog;
  private String languageCode, languagePath;
  private String recognizedText,finalText ="";
  private Bitmap bitmap;
  private EditText ocrResult;
  private int photoCounter;
  private int percent;
  private long startTime;

  /**
   * AsyncTask to asynchronously start OCR.
   * 
   * @param dialog
   *          Dialog box with thermometer progress indicator
   * @param languageCode
   *          ISO 639-2 OCR language code
   * @param languagePath
   *          Path of the OCR language data files
   * @param ocrResult
   * 		  A reference to the editText where the result will be shown
   * @param photoCounter 
   * 		  The number of photos that have been taken
   * 
   */
  public OcrAsyncTask(Context myContext, ProgressDialog dialog, String languageCode, String languagePath, EditText ocrResult, int photoCounter, long startTime) {
    this.myContext = myContext;
	this.dialog = dialog;
    this.languageCode = languageCode;
    this.languagePath = languagePath;
    this.ocrResult = ocrResult;
    this.photoCounter = photoCounter;
    this.startTime = startTime;
    
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    dialog.setTitle("Please wait");
    dialog.setMessage("Ocr Process...");
    dialog.setIndeterminate(false);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.setCancelable(false);
    dialog.show();
  }

  /**
   * In background thread, perform required setup.
   * 
   * @param params
   *          [0] Pathname for the directory for storing language data files to the SD card
   */
  protected Boolean doInBackground(String... params) {
	  
	  recognizedText = "";
	  
	  int divisor = (photoCounter+1)*4; //divisor for percent
	  
	  if (photoCounter == 0){
		  percent = 25; 
	  }else{
		  percent = (int) 100/divisor; //compute the percentage increase
	  }
	  
	  publishProgress("Processing Data...", "0");
	  
	  int m = 1; //m is a multiplier for percent
	  
	  for(int i=0; i<=photoCounter; i++){
		  
		  Log.v(TAG, "i:" + i);
		  
		  publishProgress("Processing Data...", Integer.toString(percent*m));
		  Log.v(TAG, "percent:" + percent*m);
		  
		  bitmap = getBitmap(i);
		  
		  TessBaseAPI baseApi = new TessBaseAPI();
		  
		  publishProgress("Processing Data...", Integer.toString(percent*(m+1)));
		  Log.v(TAG, "percent:" + percent*(m+1));
		  
		  if (languageCode.equals("dig")){
		  		baseApi.init(languagePath, "eng");
		  		baseApi.setVariable("tessedit_char_whitelist", "0123456789,.%");
		  		Log.v(TAG, "DIGITS");
		  }else {
		  		Log.v(TAG, languagePath + " and " + languageCode);
		  		baseApi.init(languagePath, languageCode);
		  }
		  
		  publishProgress("Processing Data...", Integer.toString(percent*(m+2)));
		  Log.v(TAG, "percent:" + percent*(m+2));
		  
		  baseApi.setImage(bitmap);
		  recognizedText = recognizedText + baseApi.getUTF8Text();
		  
		  publishProgress("Processing Data...", Integer.toString(percent*(m+3)));
		  Log.v(TAG, "percent:" + percent*(m+3));

		  baseApi.end();
		  bitmap.recycle();
		  
		  m = m+4;
		   
	  }
	  Log.v(TAG, "THE RESULT IS:/n" + recognizedText);
	  
	  RecognizeProducts recognition = new RecognizeProducts(recognizedText, myContext);
	  finalText = recognition.recognize();
	  
	  long endTime   = System.currentTimeMillis();
	  long totalTime = endTime - startTime;
	  
	  Log.v(TAG, "THE TIME IN MS IS: " + totalTime);
	  Log.v(TAG, "THE FINAL RIGHT TEXT IS: " + finalText);
	  
	  publishProgress("Finishing OCR Procedure...", "100");
	  
	  try {
	      dialog.dismiss();
	  } catch (IllegalArgumentException e) {
	      // Catch "View not attached to window manager" error, and continue
	  }
	   
	    return false;
  }
  
  

/**
   * Update the dialog box with the latest incremental progress.
   * 
   * @param message
   *          [0] Text to be displayed
   * @param message
   *          [1] Numeric value for the progress
   */
  @Override
  protected void onProgressUpdate(String... message) {
    super.onProgressUpdate(message);
    int percentComplete = 0;

    percentComplete = Integer.parseInt(message[1]);
    dialog.setMessage(message[0]);
    dialog.setProgress(percentComplete);
    dialog.show();
  }

  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    //ocrResult.setText(finalText);
    if(!finalText.equals("")){
	    ocrResult.append("New Products:\n\n");
	    ocrResult.append(finalText);
    }
  }
  
  public Bitmap getBitmap(int picNum){
		bitmap = BitmapFactory.decodeFile(languagePath + "Pictures/ocr" + picNum + "T.jpg");
		//bitmap = BitmapFactory.decodeFile(languagePath + "Pictures/ocr" + picNum + ".jpg");
		//bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		//May need a conversion to ARGB_8888
		return bitmap;
	}
  
}