package erus.android.erusbot;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Point;

import android.util.Log;

public class RobotBrain
{
	private static final String TAG = "RobotBrain";
	
	private static final int LIMIT_MOTOR_MOVEMENT = 255;
	
	public static final int ROBOT_CENTER_OFFSET = 0;
	public static final int CATCHABLE_CAN_LIMIT_Y_MIN = 10;
	public static final int CATCHABLE_CAN_LIMIT_Y_MAX = 30;
	public static final int CATCHABLE_CAN_LIMIT_X = 40;
	
	
	// States
	private static final int NO_STATE = -1;
	private static final int WAIT_START = 0;
	private static final int STOP = 1002;
	
	private static final int FORWARD = 2001;
	private static final int GO_TO_CAN = 2002;
	
	private int state;
	private int lastState;
	
	private long time;
	
	private int motorLeft;
	private int motorRight;
	private int motorVassoura;
	private int motorDoor;
	private int buzzer;
	
	private Connection arduinoConnection;
	private Connection pcConnection;
	
	private ErusView erusView;
	private Trash trash = new Trash();
		
	public RobotBrain(Connection arduinoConnection, Connection pcConnection, ErusView erusView)
	{
		state = FORWARD;
		lastState = NO_STATE;
		
		time = System.currentTimeMillis();
		
		motorLeft = -1;
		motorRight = -1;
		motorVassoura = -1;
		motorDoor = -1;
		buzzer = -1;
		
		trash.position = new Point();
		
		this.arduinoConnection = arduinoConnection;
		this.pcConnection = pcConnection;
		
		this.erusView = erusView;
	}

	public void setTrashPosition(Trash trash)
	{
		this.trash = trash; 
	}
	
	public void setArduinoConnection(Connection arduinoConnection)
	{
		this.arduinoConnection = arduinoConnection;
	}
	
	private void pcPrint(String str) throws IOException
	{
		if(pcConnection != null)
		{
			byte[] byteStr = str.getBytes();
			byte[] head = new byte[2];
			head[0] = 0x65;
			head[1] = (byte) byteStr.length;
						
			pcConnection.sendMessage(head, 0, 2);
			pcConnection.sendMessage(byteStr, 0, byteStr.length);
		}
	}
	
	private int checkPowerLimits(int power)
	{
		if(power > 100)
		{
			power = 100;
		}
		if(power < -100)
		{
			power = -100;
		}
		
		return power;
	}
	
	private int convert100To255(int power, int limit)
	{
		int finalPower = (power * limit) / 100;
		
		if(finalPower > limit)
			finalPower = limit;
		
		if(finalPower < 0)
			finalPower = 0;
		
		return finalPower;
	}
		
	private void setMotorsMovement(int leftMotor, int rightMotor) throws IOException
	{	
		leftMotor = -leftMotor;
		rightMotor = -rightMotor;
		
		leftMotor = checkPowerLimits(leftMotor);
		rightMotor = checkPowerLimits(rightMotor);
		
		if(motorLeft == leftMotor && motorRight == rightMotor)
		{
			return;
		}		
		
		this.motorLeft = leftMotor;
		this.motorRight = rightMotor;

		byte leftDirection = 0;
		if(leftMotor < 0)
		{
			leftMotor = -leftMotor;
			leftDirection = 1;
		}
		
		byte rightDirection = 0;
		if(rightMotor < 0)
		{
			rightMotor = -rightMotor;
			rightDirection  = 1;
		}
		
		int left255 = convert100To255(leftMotor, LIMIT_MOTOR_MOVEMENT);
		int right255 = convert100To255(rightMotor, LIMIT_MOTOR_MOVEMENT);
					
		byte motorData[] = {0x12, (byte)left255, leftDirection, 0x11, (byte)right255, rightDirection};
		
		if(arduinoConnection != null)
		{
			pcPrint("Motor Movement: "+ left255 + " " + right255);
			
			arduinoConnection.sendMessage(motorData, 0, 6);
		}
	}
	
	private void setVassouraMovement(int speed) throws IOException
	{	
		byte Direction = 1;
		if(speed < 0)
		{
			speed = -speed;
			Direction = 0;
		}
		
		if(motorVassoura == speed)
		{
			return;
		}		
		
		this.motorVassoura = speed;
		
		int speed255 = convert100To255(speed, LIMIT_MOTOR_MOVEMENT);
					
		byte motorData[] = {0x13, (byte)speed255, Direction};
		
		if(arduinoConnection != null)
		{
			pcPrint("Vassoura Movement: "+ speed255);
			
			arduinoConnection.sendMessage(motorData, 0, 3);
		}
	}
	
	private void setMotorDoor(int speed) throws IOException
	{
		if(motorDoor == speed)
		{
			return;
		}
		
		this.motorDoor = speed;
		
		// The motor is a servo motor, so 0 is maximum speed on one direction
		// 180 is maximum speed on the other direction and 90 is zero speed
		speed = ((speed + 100) * 180) / 200;
		
		byte doorData[] = {0x33, (byte)speed};
		
		if(arduinoConnection != null)
		{
			arduinoConnection.sendMessage(doorData, 0, 2);
		}
	}
	
	private void setBuzzer(int buzz) throws IOException
	{
		if(buzzer == buzz)
		{
			return;
		}
		
		this.buzzer = buzz;
		
		byte buzzData[] = {0x45, (byte)buzz};
		
		if(arduinoConnection != null)
		{
			arduinoConnection.sendMessage(buzzData, 0, 2);
		}
	}
	
	private Can getNearestCan(CameraProcessor cameraProcessor) throws IOException
	{
		Can nearestCan = null;
		
		List<Can> listOfCans = cameraProcessor.getListOfCans();
		Iterator<Can> itr = listOfCans.iterator();
		
		while(itr.hasNext())
		{
			Can can = itr.next();
			
			pcPrint("getNearest -> minY: "+can.minY + " maxSand: "+cameraProcessor.getMaxSand());
			
			//if(can.minY < cameraProcessor.getBlueLimits() && can.minY < cameraProcessor.getTrashPosition().y)
			if(can.minY < cameraProcessor.getMaxSand() && (can.minY < trash.position.y || trash.position.y < 0))
			{
				if(nearestCan == null)
				{
					nearestCan = can;
				}
				else if(can.minY < nearestCan.minY)
				{					
					nearestCan = can;
				}
			}
		}
		
		return nearestCan;
		
	}
	
	private void stateGoToCan(Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != GO_TO_CAN)
		{
			time = System.currentTimeMillis() - 1000;
		}
		
		Can can = getNearestCan(cameraProcessor);
		
		int width = cameraProcessor.getFrameWidth();		
		int robotCenter = width/2 + ROBOT_CENTER_OFFSET;		
		int error = 0;
		
		if(can != null)
		{
			error = ((int)can.position.x) - robotCenter;
		}
				
		if(can == null)
		{
			//state = SEARCH_CAN;
			setMotorsMovement(0, 0);
		}
		else
		{			
			if(System.currentTimeMillis() > time + 100)
			{
				time = System.currentTimeMillis();				
				
				int powerLeft = 70 + error;
				int powerRight = 70 - error;
								
				setMotorsMovement( powerLeft, powerRight);					
			}
		}
						
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		//checkObstacle(cameraProcessor, ult);
	}
	
	private void stateWaitStart(Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{							
		//this.setMotorsMovement(0, 0);
		//this.setMotorClawOpenClose(0);
		//this.setMotorClawUpDown(0);
		//this.setMotorDoor(0);
		//this.setBuzzer(0);
	}
	
	private void stateStop(Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		this.setMotorsMovement(0, 0);
		this.setVassouraMovement(0);
		this.setMotorDoor(0);
		this.setBuzzer(0);		
	}
	
	private void stateForward(Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != FORWARD)
		{
			time = System.currentTimeMillis() + 1500;
		}
		
		this.setMotorsMovement(80, 80);
		this.setVassouraMovement(80);
		
		if(System.currentTimeMillis() > time)
		{
			state = STOP;
		}		
	}
	
	public void process(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor)
	{
		int lastStateTemp = state;
		try
		{
			switch(state)
			{
				case WAIT_START:
					stateWaitStart(acc, comp, ult, cameraProcessor);
				break;			
				case STOP:
					stateStop(acc, comp, ult, cameraProcessor);
				break;
				case FORWARD:
					stateForward(acc, comp, ult, cameraProcessor);
				break;
				case GO_TO_CAN:
					stateGoToCan(acc, comp, ult, cameraProcessor);
				break;
			}
			
			lastState = lastStateTemp;
			
			if(lastState != state)
			{
				pcPrint("State: "+state);
			}
		}
		catch(Exception e)
		{
			try {
				pcPrint(e.toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			Log.i(TAG, "Error on process");
			
			act.reconectArduino();
		}		
	}
	
	public void startButtonPressed()
	{		
		if(state == WAIT_START)
		{
			try 
			{
				pcPrint("Robot Brain Started");
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		
			
			state = STOP;
		}
	}
	
}
