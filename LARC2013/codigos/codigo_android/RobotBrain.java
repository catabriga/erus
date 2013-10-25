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
	
	public static final double ROBOT_CENTER_OFFSET = 0.0;
	public static final int CATCHABLE_CAN_LIMIT_Y_MIN = 10;
	public static final int CATCHABLE_CAN_LIMIT_Y_MAX = 30;
	public static final int CATCHABLE_CAN_LIMIT_X = 40;
	public static final int MIN_OBSTACLE_DISTANCE = 30;
	
	public static final int DEFAULT_VELOCITY = 100;
	
	// States
	private static final int NO_STATE = -1;
	private static final int WAIT_START = 0;
	private static final int STOP = 1002;
	
	private static final int GO_TO_CAN_FORWARD = 2001;
	private static final int GO_TO_CAN_TURN = 2002;
	private static final int SEARCH_CAN = 2003;
	private static final int LEFT_1 = 2011;
	private static final int LEFT_2 = 2012;
	private static final int LEFT_3 = 2013;
	private static final int LEFT_4 = 2014;
	private static final int RIGHT_1 = 2021;
	private static final int RIGHT_2 = 2022;
	private static final int RIGHT_3 = 2023;
	private static final int RIGHT_4 = 2024;
	private static final int FORWARD = 2004;
	
	private static final int GO_TO_TRASH_TURN = 3010;
	private static final int GO_TO_TRASH_FORWARD = 3011;
	//private static final int GO_TO_TRASH_DEBOUNCE_ULTRASOUND = 3012;
	private static final int GO_TO_TRASH_DEBOUNCE_BUTTON = 3012;
	private static final int SEARCH_TRASH = 3013;
	private static final int SEARCH_TRASH_LEFT_1 = 3014;
	private static final int SEARCH_TRASH_LEFT_2 = 3015;
	private static final int SEARCH_TRASH_RIGHT_1 = 3016;
	private static final int SEARCH_TRASH_RIGHT_2 = 3017;
		
	private static final int RUN_FROM_OBSTACLE_0 = 6000;
	private static final int RUN_FROM_OBSTACLE_1 = 6001;
	private static final int RUN_FROM_OBSTACLE_LEFT_BACK = 6002;
	private static final int RUN_FROM_OBSTACLE_LEFT_TURN = 6003;
	private static final int RUN_FROM_OBSTACLE_RIGHT_BACK = 6004;
	private static final int RUN_FROM_OBSTACLE_RIGHT_TURN = 6005;
	private static final int RUN_FROM_OBSTACLE_4 = 6006;
	
	private static final int OPEN_DEPOSIT = 8001;
	private static final int BACKUP_FROM_TRASH = 8002;
	private static final int CLOSE_DEPOSIT = 8003;
	
	private static final int TURNING_TIME = 500;
	private static final int SEARCHING_TIME = 500;
	private static final int NUMBER_OF_TURNINGS = 4;
	
	private int state;
	private int lastState;
	
	private long time;
	private long lastTrashTime;
	
	private int motorLeft;
	private int motorRight;
	private int motorVassoura;
	private int motorDoor;
	private int vibratorMotor;
	private int buzzer;
	private int counter;
	private int obstacleButton;
	
	private Connection arduinoConnection;
	private Connection pcConnection;
	
	private ErusView erusView;
	
	public RobotBrain(Connection arduinoConnection, Connection pcConnection, ErusView erusView)
	{
		state = WAIT_START;
		lastState = NO_STATE;
		
		time = System.currentTimeMillis();
		
		motorLeft = -1;
		motorRight = -1;
		motorVassoura = -1;
		motorDoor = -1;
		buzzer = -1;
		
		this.arduinoConnection = arduinoConnection;
		this.pcConnection = pcConnection;
		
		this.erusView = erusView;
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
					
		byte motorData[] = {0x12, (byte)left255, leftDirection, 0x11, (byte)right255, rightDirection};
		
		if(arduinoConnection != null)
		{
			//pcPrint("Motor Movement: "+ left255 + " " + right255);
			
			arduinoConnection.sendMessage(motorData, 0, 6);
		}
	}
	
	private void setVassouraMovement(int speed) throws IOException
	{	
		speed = -speed;
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
	
	private void setVibrator(int speed) throws IOException
	{	
		if(speed < 0)
		{
			speed = -speed;
		}
		
		if(vibratorMotor == speed)
		{
			return;
		}		
		
		this.vibratorMotor = speed;
		
		int speed255 = convert100To255(speed, LIMIT_MOTOR_MOVEMENT);
					
		byte motorData[] = {Protocol.MOTOR_VIBRATOR, (byte)speed255};
		
		if(arduinoConnection != null)
		{
			pcPrint("Vibrator speed: "+ speed255);
			
			arduinoConnection.sendMessage(motorData, 0, 2);
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
		
		byte doorData[] = {0x14, (byte)speed};
		
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
			
			//pcPrint("getNearest -> minY: "+can.minY + " maxSand: "+cameraProcessor.getMaxSand());
			
			//if(can.minY < cameraProcessor.getBlueLimits() && can.minY < cameraProcessor.getTrashPosition().y)
			//if(can.minY < cameraProcessor.getMaxSand() && (can.minY < trash.position.y || trash.position.y < 0))
			if(can.minY < cameraProcessor.getMaxSand())
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
	
	private void stateGoToCanForward(Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != GO_TO_CAN_FORWARD)
		{
			time = System.currentTimeMillis() + SEARCHING_TIME;
		}
		
		Can can = getNearestCan(cameraProcessor);
			
		if(can == null)
		{
			state = FORWARD;
		}
		else
		{							
			
			setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
			
			setVassouraMovement(-100);
		}
		
		if(System.currentTimeMillis() > time)
		{
			state = GO_TO_CAN_TURN;
		}
						
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateGoToCanTurn(Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != GO_TO_CAN_TURN)
		{
			time = System.currentTimeMillis() + SEARCHING_TIME;
		}
		
		Can can = getNearestCan(cameraProcessor);
		
		int width = cameraProcessor.getFrameWidth();		
		int robotCenter = width/2 + (int)(ROBOT_CENTER_OFFSET*width);		
		double error = 0;
	
		if(can != null)
		{
			error = ((can.position.x) - robotCenter)/((double)width);
		}
				
		if(can == null)
		{
			state = FORWARD;
		}
		else
		{							
			
			if(error > 0.25)
			{
				setMotorsMovement(DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
			} 
			else if (error < -0.25){
				setMotorsMovement(-DEFAULT_VELOCITY, DEFAULT_VELOCITY);
			}
			else
			{
				state = GO_TO_CAN_FORWARD;
			}
			setVassouraMovement(-100);
		}
		
		if(System.currentTimeMillis() > time)
		{
			state = GO_TO_CAN_FORWARD;
		}
						
		checkObstacle(cameraProcessor, ult);
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
		this.setVibrator(0);
		this.setBuzzer(0);		
	}
	
	private void checkTrashTime()
	{
		if(System.currentTimeMillis() - lastTrashTime > 15 * 60 * 1000)
		{
			state = SEARCH_TRASH;
		}
	}
	
	private void stateSearchCan(Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		double random = Math.random();
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
		}
		else
		{
			//pcPrint("search Can");
			if(random < 0.2)
			{
				state = RIGHT_1;
			}
			else if(random < 0.4)
			{
				state = FORWARD;
			}
			else
			{
				state = LEFT_1;
			}
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateLeft1(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != LEFT_1)
		{
			time = System.currentTimeMillis() + TURNING_TIME;
		}
		
		setMotorsMovement(-DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time)
		{
			state = LEFT_2;			
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateLeft2(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != LEFT_2)
		{
			time = System.currentTimeMillis() + TURNING_TIME + 3500;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time)
		{
			state = LEFT_1; // parar de ir para tras			
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateLeft3(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != LEFT_3)
		{
			time = System.currentTimeMillis() + TURNING_TIME;
		}
		
		setMotorsMovement(-DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time)
		{
			state = LEFT_4;			
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateLeft4(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != LEFT_4)
		{
			time = System.currentTimeMillis() + TURNING_TIME;
		}
		
		setMotorsMovement(-DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time)
		{
			state = LEFT_1;			
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateForward(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != FORWARD)
		{
			time = System.currentTimeMillis() + 3000 + (int)(10000 * Math.random())%4000;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time)
		{
			state = SEARCH_CAN;			
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateRight1(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != RIGHT_1)
		{
			time = System.currentTimeMillis() + TURNING_TIME;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		setVassouraMovement(-100);
			
		if(System.currentTimeMillis() > time)
		{			
			state = RIGHT_2;
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
			return;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateRight2(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != RIGHT_2)
		{
			time = System.currentTimeMillis() + TURNING_TIME + 3500;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
			
		if(System.currentTimeMillis() > time)
		{			
			state = RIGHT_1; // parou de ir para tras
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
			return;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateRight3(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != RIGHT_3)
		{
			time = System.currentTimeMillis() + TURNING_TIME;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		setVassouraMovement(-100);
			
		if(System.currentTimeMillis() > time)
		{			
			state = RIGHT_4;
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
			return;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateRight4(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		Can can = getNearestCan(cameraProcessor);
		
		if(lastState != RIGHT_4)
		{
			time = System.currentTimeMillis() + TURNING_TIME;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		setVassouraMovement(-100);
			
		if(System.currentTimeMillis() > time)
		{			
			state = RIGHT_1;
		}
		
		if(can != null)
		{
			state = GO_TO_CAN_TURN;
			return;
		}
		
		checkTrashTime();
		checkObstacle(cameraProcessor, ult);
	}
	
	private void checkObstacle(CameraProcessor camera, UltraSound ult) throws IOException
	{
		if(ult.getUs1() < 40 || ult.getUs2() < 40 || ult.getUs4() < 40)
		{
			pcPrint("Ultrasound Obstacle");
			state = RUN_FROM_OBSTACLE_0;
		}
		
		
		if(camera.getTrashSize() > 200)
		{
			pcPrint("Trash Size");
			state = RUN_FROM_OBSTACLE_1;
		}
		
		
		//pcPrint(camera.getBlueLimits() + " < " + camera.getFrameHeight()/5);
		//pcPrint(camera.getTotalBlueArea() + " > " + camera.getFrameHeight() * camera.getFrameWidth() / 8);
		//if(camera.getTotalSandArea() < camera.getFrameHeight() * camera.getFrameWidth() / 8)
		if(camera.getBlueLimits() < camera.getFrameHeight()/5 && camera.getTotalBlueArea() > camera.getFrameHeight() * camera.getFrameWidth() / 8)
		{
			pcPrint("Obstacle blue limits");
			state = RUN_FROM_OBSTACLE_1;
		}
	}
	
	private void runFromObstacle0(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_0)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		setMotorsMovement(0, 0);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() < time)
		{
			if(ult.getUs1() > 40 && ult.getUs2() > 40 && ult.getUs4() > 40)
			{
				state = SEARCH_CAN;
			}
		}
		else
		{
			state = RUN_FROM_OBSTACLE_1;
		}
	}
	
	private void runFromObstacle1(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_1)
		{
			time = System.currentTimeMillis() + 500;
		}
		
		setMotorsMovement(-DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time || ult.getUs5() < 30 || ult.getUs6() < 30)
		{
			if(Math.random() < 0.2)
			{
				state = RUN_FROM_OBSTACLE_LEFT_BACK;
			}
			else
			{
				state = RUN_FROM_OBSTACLE_RIGHT_BACK;
			}
		}
	}
	
	private void runFromObstacleLeftBack(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_LEFT_BACK && lastState != RUN_FROM_OBSTACLE_LEFT_TURN)
		{
			counter = 0;
		}
		
		if(lastState != RUN_FROM_OBSTACLE_LEFT_BACK)
		{
			time = System.currentTimeMillis() + 200 + System.currentTimeMillis()%200;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time || ult.getUs5() < 30 || ult.getUs6() < 30)
		{
			state = RUN_FROM_OBSTACLE_LEFT_TURN;
			if(counter > NUMBER_OF_TURNINGS || ult.getUs5() < 30 || ult.getUs6() < 30)
			{
				state = RUN_FROM_OBSTACLE_4;
			}
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void runFromObstacleLeftTurn(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_LEFT_TURN)
		{
			time = System.currentTimeMillis() + 200 + System.currentTimeMillis()%200;
		}
		
		setMotorsMovement(-DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time || ult.getUs5() < 30 || ult.getUs6() < 30)
		{
			state = RUN_FROM_OBSTACLE_LEFT_BACK;
			counter++;
			if(ult.getUs5() < 30 || ult.getUs6() < 30)
			{
				state = RUN_FROM_OBSTACLE_4;
			}
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void runFromObstacleRightBack(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_RIGHT_BACK && lastState != RUN_FROM_OBSTACLE_RIGHT_TURN)
		{
			counter = 0;
		}
		
		if(lastState != RUN_FROM_OBSTACLE_RIGHT_BACK)
		{
			time = System.currentTimeMillis() + 200 + System.currentTimeMillis()%200;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time || ult.getUs5() < 30 || ult.getUs6() < 30)
		{
			state = RUN_FROM_OBSTACLE_RIGHT_TURN;
			if(counter > NUMBER_OF_TURNINGS || ult.getUs5() < 30 || ult.getUs6() < 30)
			{
				state = RUN_FROM_OBSTACLE_4;
			}
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void runFromObstacleRightTurn(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{
		if(lastState != RUN_FROM_OBSTACLE_RIGHT_TURN)
		{
			time = System.currentTimeMillis() + 200 + System.currentTimeMillis()%200;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		setVassouraMovement(-100);
		
		if(System.currentTimeMillis() > time || ult.getUs5() < 30 || ult.getUs6() < 30)
		{
			state = RUN_FROM_OBSTACLE_RIGHT_BACK;
			counter++;
			if(ult.getUs5() < 30 || ult.getUs6() < 30)
			{
				state = RUN_FROM_OBSTACLE_4;
			}
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void runFromObstacle4(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{		
		state = SEARCH_CAN;
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchTrash(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(Math.random() < 0.2)
		{
			state = SEARCH_TRASH_RIGHT_1;
		}
		else
		{
			state = SEARCH_TRASH_LEFT_1;
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchTrashLeft1(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != SEARCH_TRASH_LEFT_1)
		{
			time = System.currentTimeMillis() + TURNING_TIME;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		
		if(System.currentTimeMillis() > time)
		{
			state = SEARCH_TRASH_LEFT_2;
		}
		
		Point trashPosition = cameraProcessor.getTrashPosition();
		
		if(trashPosition.x > 0)
		{
			state = GO_TO_TRASH_TURN;
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchTrashLeft2(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != SEARCH_TRASH_LEFT_2)
		{
			time = System.currentTimeMillis() + TURNING_TIME + 3500;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		
		if(System.currentTimeMillis() > time)
		{
			state = SEARCH_TRASH_LEFT_1;
		}
		
		Point trashPosition = cameraProcessor.getTrashPosition();
		
		if(trashPosition.x > 0)
		{
			state = GO_TO_TRASH_TURN;
		}
		
		checkObstacle(cameraProcessor, ult);
	}

	private void stateSearchTrashRight1(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != SEARCH_TRASH_RIGHT_1)
		{
			time = System.currentTimeMillis() + TURNING_TIME + System.currentTimeMillis()%1000;
		}
		
		setMotorsMovement(-DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		
		Point trashPosition = cameraProcessor.getTrashPosition();
		
		if(System.currentTimeMillis() > time)
		{
			state = SEARCH_TRASH_RIGHT_2;
		}
		
		if(trashPosition.x > 0)
		{
			state = GO_TO_TRASH_TURN;
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateSearchTrashRight2(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != SEARCH_TRASH_RIGHT_2)
		{
			time = System.currentTimeMillis() + TURNING_TIME + 3500 + System.currentTimeMillis()%1000;
		}
		
		setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);
		
		Point trashPosition = cameraProcessor.getTrashPosition();
		
		if(System.currentTimeMillis() > time)
		{
			state = SEARCH_TRASH_RIGHT_1;
		}
		
		if(trashPosition.x > 0)
		{
			state = GO_TO_TRASH_TURN;
		}
		
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateGoToTrashTurn(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != GO_TO_TRASH_TURN)
		{
			time = System.currentTimeMillis() + SEARCHING_TIME;
		}
		
		Point trashPosition = cameraProcessor.getTrashPosition();
		
		int width = cameraProcessor.getFrameWidth();		
		int robotCenter = width/2 + (int)(ROBOT_CENTER_OFFSET*width);		
		double error = 0;
		
		if(trashPosition.x > 0)
		{
			error = ((trashPosition.x) - robotCenter)/(double)width;
						
			if(error > 0.25)
			{
				setMotorsMovement(-DEFAULT_VELOCITY, DEFAULT_VELOCITY);
			}
			else if (error < 0.25)
			{
				setMotorsMovement(DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
			}
			else
			{
				state = GO_TO_TRASH_FORWARD;
			}
							
			setVassouraMovement(-100);
			
			if(System.currentTimeMillis() > time)
			{
				state = GO_TO_TRASH_FORWARD;
			}
			
			/*if(ult.getInfra() < 300)
			{
				state = GO_TO_TRASH_DEBOUNCE_ULTRASOUND;
//				setMotorsMovement(0, 0);
			}*/
			if(this.obstacleButton == 1)
			{
				state = GO_TO_TRASH_DEBOUNCE_BUTTON;
			}
		}
		else
		{
			state = SEARCH_TRASH;
			//setMotorsMovement(0, 0);
		}
							
		checkObstacle(cameraProcessor, ult);
	}
	
	private void stateGoToTrashForward(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{			
		if(lastState != GO_TO_TRASH_FORWARD)
		{
			time = System.currentTimeMillis() + SEARCHING_TIME;
		}
		
		Point trashPosition = cameraProcessor.getTrashPosition();
		
		if(trashPosition.x > 0)
		{
			setMotorsMovement(DEFAULT_VELOCITY, DEFAULT_VELOCITY);		
			setVassouraMovement(-100);
			
			if(System.currentTimeMillis() > time)
			{
				state = GO_TO_TRASH_FORWARD;
			}
			
			/*if(ult.getInfra() < 300)
			{
				state = GO_TO_TRASH_DEBOUNCE_ULTRASOUND;
//				setMotorsMovement(0, 0);
			}*/
			if(this.obstacleButton == 1)
			{
				state = GO_TO_TRASH_DEBOUNCE_BUTTON;
			}
		}
		else
		{
			state = SEARCH_TRASH;
			//setMotorsMovement(0, 0);
		}
							
		checkObstacle(cameraProcessor, ult);
	}
	
	/*private void stateGoToTrashDebounceUltrasound(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{		
		if(lastState != GO_TO_TRASH_DEBOUNCE_ULTRASOUND)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		setMotorsMovement( 0, 0);
		
		if(System.currentTimeMillis() < time)
		{			
			if(ult.getInfra() > 300)
			{
				state = GO_TO_TRASH_TURN;
			}	
		}
		else
		{
			state = OPEN_DEPOSIT;
		}	
	}*/
	private void stateGoToTrashDebounceButton(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{		
		if(lastState != GO_TO_TRASH_DEBOUNCE_BUTTON)
		{
			time = System.currentTimeMillis() + 1000;
		}
		
		setMotorsMovement( 0, 0);
		
		if(System.currentTimeMillis() < time)
		{			
			if(this.obstacleButton == 0)
			{
				state = GO_TO_TRASH_TURN;
			}	
		}
		else
		{
			state = OPEN_DEPOSIT;
		}	
	}
	
	private void stateOpenDeposit(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{		
		if(lastState != OPEN_DEPOSIT)
		{
			time = System.currentTimeMillis() + 5000;
		}
		
		setMotorsMovement(0, 0);
		setVassouraMovement(-100);
		setMotorDoor(-50);
		setVibrator(60);
		
		if(System.currentTimeMillis() > time)
		{
			state = BACKUP_FROM_TRASH;
			lastTrashTime = System.currentTimeMillis();
		}
	}
	
	private void stateBackupFromTrash(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{		
		if(lastState != BACKUP_FROM_TRASH)
		{
			time = System.currentTimeMillis() + 2000;
		}
		
		setMotorsMovement(-DEFAULT_VELOCITY, -DEFAULT_VELOCITY);
		setMotorDoor(-50);
		setVibrator(0);
		
		if(System.currentTimeMillis() > time)
		{
			state = CLOSE_DEPOSIT;
		}
	}
	
	private void stateCloseDeposit(CodigoAndroidActivity act, Accelerometer acc, Compass comp, UltraSound ult, CameraProcessor cameraProcessor) throws IOException
	{		
		if(lastState != CLOSE_DEPOSIT)
		{
			time = System.currentTimeMillis() + 250;
		}
		
		setMotorsMovement(0, 0);
		setMotorDoor(0);
		
		if(System.currentTimeMillis() > time)
		{
			state = SEARCH_CAN;
			lastTrashTime = System.currentTimeMillis();
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
				case GO_TO_CAN_TURN:
					stateGoToCanTurn(acc, comp, ult, cameraProcessor);
				break;
				case GO_TO_CAN_FORWARD:
					stateGoToCanForward(acc, comp, ult, cameraProcessor);
				break;
				case SEARCH_CAN:
					stateSearchCan(acc, comp, ult, cameraProcessor);
				break;
				case LEFT_1:
					stateLeft1(act, acc, comp, ult, cameraProcessor);
				break;
				case LEFT_2:
					stateLeft2(act, acc, comp, ult, cameraProcessor);
				break;
				case LEFT_3:
					stateLeft3(act, acc, comp, ult, cameraProcessor);
				break;
				case LEFT_4:
					stateLeft4(act, acc, comp, ult, cameraProcessor);
				break;
				case RIGHT_1:
					stateRight1(act, acc, comp, ult, cameraProcessor);
				break;
				case RIGHT_2:
					stateRight2(act, acc, comp, ult, cameraProcessor);
				break;
				case RIGHT_3:
					stateRight3(act, acc, comp, ult, cameraProcessor);
				break;
				case RIGHT_4:
					stateRight4(act, acc, comp, ult, cameraProcessor);
				break;
				case FORWARD:
					stateForward(act, acc, comp, ult, cameraProcessor);
				case RUN_FROM_OBSTACLE_0:
					runFromObstacle0(act, acc, comp, ult, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_1:
					runFromObstacle1(act, acc, comp, ult, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_LEFT_BACK:
					runFromObstacleLeftBack(act, acc, comp, ult, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_LEFT_TURN:
					runFromObstacleLeftTurn(act, acc, comp, ult, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_RIGHT_BACK:
					runFromObstacleRightBack(act, acc, comp, ult, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_RIGHT_TURN:
					runFromObstacleRightTurn(act, acc, comp, ult, cameraProcessor);
				break;
				case RUN_FROM_OBSTACLE_4:
					runFromObstacle4(act, acc, comp, ult, cameraProcessor);
				break;
				case SEARCH_TRASH:
					stateSearchTrash(act, acc, comp, ult, cameraProcessor);
				break;
				case SEARCH_TRASH_LEFT_1:
					stateSearchTrashLeft1(act, acc, comp, ult, cameraProcessor);
				break;
				case SEARCH_TRASH_LEFT_2:
					stateSearchTrashLeft2(act, acc, comp, ult, cameraProcessor);
				break;
				case SEARCH_TRASH_RIGHT_1:
					stateSearchTrashRight1(act, acc, comp, ult, cameraProcessor);
				break;
				case SEARCH_TRASH_RIGHT_2:
					stateSearchTrashRight2(act, acc, comp, ult, cameraProcessor);
				break;
				case GO_TO_TRASH_TURN:
					stateGoToTrashTurn(act, acc, comp, ult, cameraProcessor);
				break;
				case GO_TO_TRASH_FORWARD:
					stateGoToTrashForward(act, acc, comp, ult, cameraProcessor);
				break;
				case GO_TO_TRASH_DEBOUNCE_BUTTON:
					stateGoToTrashDebounceButton(act, acc, comp, ult, cameraProcessor);
				break;
				case OPEN_DEPOSIT:
					stateOpenDeposit(act, acc, comp, ult, cameraProcessor);
				break;
				case BACKUP_FROM_TRASH:
					stateBackupFromTrash(act, acc, comp, ult, cameraProcessor);
				break;
				case CLOSE_DEPOSIT:
					stateCloseDeposit(act, acc, comp, ult, cameraProcessor);
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
	
	public void obstacleButtonPressed(int state)
	{
		this.obstacleButton = state;
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
			
			lastTrashTime = System.currentTimeMillis();
		}
		else if (state == STOP)
		{
			state = FORWARD;
			
		}
		else
		{
			state = STOP;	
		}
	}
}