package erus.android;


import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.View;

public class ErusView extends View
{
	private static final String TAG = "CameraProcessor";
	
//	private boolean connectedArduino;
//	private boolean connectedPC;
	private boolean connectedAndroid;
	private String teste;
	
//	private double xC, yC, zC;//Compass
//	private double xA, yA, zA;//Accelerometer
//	private int encRight, encLeft;
//	private int us1, us2, us3, us4, us5, us6;
	
	private long lastUpdate;
	
	private byte color[];
	
	public ErusView(Activity AppContext)
	{
		super(AppContext);
		
		setFocusable(true);
		
//		connectedArduino = false;
//		connectedPC = false;
		connectedAndroid = false;
		teste = "false";
		
//		xC = yC = zC = 0.0;
//		xA = yA = zA = 0.0;
		
		color = new byte[49];
	}
	
	public void setTeste(String teste)
	{
		this.teste = teste;
		this.postInvalidate();
	}
	
	public void setAndroidConnected(boolean connected)
	{
		connectedAndroid = connected;
		this.postInvalidate();
	}
	
/*	public void setPCConnected(boolean connected)
	{
		connectedPC = connected;		
		this.postInvalidate();
	}*/
	
/*	public void setArduinoConnected(boolean connected)
	{
		connectedArduino = connected;		
		this.postInvalidate();
	}
	
	public void setAccelerometer(float x, float y, float z)
	{
		xA = x;
		yA = y;
		zA = z;
		
		if( System.currentTimeMillis() - lastUpdate > 100)
		{
			this.postInvalidate();
		}
	}
	
	public void setCompass(float x, float y, float z)
	{
		xC = x;
		yC = y;
		zC = z;
		
		if( System.currentTimeMillis() - lastUpdate > 100)
		{
			this.postInvalidate();
		}
	}

	public void setUltraSound(int data1, int data2, int data3, int data4, int data5, int data6)
	{
		us1 = data1;
		us2 = data2;
		us3 = data3;
		us4 = data4;
		us5 = data5;
		us6 = data6;
		//if( System.currentTimeMillis() - lastUpdate > 100)
		//{
			this.postInvalidate();
		//}
	}	
	
	public void setEncoder(int right, int left)
	{
		encRight = right;
		encLeft = left;
		
		if( System.currentTimeMillis() - lastUpdate > 100)
		{
			this.postInvalidate();
		}
	}	*/
	
	public void setColor(byte[] colorCal)
	{
		for(int i=0; i < 49; i++)
		{
			color[i] = colorCal[i];
		}
		this.postInvalidate();
	}
	
	@SuppressWarnings("unused")
	private void drawSensors(Canvas canvas)
	{
		Paint redPaint = new Paint();
		redPaint.setColor(Color.RED);
		redPaint.setStyle(Style.FILL);
		redPaint.setTextSize(30);
			
/*		canvas.drawText("Accelerometer:", 4, 200, redPaint);
		//canvas.drawText("Teste",4,200,redPaint);
		canvas.drawText("x: "+xA, 4, 250, redPaint);
		canvas.drawText("y: "+yA, 4, 300, redPaint);
		canvas.drawText("z: "+zA, 4, 350, redPaint);
		
		canvas.drawText("Compass:", 4, 400, redPaint);
		canvas.drawText("x: "+xC, 4, 450, redPaint);
		canvas.drawText("y: "+yC, 4, 500, redPaint);
		canvas.drawText("z: "+zC, 4, 550, redPaint);
	*/	
	}
	
	@Override
	public void onDraw(Canvas canvas) 
	{
		Paint borderPaint = new Paint();
		
		borderPaint.setColor(Color.WHITE);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.FILL);

		Paint redPaint = new Paint();
		redPaint.setColor(Color.RED);
		redPaint.setStyle(Style.FILL);
		redPaint.setTextSize(30);
		
		Paint greenPaint = new Paint();
		greenPaint.setColor(Color.GREEN);
		greenPaint.setStyle(Style.FILL);
		greenPaint.setTextSize(30);
		
		canvas.drawRect(this.getLeft(), this.getTop(), this.getRight(), this.getBottom(), borderPaint);
		
		canvas.drawText("Erusbot", 4, 50, greenPaint);
		/*
		if(connectedArduino)
			canvas.drawText("Arduino Connected", 4, 100, greenPaint);
		else
			canvas.drawText("Arduino NOT Connected", 4, 100, redPaint);
		
		canvas.drawText(teste, 4, 125, greenPaint);
		
		if(connectedPC)
			canvas.drawText("PC Connected", 4, 150, greenPaint);
		else
			canvas.drawText("PC NOT Connected", 4, 150, redPaint);
	*/	
		if(connectedAndroid)
			canvas.drawText("Android Connected", 4, 100, greenPaint);
		else
			canvas.drawText("Android NOT Connected", 4, 100, redPaint);
		
/*		canvas.drawText("Encoder: "+encRight+ " " + encLeft, 4, 200, redPaint);
		
		canvas.drawText("Ultrasound: "+us1+ " " + us2+ " " + us3+ " " + us4+ " " + us5+ " " + us6, 4, 250, redPaint);
	*/	

		Paint paintColor = new Paint();
		paintColor.setStyle(Style.FILL);
		paintColor.setTextSize(30);
		
/*		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.put(color, 9, 4);
		bb.rewind();*/

		paintColor.setStrokeWidth(0);
		
		canvas.drawText("Cor 1:"/*+"("+(color[1]&0xFF)+"|"+(color[2]&0xFF)+"|"+(color[3]&0xFF)+"|"+(color[4]&0xFF)+") ("+(color[5]&0xFF)+"|"+(color[6]&0xFF)+"|"+(color[7]&0xFF)+"|"+(color[8]&0xFF)+") ("+bb.getFloat()+")"*/, 4, 300, redPaint);
		paintColor.setARGB(255 , color[1]&0xFF, color[2]&0xFF, color[3]&0xFF);
		canvas.drawRect(100, 280, 120, 300, paintColor);
		
/*		bb.rewind();
		bb.put(color, 21, 4);
		bb.rewind();*/
		canvas.drawText("Cor 2:"/*+"("+(color[13]&0xFF)+"|"+(color[14]&0xFF)+"|"+(color[15]&0xFF)+"|"+(color[16]&0xFF)+") ("+(color[17]&0xFF)+"|"+(color[18]&0xFF)+"|"+(color[19]&0xFF)+"|"+(color[20]&0xFF)+") ("+bb.getFloat()+")"*/, 4, 340, redPaint);
		paintColor.setARGB(255, color[13]&0xFF, color[14]&0xFF, color[15]&0xFF);	
		canvas.drawRect(100, 320, 120, 340, paintColor);
		
/*		bb.rewind();
		bb.put(color, 33, 4);
		bb.rewind();*/
		canvas.drawText("Cor 3:"/*+"("+(color[25]&0xFF)+"|"+(color[26]&0xFF)+"|"+(color[27]&0xFF)+"|"+(color[28]&0xFF)+") ("+(color[29]&0xFF)+"|"+(color[30]&0xFF)+"|"+(color[31]&0xFF)+"|"+(color[32]&0xFF)+") ("+bb.getFloat()+")"*/, 4, 380, redPaint);
		paintColor.setARGB(255, color[25]&0xFF, color[26]&0xFF, color[27]&0xFF);
		canvas.drawRect(100, 360, 120, 380, paintColor);
		
/*		bb.rewind();
		bb.put(color, 45, 4);
		bb.rewind();*/
		canvas.drawText("Cor 4:"/*+"("+(color[37]&0xFF)+"|"+(color[38]&0xFF)+"|"+(color[39]&0xFF)+"|"+(color[40]&0xFF)+") ("+(color[41]&0xFF)+"|"+(color[42]&0xFF)+"|"+(color[43]&0xFF)+"|"+(color[44]&0xFF)+") ("+bb.getFloat()+")"*/, 4, 420, redPaint);
		paintColor.setARGB(255, color[37]&0xFF, color[38]&0xFF, color[39]&0xFF);			
		canvas.drawRect(100, 400, 120, 420, paintColor);
		
/*		redPaint.setARGB(0,0,200,0);
		redPaint.setStrokeWidth(0);
		canvas.drawRect(20, 250, 36, 300, redPaint);*/
		
		lastUpdate = System.currentTimeMillis();
		
		Log.i(TAG, "onDraw");
	}

}
