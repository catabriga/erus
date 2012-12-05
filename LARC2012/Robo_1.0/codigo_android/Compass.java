package erus.android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
	
public class Compass implements SensorEventListener
{
	private float accX;
	private float accY;
	private float accZ;

	private Sensor compass;

	Compass(SensorManager sm)
	{
		compass = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	public void regListener(SensorManager sm)
	{
		sm.registerListener( this, compass, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	public void onSensorChanged(SensorEvent event) 
	{
		accX = event.values[0];
		accY = event.values[1];
		accZ = event.values[2];
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{

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

