package com.geo.saveprice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class SendToServerAsync extends AsyncTask<String, String, Boolean> {
	
	private static final String TAG = "ServerAsync";
	private final int CLASS_CODE = 1;
	
	private Context context;
	private ProgressDialog dialog;
	private int initialProductsSize;
	private EditText ocrResult;
	private int sCounter, nsCounter;
	
    public SendToServerAsync(Context context, ProgressDialog dialog, EditText ocrResult) {
    	this.context = context;
    	this.dialog = dialog;
    	this.ocrResult = ocrResult;
    }
    
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      dialog.setTitle("Please wait");
      dialog.setMessage("Sending To Server...");
      dialog.setIndeterminate(false);
      dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      dialog.setCancelable(false);
      dialog.show();
    }
    
	protected Boolean doInBackground(String... params) {
		
		HttpClient httpclient = new DefaultHttpClient();
		//HttpPost httppost = new HttpPost("http://georgemelidis.no-ip.biz/androidserver/saveToDB.php");
		HttpPost httppost = new HttpPost("http://192.168.43.96/androidserver/saveToDB.php");
		
		int[] dataSended = new int[MyProducts.getProducts().size()];
		
		initialProductsSize = MyProducts.getProducts().size();
		
		sCounter = 0;
		nsCounter = 0;
		
		for(int i=0; i<MyProducts.getProducts().size(); i++){
			
			String realName = ((MainActivity) context).getParallelMap().get(MyProducts.getProducts().get(i).getName());
			Log.v(TAG, "Send to server realName:" + realName);
			if(realName == null){
				nsCounter++;
			}else{
				try {
					// Add your data
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
					
					Log.v(TAG, "pname we send:" + realName + "pprice:" + MyProducts.getProducts().get(i).getPrice());
					
					byte[] data = realName.getBytes("UTF-8");
	    			String base64 = Base64.encodeBytes(data);
					
					nameValuePairs.add(new BasicNameValuePair("productName", base64 )); //"hello + ΓΕΙΑ ΣΟΥ"
					nameValuePairs.add(new BasicNameValuePair("price", MyProducts.getProducts().get(i).getPrice())); //"1.92"
					nameValuePairs.add(new BasicNameValuePair("latitude", params[0]));
					nameValuePairs.add(new BasicNameValuePair("longitude", params[1]));
					
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
					try {
						httpclient.execute(httppost);
						dataSended[sCounter] = i;
						sCounter++;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						nsCounter++;
					}
					
				} catch (IOException e) {
					Log.i("HTTP Failed", e.toString());
					nsCounter++;
				}    	
			}
				
		}
		
		//Remove Items from products that have been sent to dataBase
		for(int i=0; i<sCounter; i++){
			int index = dataSended[i];
			MyProducts.getProducts().remove(index);
			//Update dataSended to point to the right object's after removing one object
			//Because the index of the next products is decreased by one(-1)
			for(int j=i+1; j<sCounter; j++){
				dataSended[j] = dataSended[j]-1;
			}
		}
		//After this products ArrayList contains all the products that haven't been sent or
		//no products if all have been sent.
		
		return false;
	}

	@SuppressLint("ShowToast")
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		dialog.dismiss();
		
		//Check if all Data have been sent
		if (sCounter == initialProductsSize){ //All products have been sent //For example: 5 = 5 or 0 = 0
			if(sCounter == 0){ //it means that there aren't any data in Arraylist of products
				Toast toast = Toast.makeText(context, "No-Data to Send!", 1000);
				toast.show();
			}else{
				Toast toast = Toast.makeText(context, "Result Saved", 1000);
				toast.show();
				ocrResult.setText("");
			}
		}else if ((nsCounter == initialProductsSize)||(nsCounter>=1)){ //No products send || some products not send
			String message = "";
			if(nsCounter == initialProductsSize){
				message = "Can't save all "+ nsCounter + " products! Possible reasons: "
						+ "\n 1.Wrong Name \n 2.Connection Error "
						+ "\n 3.Server Error \n 4.Database Error ";
			}else if (nsCounter>=1){
				if(nsCounter == 1){
					message = "Can't save "+ nsCounter + " product! Possible reasons: "
							+ "\n 1.Wrong Name \n 2.Connection Error "
							+ "\n 3.Server Error \n 4.Database Error ";
				}else{
					message = "Can't save "+ nsCounter + " products! Possible reasons: "
							+ "\n 1.Wrong Name \n 2.Connection Error "
							+ "\n 3.Server Error \n 4.Database Error ";
				}
			}
			
			//Data that haven't sent are still stored in ArrayList products
			//If you hit Cancel in the alertDialog they will be stored in oldProducts
			//and they will be shown to the user in the EditText.
			alert(message);
			
		}
		
			
	}
	
	public void alert(String message){
		String title = "Warning";
		MyAlertDialog mad = new MyAlertDialog(context, message, title, this.CLASS_CODE);
		mad.showAlertDialog();
	}
   	
}    


