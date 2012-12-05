package erus.android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Accelerometer implements SensorEventListener
{
	private float accX;
	private float accY;
	private float accZ;
	
	private Sensor accelerometer;
	
	Accelerometer(SensorManager sm)
	{		
		accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
	}
	
	public void regListener(SensorManager sm)
	{	
		sm.registerListener( this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
	
	}

	public void onSensorChanged(SensorEvent event) 
	{
		accX = event.values[0];
		accY = event.values[1];
		accZ = event.values[2];	
	}

	public float getX()
	{
		return this.accX;
	}
	
	public float getY()
	{
		return this.accY;
	}
	
	public float getZ()
	{
		return this.accZ;
	}
}
