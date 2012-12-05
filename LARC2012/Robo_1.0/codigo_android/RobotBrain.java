package erus.android;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Point;

import android.util.Log;

public class RobotBrain
{
	private static final String TAG = "RobotBrain";
	
	private static final int LIMIT_MOTOR_MOVEMENT = 255;
	private static final int LIMIT_MOTOR_CLAW = 191;
	
	public static final int ROBOT_CENTER_OFFSET = -25;
	public static final int CATCHABLE_CAN_LIMIT_Y_MIN = 10;
	public static final int CATCHABLE_CAN_LIMIT_Y_MAX = 30;
	public static final int CATCHABLE_CAN_LIMIT_X = 40;
	
	// States
	private static final int NO_STATE = -1;
	private static final int WAIT_START = 0;
	private static final int FORWARD = 1001;
	private static final int STOP = 1002;
	private static final int SEARCH_CAN = 1504;
	private static final int SEARCH_CAN_LEFT_1 = 1505;
	private static final int SEARCH_CAN_LEFT_2 = 1506;
	private static final int SEARCH_CAN_LEFT_3 = 1507;
	private static final int SEARCH_CAN_LEFT_4 = 1508;
	private static final int SEARCH_CAN_RIGHT_1 = 1551;
	private static final int SEARCH_CAN_RIGHT_2 = 1552;
	private static final int SEARCH_CAN_RIGHT_3 = 1553;
	private static final int SEARCH_CAN_RIGHT_4 = 1554;
	private static final int GO_TO_CAN = 1601;
	private static final int DEBOUNCE_CATCH_GO_TO_CAN = 1602;
	private static final int CATCH_CAN_1 = 2001;
	private static final int CATCH_CAN_2 = 2002;
	private static final int CATCH_CAN_3 = 2003;
	private static final int CATCH_CAN_4 = 2004;
	private static final int CATCH_CAN_5 = 2005;
	private static final int CATCH_CAN_6 = 2006;
	private static final int CATCH_CAN_7 = 2007;
	private static final int CATCH_CAN_8 = 2008;
	private static final int CATCH_CAN_9 = 2009;
	private static final int CATCH_CAN_10 = 2010;
	private static final int CATCH_CAN_11 = 2011;
	private static final int CATCH_CAN_12 = 2012;
	private static final int CATCH_CAN_13 = 2013;
	private static final int OPEN_DEPOSIT_1 = 3001;
	private static final int OPEN_DEPOSIT_2 = 3002;
	private static final int OPEN_DEPOSIT_3 = 3003;
	private static final int CLOSE_DEPOSIT_1 = 3011;
	private static final int CLOSE_DEPOSIT_2 = 3012;
	private static final int RUN_FROM_BLUE_1 = 4001;
	private static final int RUN_FROM_BLUE_2 = 4002;	
	private static final int ON_CURVE_1 = 5001;
	private static final int ON_CURVE_2 = 5002;
	private static final int ON_CURVE_3 = 5003;
	private static final int ON_CURVE_4 = 5004;
	private static final int ON_CURVE_5 = 5005;
	private static final int ON_CURVE_6 = 5006;
	private static final int ON_CURVE_7 = 5007;
	private static final int ON_CURVE_8 = 5008;
	private static final int ON_CURVE_9 = 5009;
	private static final int RUN_FROM_OBSTACLE_0 = 6000;
	private static final int RUN_FROM_OBSTACLE_1 = 6001;
	private static final int RUN_FROM_OBSTACLE_2 = 6002;
	private static final int RUN_FROM_OBSTACLE_3 = 6003;
	private static final int RUN_FROM_OBSTACLE_4 = 6004;

	private static final int SEARCH_TRASH = 7001;
	private static final int SEARCH_TRASH_LEFT_1 = 7002;
	private static final int SEARCH_TRASH_RIGHT_1 = 7003;
	private static final int GO_TO_TRASH = 7004;
	private static final int MANOUVER_TO_TRASH = 7005;
	private static final int MANOUVER_TO_TRASH_BACK_UP = 7006;
	private static final int GO_TO_TRASH_DEBOUNCE_ULTRASOUND = 7007;
	private static final int SEARCH_TRASH_LEFT_2 = 7008;
	private static final int SEARCH_TRASH_LEFT_3 = 7009;
	private static final int SEARCH_TRASH_LEFT_4 = 7010;
	private static final int SEARCH_TRASH_RIGHT_2 = 7011;
	private static final int SEARCH_TRASH_RIGHT_3 = 7012;
	private static final int SEARCH_TRASH_RIGHT_4 = 7013;
	private static final int MANOUVER_TO_TRASH2 = 7014;	

	private int state, turnState;
	private int initialEncoder, currentTurn, totalTurn;
	
	private int motorLeft;
	private int motorRight;
	private int motorClawOpenClose;
	private int motorClawUpDown;
	private int motorDoor;
	private int buzzer;
	private int lastState;
	private long time;
	
	private int canCount;
	
	private Connection arduinoConnection;
	private Connection pcConnection;
	
	private ErusView erusView;
	private Trash trash = new Trash();
		
	public RobotBrain(Connection arduinoConnection, Connection pcConnection, ErusView erusView)
	{
		state = WAIT_START;
		lastState = NO_STATE;
		
		motorLeft = -1;
		motorRight = -1;
		motorClawOpenClose = -1;
		motorClawUpDown = -1;
		motorDoor = -1;
		buzzer = -1;
		
		canCount = 0;
		
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
					
		byte motorData[] = {0x14, (byte)left255, leftDirection, (byte)right255, rightDirection};
		
		if(arduinoConnection != null)
		{
			pcPrint("Motor Movement: "+ left255 + " " + right255);
			
			arduinoConnection.sendMessage(motorData, 0, 5);
		}
	}
	
	private void setMotorClawOpenClose(int power) throws IOException
	{		
		if(motorClawOpenClose == power)
		{
			return;
		}
		
		this.motorClawOpenClose = power;
		
		byte direction = 0;
		if(power < 0)
		{
			power = -power;
			direction = 1;
		}
		
		int power255 = convert100To255(power, LIMIT_MOTOR_CLAW);
		
		
		byte clawData[] = {0x31, (byte)power255, direction};
		
		if(arduinoConnection != null)
		{
			arduinoConnection.sendMessage(clawData, 0, 3);
		}
	}

	private void setMotorClawUpDown(int power) throws IOException
	{
		power = -power;
		
		if(motorClawUpDown == power)
		{
			return;
		}
		
		this.motorClawUpDown = power;
		
		byte direction = 0;
		if(power < 0)
		{
			power = -power;
			direction = 1;
		}
		
		int power255 = convert100To255(power, LIMIT_MOTOR_CLAW);
				
		byte clawData[] = {0x32, (byte)power255, direction};
		
		if(arduinoConnection != null)
		{
			arduinoConnection.sendMessage(clawData, 0, 3);
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
	
	
	private void stateWaitStart(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{							
		this.setMotorsMovement(0, 0);
		this.setMotorClawOpenClose(0);
		this.setMotorClawUpDown(0);
		this.setMotorDoor(0);
		this.setBuzzer(0);
	}
	
	private void keepClawUp(ClawButton cb) throws IOException
	{
		if(cb.getState() != 0)
		{
			setMotorClawUpDown(0);
		}
		else
		{
			setMotorClawUpDown(100);
		}
		
		setMotorClawOpenClose(0);
	
	}
	
	/*private void turn(int nextState, int degrees, Compass comp)
	{
		turnState = nextState;
		this.degrees = degrees;

		initialDegrees = 57.3 * Math.atan2(comp.getY(), comp.getZ()) ;
		
		// Range: 0 - 360
		//initialDegrees = ((int)initialDegrees + 180)%360;
		
		state = ON_CURVE_1;
	}
	
	private void stateOnCurve1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		double currentDegree = 57.3 * Math.atan2(comp.getY(), comp.getZ());
		int error = ((int)currentDegree - ((int)initialDegrees + (int)degrees))%360;
		
		
		if (error > 0)
		{
			setMotorsMovement(100, 0);	
		}
		else
		{
			setMotorsMovement(0, 100);
		}
		
		state = ON_CURVE_2;
	}
	
	private void stateOnCurve2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		
		if(lastState != ON_CURVE_2)
		{
			time = System.currentTimeMillis() + 500;
		}
		if(System.currentTimeMillis() > time)
		{
			state = ON_CURVE_3;
		}
	}
	
	private void stateOnCurve3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		
		setMotorsMovement(100, 100);	
		
		state = ON_CURVE_4;
	}
	
	private void stateOnCurve4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != ON_CURVE_4)
		{
			time = System.currentTimeMillis() + 400;
		}
		if(System.currentTimeMillis() > time)
		{
			state = ON_CURVE_5;
		}

	}
	
	private void stateOnCurve5(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		double currentDegree = 57.3 * Math.atan2(comp.getY(), comp.getZ());
		
		int error = ((int)currentDegree - ((int)initialDegrees + (int)degrees))%360;
		
		if (error > 0)
		{
			setMotorsMovement(0, -100);	
		}
		else
		{
			setMotorsMovement(-100, 0);
		}
		
		state = ON_CURVE_6;

	}
	
	private void stateOnCurve6(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != ON_CURVE_6)
		{
			time = System.currentTimeMillis() + 400;
		}
		if(System.currentTimeMillis() > time)
		{
			state = ON_CURVE_7;
		}		
	}
	
	private void stateOnCurve7(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(-100, -100);
		
		state = ON_CURVE_8;
	}
	
	private void stateOnCurve8(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != ON_CURVE_8)
		{
			time = System.currentTimeMillis() + 400;
		}
		if(System.currentTimeMillis() > time)
		{
			state = ON_CURVE_9;
		}		
	}
	
	private void stateOnCurve9(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		double currentDegree = 57.3 * Math.atan2(comp.getY(), comp.getZ());
		
		int error = ((int)currentDegree - ((int)initialDegrees + (int)degrees))%360;
				
		erusView.setTeste(currentDegree+"");
		if( Math.abs(error) < 5)			
		{
			state = turnState;
		}
		else
		{
			state = ON_CURVE_1;
		}
	}*/
	
	private void turn(int nextState, int turns)
	{
		turnState = nextState;
		this.currentTurn = 0;
		this.totalTurn = turns;
		
		state = ON_CURVE_1;
	}
	
	private void stateOnCurve1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{	
		
		setMotorsMovement(0,100);
		
		state = ON_CURVE_2;
	}
	
	private void stateOnCurve2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		
		if(lastState != ON_CURVE_2)
		{
			this.initialEncoder = enc.getEncRight() + 50;
		}
		if(enc.getEncRight() > this.initialEncoder)
		{
			state = ON_CURVE_3;
		}
	}
	
	private void stateOnCurve3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		
		setMotorsMovement(100, 100);	
		
		state = ON_CURVE_4;
	}
	
	private void stateOnCurve4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != ON_CURVE_4)
		{
			time = System.currentTimeMillis() + 400;
		}
		if(System.currentTimeMillis() > time)
		{
			state = ON_CURVE_5;
		}

	}
	
	private void stateOnCurve5(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		setMotorsMovement(-100,0);
		
		state = ON_CURVE_6;

	}
	
	private void stateOnCurve6(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != ON_CURVE_6)
		{
			this.initialEncoder = enc.getEncLeft() - 50;
		}
		if(enc.getEncLeft() > this.initialEncoder)
		{
			state = ON_CURVE_7;
		}	
	}
	
	private void stateOnCurve7(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(-100, -100);
		
		state = ON_CURVE_8;
	}
	
	private void stateOnCurve8(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != ON_CURVE_8)
		{
			time = System.currentTimeMillis() + 400;
		}
		if(System.currentTimeMillis() > time)
		{
			state = ON_CURVE_9;
		}		
	}
	
	private void stateOnCurve9(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(0,0);
		this.currentTurn++;
		if( this.currentTurn < this.totalTurn)			
		{
			state = ON_CURVE_1;
		}
		else
		{
			state = turnState;
		}
	}
	
	private void stateForward(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		this.setMotorsMovement(0, 0);
		this.setMotorDoor(-5);
		
		//turn(STOP, 2);
		
		state = SEARCH_CAN;
		
		
		//state = SEARCH_TRASH;
		
		/*if(ult.getUs4() < 10)
		{
			
			
			//pcPrint("Search can");
			
			//state = SEARCH_CAN;
			//turn(STOP,30,comp);
		}*/
	}
	
	private void stateStop(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		this.setMotorsMovement(0,0);
		this.setMotorClawUpDown(0);
		this.setMotorClawOpenClose(0);		
	}
	
	private void stateOpenDeposit1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		this.setMotorsMovement(0,0);
		this.setMotorClawUpDown(0);
		this.setMotorClawOpenClose(0);
		
		state = OPEN_DEPOSIT_2;
	}
	
	private void stateOpenDeposit2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{	
		this.setMotorDoor(15);
		
		state = OPEN_DEPOSIT_3;
	}
	
	private void stateOpenDeposit3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != OPEN_DEPOSIT_3)
		{
			time = System.currentTimeMillis() + 2000;
		}
		if(System.currentTimeMillis() > time)
		{
			state = CLOSE_DEPOSIT_1;
		}		
	}
	
	private void stateCloseDeposit1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		this.setMotorDoor(-5);
		state = CLOSE_DEPOSIT_2;
	}
	
	private void stateCloseDeposit2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != CLOSE_DEPOSIT_2)
		{
			time = System.currentTimeMillis() + 2000;
		}
		if(System.currentTimeMillis() > time)
		{
			state = FORWARD;
			canCount = 0;
		}
	}	
	
	private void stateCatchCan1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(0,0);
		setMotorClawUpDown(-30);
		setMotorClawOpenClose(-30);
		state = CATCH_CAN_2;
	}
	
	private void stateCatchCan2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != CATCH_CAN_2)
		{
			time = System.currentTimeMillis() + 2000;
		}
		
		if(System.currentTimeMillis() > time)
		{					
			state = CATCH_CAN_3;
		}
	}
	
	private void stateCatchCan3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{				
		setMotorsMovement(0,0);
		setMotorClawUpDown(-30);
		setMotorClawOpenClose(-30);
		state = CATCH_CAN_4;		
	}
	
	private void stateCatchCan4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != CATCH_CAN_4)
		{
			time = System.currentTimeMillis() + 6000;
		}
		
		if(System.currentTimeMillis() > time)
		{					
			state = CATCH_CAN_5;
		}
	}
	
	private void stateCatchCan5(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(100,100);
		setMotorClawUpDown(0);
		setMotorClawOpenClose(-30);
		state = CATCH_CAN_6;
	}
	
	private void stateCatchCan6(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != CATCH_CAN_6)
		{
			time = System.currentTimeMillis() + 500;
		}
		
		if (ult.getUs1() <= 10)
		{
			state = CATCH_CAN_7;
		}
			
		
		if(System.currentTimeMillis() > time)
		{
			state = CATCH_CAN_7;
		}
	}
	
	private void stateCatchCan7(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(0,0);
		setMotorClawUpDown(0);
		setMotorClawOpenClose(100);
		state = CATCH_CAN_8;
	}
	
	private void stateCatchCan8(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != CATCH_CAN_8)
		{
			time = System.currentTimeMillis() + 1500;
		}
		if(System.currentTimeMillis() > time)
		{
			state = CATCH_CAN_9;
		}
	}
	
	private void stateCatchCan9(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(0,0);
		setMotorClawUpDown(100);
		setMotorClawOpenClose(100);
		state = CATCH_CAN_10;
	}
	
	private void stateCatchCan10(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(cb.getState() != 0)
		{
			state = CATCH_CAN_11;
		}
	}
	
	private void stateCatchCan11(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(0,0);
		setMotorClawUpDown(0);
		setMotorClawOpenClose(-30);
		state = CATCH_CAN_12;
	}
	
	private void stateCatchCan12(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != CATCH_CAN_12)
		{
			time = System.currentTimeMillis() + 1500;
		}
		
		if(System.currentTimeMillis() > time)
		{
			state = CATCH_CAN_13;
		}
	}
	
	private void stateCatchCan13(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(0,0);
		setMotorClawUpDown(0);
		setMotorClawOpenClose(0);
		
		canCount++;
		
		state = FORWARD;
		
	}
	
	private Can getNearestCan(CameraProcessor cameraProcessor) throws IOException
	{
		Can nearestCan = null;
		
		List<Can> listOfCans = cameraProcessor.getListOfCans();
		Iterator<Can> itr = listOfCans.iterator();
		
		while(itr.hasNext())
		{
			Can can = itr.next();
			
			//pcPrint("getNearest -> minY: "+can.minY + " minBlue: "+cameraProcessor.getBlueLimits());
			
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
	
	private void stateSearchCan(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		
		if(Math.random() > 0.5 || ult.getUs2() < 30)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_LEFT_4;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_LEFT_3;
			}
			else
			{
				state = SEARCH_CAN_LEFT_4;
			}
		}
		else
		{
			int rand = (int) (Math.random() * 4);
						
			if(rand < 1)
			{
				state = SEARCH_CAN_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_RIGHT_4;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_RIGHT_3;
			}
			else
			{
				state = SEARCH_CAN_RIGHT_4;
			}
		}
	}
	
	private void stateSearchCanLeft1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_LEFT_1)
		{
			time = System.currentTimeMillis() + 5000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_LEFT_3;
			}
			else
			{
				state = SEARCH_CAN_LEFT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( 50, 100);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchCanLeft2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_LEFT_2)
		{
			time = System.currentTimeMillis() + 3000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_LEFT_3;
			}
			else
			{
				state = SEARCH_CAN_LEFT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( -100, -100);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchCanLeft3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_LEFT_3)
		{
			time = System.currentTimeMillis() + 5000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_LEFT_3;
			}
			else
			{
				state = SEARCH_CAN_LEFT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( 50, 100);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchCanLeft4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_LEFT_4)
		{
			time = System.currentTimeMillis() + 5000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_LEFT_3;
			}
			else
			{
				state = SEARCH_CAN_LEFT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( 100, 100);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchCanRight1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_RIGHT_1)
		{
			time = System.currentTimeMillis() + 5000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_RIGHT_3;
			}
			else
			{
				state = SEARCH_CAN_RIGHT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( 100, 50);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchCanRight2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_RIGHT_2)
		{
			time = System.currentTimeMillis() + 3000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_RIGHT_3;
			}
			else
			{
				state = SEARCH_CAN_RIGHT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( -100, -100);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchCanRight3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_RIGHT_3)
		{
			time = System.currentTimeMillis() + 5000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_RIGHT_3;
			}
			else
			{
				state = SEARCH_CAN_RIGHT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( 100, 50);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchCanRight4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_CAN_RIGHT_4)
		{
			time = System.currentTimeMillis() + 5000;
		}
				
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_CAN_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_CAN_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_CAN_RIGHT_3;
			}
			else
			{
				state = SEARCH_CAN_RIGHT_4;
			}
		}
				
		Can can = getNearestCan(cameraProcessor);
		
		if(can != null)
		{
			state = GO_TO_CAN;
		}
		else
		{
			setMotorsMovement( 100, 100);
		}
				
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private boolean isCanCatchable(Can can, UltraSound ult, int error)
	{
		if(ult.getUs5() > 22 && ult.getUs5() < 35 )
		{
			return true;
		}
		
		if( can.minY > CATCHABLE_CAN_LIMIT_Y_MIN && can.minY < CATCHABLE_CAN_LIMIT_Y_MAX && Math.abs(error) < CATCHABLE_CAN_LIMIT_X)
		{
			return true;
		}
		
		return false;
	}
	
	private void checkBlueLimits(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(cameraProcessor.getBlueLimits() <= CATCHABLE_CAN_LIMIT_Y_MIN)
		{
			setMotorsMovement(0,0);
			state = RUN_FROM_BLUE_1;
		}
	}
	
	private void checkObstacle(CameraProcessor camera, UltraSound ult) throws IOException
	{
		if(ult.getUs6() < 40 || ult.getUs3() < 40)
		{
			state = RUN_FROM_OBSTACLE_0;
		}
		
		if(trash.size > 100)
		{
			state = RUN_FROM_OBSTACLE_1;
		}
		
		if(camera.getTotalSandArea() < camera.getFrameHeight() * camera.getFrameWidth() / 8)
		{
			state = RUN_FROM_OBSTACLE_1;
		}
	}
	
	private void runFromObstacle0(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_0)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		keepClawUp(cb);
		setMotorsMovement(0, 0);
		
		if(System.currentTimeMillis() < time)
		{
			if(ult.getUs6() > 40 && ult.getUs3() > 40)
			{
				state = FORWARD;
			}
		}
		else
		{
			state = RUN_FROM_OBSTACLE_1;
		}
	}
	
	private void runFromObstacle1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_1)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		keepClawUp(cb);
		setMotorsMovement(-100, -100);
		
		if(System.currentTimeMillis() > time || ult.getUs4() < 30)
		{
			if(Math.random() < 0.2)
			{
				state = RUN_FROM_OBSTACLE_2;
			}
			else
			{
				state = RUN_FROM_OBSTACLE_3;
			}
		}
	}
	
	private void runFromObstacle2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_2)
		{
			time = System.currentTimeMillis() + 2000 + System.currentTimeMillis()%4000;
		}
		
		keepClawUp(cb);
		setMotorsMovement(-100, -50);
		
		if(System.currentTimeMillis() > time || ult.getUs4() < 30)
		{
			state = RUN_FROM_OBSTACLE_4;
		}
	}
	
	private void runFromObstacle3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_3)
		{
			time = System.currentTimeMillis() + 2000 + System.currentTimeMillis()%4000;
		}
		
		keepClawUp(cb);
		setMotorsMovement(-50, -100);
		
		if(System.currentTimeMillis() > time || ult.getUs4() < 30)
		{
			state = RUN_FROM_OBSTACLE_4;
		}
	}
	
	private void runFromObstacle4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		/*
		if(lastState != RUN_FROM_OBSTACLE_4)
		{
			time = System.currentTimeMillis() + 1000 + System.currentTimeMillis()%2000;
		}
		
		keepClawUp(cb);
		setMotorsMovement(100, 100);
		
		if(System.currentTimeMillis() > time)
		{
			state = FORWARD;
		}
		*/
		state = FORWARD;
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateGoToCan(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{			
		keepClawUp(cb);
		
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
			state = SEARCH_CAN;
			setMotorsMovement(0, 0);
		}
		else if( isCanCatchable(can, ult, error) )
		{
			state = DEBOUNCE_CATCH_GO_TO_CAN;
			setMotorsMovement(0, 0);
		}
		else if(ult.getUs5() < 22 || can.minY < CATCHABLE_CAN_LIMIT_Y_MIN)
		//else if(can.minY < CATCHABLE_CAN_LIMIT_Y_MIN)
		{
			if(System.currentTimeMillis() > time + 100)
			{
				time = System.currentTimeMillis();				
				
				int powerLeft = -100 + error;
				int powerRight = -100 - error;
				
				if(powerLeft > -50)
				{
					powerLeft = -50;
				}
				
				setMotorsMovement( powerLeft, powerRight);					
			}
		}
		else
		{			
			if(System.currentTimeMillis() > time + 100)
			{
				time = System.currentTimeMillis();				
				
				int powerLeft = 100 + error;
				int powerRight = 100 - error;
				
				if(powerRight < 50)
				{
					powerRight = 50;
				}
				
				setMotorsMovement( powerLeft, powerRight);					
			}
		}
						
		//checkBlueLimits(acc,comp,enc,ult,cb,cameraProcessor);
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateDebounceUltrasoundGoToCan(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		if(lastState != DEBOUNCE_CATCH_GO_TO_CAN)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		setMotorsMovement( 0, 0);
		
		if(System.currentTimeMillis() < time)
		{						
			Can can = getNearestCan(cameraProcessor);
			
			int width = cameraProcessor.getFrameWidth();		
			int robotCenter = width/2 + ROBOT_CENTER_OFFSET;		
			int error = 0;			
			if(can != null)
			{
				error = ((int)can.position.x) - robotCenter;
			}
			
			if(isCanCatchable(can, ult, error) == false)
			{
				state = GO_TO_CAN;
			}	
		}
		else
		{
			state = CATCH_CAN_1;
		}	
	}
	
	private void runFromBlue1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		setMotorsMovement(-100, -100);
		
		if(cameraProcessor.getBlueLimits() > CATCHABLE_CAN_LIMIT_Y_MAX )
		{
			state = RUN_FROM_BLUE_2;
		}		
	}
	
	private void runFromBlue2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
						
		//this.turn(FORWARD, 180, comp);		
	}

	private void stateSearchTrash(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(System.currentTimeMillis()%100 > 50)
		{
			state = SEARCH_TRASH_RIGHT_1;
		}
		else
		{
			state = SEARCH_TRASH_LEFT_1;
		}
	}
	
	private void stateSearchTrashLeft1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_LEFT_1)
		{
			time = System.currentTimeMillis() + 3000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_LEFT_3;
			}
			else
			{
				state = SEARCH_TRASH_LEFT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( 100, 100);
		}
	}
	
	private void stateSearchTrashLeft2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_LEFT_2)
		{
			time = System.currentTimeMillis() + 5000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_LEFT_3;
			}
			else
			{
				state = SEARCH_TRASH_LEFT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( -100, -100);
		}
	}
	
	private void stateSearchTrashLeft3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_LEFT_3)
		{
			time = System.currentTimeMillis() + 2000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_LEFT_3;
			}
			else
			{
				state = SEARCH_TRASH_LEFT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( -50, -100);
		}
	}
	
	private void stateSearchTrashLeft4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_LEFT_4)
		{
			time = System.currentTimeMillis() + 5000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_LEFT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_LEFT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_LEFT_3;
			}
			else
			{
				state = SEARCH_TRASH_LEFT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( -50, -100);
		}
	}
	
	private void stateSearchTrashRight1(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_RIGHT_1)
		{
			time = System.currentTimeMillis() + 5000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_RIGHT_3;
			}
			else
			{
				state = SEARCH_TRASH_RIGHT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( -100, -100);
		}		
	}
	
	private void stateSearchTrashRight2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_RIGHT_2)
		{
			time = System.currentTimeMillis() + 3000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_RIGHT_3;
			}
			else
			{
				state = SEARCH_TRASH_RIGHT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( 100, 100);
		}		
	}
	
	private void stateSearchTrashRight3(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_RIGHT_3)
		{
			time = System.currentTimeMillis() + 5000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_RIGHT_3;
			}
			else
			{
				state = SEARCH_TRASH_RIGHT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( -100, -50);
		}		
	}
	
	private void stateSearchTrashRight4(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		if(lastState != SEARCH_TRASH_RIGHT_4)
		{
			time = System.currentTimeMillis() + 5000;
		}
		
		if(System.currentTimeMillis() > time)
		{
			int rand = (int) (Math.random() * 4);
			
			if(rand < 1)
			{
				state = SEARCH_TRASH_RIGHT_1;
			}
			else if(rand < 2)
			{
				state = SEARCH_TRASH_RIGHT_2;
			}
			else if(rand < 3)
			{
				state = SEARCH_TRASH_RIGHT_3;
			}
			else
			{
				state = SEARCH_TRASH_RIGHT_4;
			}
		}
		
			
		if(trash.position.x > 0)
		{
			state = GO_TO_TRASH;
		}
		else
		{
			setMotorsMovement( -100, -50);
		}		
	}
	
	private void stateGoToTrash(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		keepClawUp(cb);
		
		Point trashPosition = trash.position;
		
		int width = cameraProcessor.getFrameWidth();		
		int robotCenter = width/2 + ROBOT_CENTER_OFFSET;		
		int error = ((int)trashPosition.x) - robotCenter;

		if(trashPosition.x == 0)
		{
			state = SEARCH_TRASH;
			setMotorsMovement(0, 0);
		}
		else if(ult.getUs4() < 35)
		{
			state = GO_TO_TRASH_DEBOUNCE_ULTRASOUND;
//			state = MANOUVER_TO_TRASH;
			setMotorsMovement(0, 0);
		}
/*		else if( can.minY < CATCHABLE_CAN_LIMIT_Y_MIN)
		{
			if(System.currentTimeMillis() > time + 100)
			{
				time = System.currentTimeMillis();				
				
				setMotorsMovement( -75 - error, -75 + error);					
			}
		}*/
		else
		{			
			if(System.currentTimeMillis() > time + 100)
			{
				time = System.currentTimeMillis();	
				
				int left = -100 + error;
				int right = -100 - error;
				
				if(right > -50)
				{
					right = -50;
				}
				
				if(left > -50)
				{
					left = -50;
				}
				
				setMotorsMovement( left, right);					
			}
		}
	}
	
	private void stateGoToTrashDebounceUltrasound(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{		
		if(lastState != GO_TO_TRASH_DEBOUNCE_ULTRASOUND)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		setMotorsMovement( 0, 0);
		
		if(System.currentTimeMillis() < time)
		{			
			if(ult.getUs4() >= 35)
			{
				state = GO_TO_TRASH;
			}	
		}
		else
		{
			state = MANOUVER_TO_TRASH;
		}	
	}
	
	private void stateManouverToTrash(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(ult.getUs4() < 5)
		{
			setMotorsMovement(0, 0);
			state = MANOUVER_TO_TRASH2;
		}
		else
		{
			setMotorsMovement(-100,-100);
		}
	}
	
	private void stateManouverToTrash2(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != MANOUVER_TO_TRASH2)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		setMotorsMovement( 0, 0);
		
		if(System.currentTimeMillis() < time)
		{			
			if(ult.getUs4() >= 5)
			{
				state = MANOUVER_TO_TRASH;
			}	
		}
		else
		{
			state = MANOUVER_TO_TRASH_BACK_UP;
		}
	}
	
	private void stateManouverToTrashBackUp(Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor) throws IOException
	{
		setMotorsMovement(0, 0);
		state = OPEN_DEPOSIT_1;
	}
	
	public void process(CodigoAndroidActivity act, Accelerometer acc, Compass comp, Encoder enc, UltraSound ult, ClawButton cb, CameraProcessor cameraProcessor)
	{
		int lastStateTemp = state;
		try
		{
			switch(state)
			{
				case WAIT_START:
					stateWaitStart(acc, comp, enc, ult, cb,cameraProcessor);
				break;			
				case FORWARD:
					stateForward(acc, comp, enc, ult, cb,cameraProcessor);
				break;
				case SEARCH_CAN:
					stateSearchCan(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_LEFT_1:
					stateSearchCanLeft1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_LEFT_2:
					stateSearchCanLeft2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_LEFT_3:
					stateSearchCanLeft3(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_LEFT_4:
					stateSearchCanLeft4(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_RIGHT_1:
					stateSearchCanRight1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_RIGHT_2:
					stateSearchCanRight2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_RIGHT_3:
					stateSearchCanRight3(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case SEARCH_CAN_RIGHT_4:
					stateSearchCanRight4(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case GO_TO_CAN:
					stateGoToCan(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case DEBOUNCE_CATCH_GO_TO_CAN:
					stateDebounceUltrasoundGoToCan(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case CATCH_CAN_1:
					stateCatchCan1(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_2:
					stateCatchCan2(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_3:
					stateCatchCan3(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_4:
					stateCatchCan4(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_5:
					stateCatchCan5(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_6:
					stateCatchCan6(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_7:
					stateCatchCan7(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_8:
					stateCatchCan8(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_9:
					stateCatchCan9(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_10:
					stateCatchCan10(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_11:
					stateCatchCan11(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_12:
					stateCatchCan12(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				case CATCH_CAN_13:
					stateCatchCan13(acc,comp,enc,ult,cb,cameraProcessor);
				break;
				
				case OPEN_DEPOSIT_1:
					stateOpenDeposit1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case OPEN_DEPOSIT_2:
					stateOpenDeposit2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case OPEN_DEPOSIT_3:
					stateOpenDeposit3(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case CLOSE_DEPOSIT_1:
					stateCloseDeposit1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case CLOSE_DEPOSIT_2:
					stateCloseDeposit2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case ON_CURVE_1:
					stateOnCurve1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case ON_CURVE_2:
					stateOnCurve2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
			
				case ON_CURVE_3:
					stateOnCurve3(acc, comp, enc, ult, cb, cameraProcessor);
				break;
			
				case ON_CURVE_4:
					stateOnCurve4(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case ON_CURVE_5:
					stateOnCurve5(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case ON_CURVE_6:
					stateOnCurve6(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case ON_CURVE_7:
					stateOnCurve7(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case ON_CURVE_8:
					stateOnCurve8(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case ON_CURVE_9:
					stateOnCurve9(acc, comp, enc, ult, cb, cameraProcessor);
				break;
			
				case RUN_FROM_BLUE_1:
					runFromBlue1(acc, comp, enc, ult, cb, cameraProcessor);
				break;				
				case RUN_FROM_BLUE_2:
					runFromBlue2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_0:
					runFromObstacle0(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_1:
					runFromObstacle1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_2:
					runFromObstacle2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_3:
					runFromObstacle3(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_4:
					runFromObstacle4(acc, comp, enc, ult, cb, cameraProcessor);
				break;

				case SEARCH_TRASH:
					stateSearchTrash(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_LEFT_1:
					stateSearchTrashLeft1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_LEFT_2:
					stateSearchTrashLeft2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_LEFT_3:
					stateSearchTrashLeft3(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_LEFT_4:
					stateSearchTrashLeft4(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_RIGHT_1:
					stateSearchTrashRight1(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_RIGHT_2:
					stateSearchTrashRight2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_RIGHT_3:
					stateSearchTrashRight3(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case SEARCH_TRASH_RIGHT_4:
					stateSearchTrashRight4(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case GO_TO_TRASH:
					stateGoToTrash(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case GO_TO_TRASH_DEBOUNCE_ULTRASOUND:
					stateGoToTrashDebounceUltrasound(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case MANOUVER_TO_TRASH:
					stateManouverToTrash(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case MANOUVER_TO_TRASH2:
					stateManouverToTrash2(acc, comp, enc, ult, cb, cameraProcessor);
				break;
				
				case MANOUVER_TO_TRASH_BACK_UP:
					stateManouverToTrashBackUp(acc, comp, enc, ult, cb, cameraProcessor);
				break;
						
				case STOP:
					stateStop(acc, comp, enc, ult,cb,cameraProcessor);
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
			
			state = FORWARD;
		}
	}
	
}
