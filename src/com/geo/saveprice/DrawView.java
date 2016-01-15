package com.geo.saveprice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceView;

/**
 * 
 * This class draws the 8-green rectangles in CameraPreview.
 *
 */

public class DrawView extends SurfaceView{

	public static final String TAG = "DrawView";
	
    private Paint textPaint = new Paint();
    private Context context;
    
	public DrawView(Context context) {
		super(context);
		this.context = context;
		// Create out paint to use for drawing
        textPaint.setARGB(255, 0, 255, 0);
        textPaint.setStrokeWidth(5);
        /* This call is necessary, or else the 
         * draw method will not be called. 
         */
        setWillNotDraw(false);
	}
	
	@Override
    protected void onDraw(Canvas canvas){
		
        textPaint.setAlpha(100);
        /*
        //Top-3 Rectangles
        canvas.drawRect(0, 0, 199, 199, textPaint);
        canvas.drawRect(201, 0, 879, 199, textPaint);
        canvas.drawRect(881, 0, 1080, 199, textPaint);
        
        //Right Rectangle
        canvas.drawRect(881, 201, 1080, 1699, textPaint);
        
        //Bottom-3 Rectangles
        canvas.drawRect(881, 1701, 1080, 1920, textPaint);
        canvas.drawRect(201, 1701, 879, 1920, textPaint);
        canvas.drawRect(0, 1701, 199, 1920, textPaint);
        
        //Left Rectangle
        canvas.drawRect(0, 201, 199, 1699, textPaint);
        */
        
        //Everytime compute numbers in order to adjust the green rectangles
        //in different screens. The factors are always the same.
        
        int previewWidth =  ((CameraActivity) context).getmPreviewWidth(); //1080
        int previewHeight = ((CameraActivity) context).getmPreviewHeight(); //1920
        
        //199-->(199/1080=0.1842)
        int num1 = (int) Math.round(0.1842 * previewWidth);
        //201
        int num2 = (int) Math.round(0.1861 * previewWidth);
        //879
        int num3 = (int) Math.round(0.8138 * previewWidth);
        //881
        int num4 = (int) Math.round(0.8157 * previewWidth);
        //1699
        int num5 = (int) Math.round(0.8848 * previewHeight);
        //1701
        int num6 = (int) Math.round(0.8859 * previewHeight);
        
        Log.v(TAG, "pw:" + previewWidth + "ph:" + previewHeight + " 199=" + num1 + " 201=" + num2 + " 879=" + num3 + " 881=" + num4 + " 1699=" + num5 + " 1701=" + num6);
        
        //Top-3 Rectangles
        canvas.drawRect(0, 0, num1, num1, textPaint);
        canvas.drawRect(num2, 0, num3, num1, textPaint);
        canvas.drawRect(num4, 0, previewWidth, num1, textPaint);
        
        //Right Rectangle
        canvas.drawRect(num4, num2, previewWidth, num5, textPaint);
        
        //Bottom-3 Rectangles
        canvas.drawRect(num4, num6, previewWidth, previewHeight, textPaint);
        canvas.drawRect(num2, num6, num3, previewHeight, textPaint);
        canvas.drawRect(0, num6, num1, previewHeight, textPaint);
        
        //Left Rectangle
        canvas.drawRect(0, num2, num1, num5, textPaint);
        
	}
}
