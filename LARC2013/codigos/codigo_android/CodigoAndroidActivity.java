package erus.android.erusbot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class CodigoAndroidActivity extends Activity implements Runnable
{
	private static final String TAG = "CodigoAndroidActivity";

	//private ServerSocket serverSocketAndroid;
	private ServerSocket serverSocketPC;
	private ServerSocket serverSocketArduino;
	//private Connection connectionAndroid;
	private Connection connectionArduino;
	private Connection connectionPC;
	//private MessageAssembler androidMessages;
	private MessageAssembler arduinoMessages;
    private MessageAssembler pcMessages;
    
	
	private final int portPC = 18550;
	private final int portArduino = 4567;
	//private final int portAndroid = 19000;
		
	private ErusView erusView;
	private CameraProcessor cameraProcessor;
	
	private SensorManager mSM; 
	private Accelerometer mAccelerometer;
	private Compass mCompass;
	private Encoder mEncoder;
	private UltraSound mUltraSound;
	private ClawButton mClawButton;
	
	private RobotBrain robotBrain;
	
	private boolean connectToPC;
	private boolean connectToArduino;
	private boolean connectToAndroid;
	
	//private long lastCameraMsg;
	//private int lastFrameCount;
	
	private boolean running;
	
	public static byte[] toByta(int data) {
	    return new byte[] {
	        (byte)((data >> 24) & 0xff),
	        (byte)((data >> 16) & 0xff),
	        (byte)((data >> 8) & 0xff),
	        (byte)((data >> 0) & 0xff),
	    };
	}
	
	public static byte[] toByta(float data) {
	    return toByta(Float.floatToRawIntBits(data));
	}
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		cameraProcessor = new CameraProcessor(this);		
		
		mSM = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = new Accelerometer(mSM);
		mCompass = new Compass(mSM);
		mEncoder = new Encoder();
		mUltraSound = new UltraSound();
		mClawButton  = new ClawButton();
    }
    
    public void finishCreation()
    {
    	erusView = new ErusView(this);
    	
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.addView(erusView);
		layout.addView(cameraProcessor);
		
		erusView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
		cameraProcessor.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
		
		setContentView(layout);
				
		connectToPC = true;
		connectToArduino = true;
		connectToAndroid = false;

		//lastCameraMsg = System.currentTimeMillis();
		//lastFrameCount = -1;
		
		(new Thread(this)).start();
    }
    
    protected void onResume()
    {
	   super.onResume();
	
	   mAccelerometer.regListener(mSM);
	   mCompass.regListener(mSM);
	   
    }
    
    public void onStop()
    {
    	super.onStop();
    	
    	running = false;
    }
    
    private Connection acceptConnection(ServerSocket ss)
    {
    	Connection connection;
    	
    	try 
		{
    		ss.setSoTimeout(100);
			Socket socket = ss.accept();				
			connection = new Connection(socket);
		}
		catch (IOException e)
		{
		
			e.printStackTrace();
			connection = null;				
		}
    	
    	return connection;
    }
    
    private void sendSensorDataToPC(MessageAssembler pcMessages)
    {    	
    	if(this.connectToPC)
    	{
	    	try
	    	{	    		
	    		Log.i(TAG, "Sending Data to PC");		    		
	    		pcMessages.sendImageMessage(cameraProcessor.getFrameData(), cameraProcessor.getFrameWidth(), cameraProcessor.getFrameHeight());
	    		pcMessages.sendAccelerometerMessage(toByta(mAccelerometer.getX()), toByta(mAccelerometer.getY()), toByta(mAccelerometer.getZ()));
	    		pcMessages.sendCompassMessage(toByta(mCompass.getX()), toByta(mCompass.getY()), toByta(mCompass.getZ()));
	    		pcMessages.sendEncoderMessage(toByta(mEncoder.getEncRight()), toByta(mEncoder.getEncLeft()));
	    		pcMessages.sendUltraSoundMessage(toByta(mUltraSound.getUs1()), toByta(mUltraSound.getUs2()), toByta(mUltraSound.getUs3()), toByta(mUltraSound.getUs4()), toByta(mUltraSound.getUs5()), toByta(mUltraSound.getUs6()));
	    		//initRobot();
	    	}
	    	catch (IOException e)
			{
	    		e.printStackTrace();
			}
    	}
    	else
    	{
    		Log.i(TAG, "Can't send data to PC, connectToPC = false");
    	}
    }
    
    private void stopConnections()
    {
    	try 
    	{    		
			if(connectToArduino)
			{
				connectionArduino.close();
			}
			
			if(connectToPC)
    		{
    			connectionPC.close();
    		}
			
			//if(connectToAndroid)
			//{
			//	connectionAndroid.close();
			//}
			
			serverSocketArduino.close();
			serverSocketPC.close();
			//serverSocketAndroid.close();
		} 
    	catch (IOException e) 
    	{
    		e.printStackTrace();
		}
    	
    }
    /*
    private void createColorFile()
    {
    	ColorCalibrator cc = new ColorCalibrator();
		Scalar[] colors = new Scalar[4];
		Scalar[] colorsRadius = new Scalar[4];
		double[] areas = new double[4];
		
		colors[0] = new Scalar(225, 0, 0, 0);
		colors[1] = new Scalar(225, 0, 0, 0);
		colors[2] = new Scalar(225, 0, 0, 0);
		colors[3] = new Scalar(225, 0, 0, 0);
		
		colorsRadius[0] = new Scalar(25, 50, 50, 0);
		colorsRadius[1] = new Scalar(25, 50, 50, 0);
		colorsRadius[2] = new Scalar(25, 50, 50, 0);
		colorsRadius[3] = new Scalar(25, 50, 50, 0);
		
		areas[0] = 0.1;
		areas[1] = 0.1;
		areas[2] = 0.1;
		areas[3] = 0.1;
		
		cc.writeCalibrationFile(colors, colorsRadius, areas);
		
    }
    */
    
    private void createSockets()
    {
    	try 
		{	
			serverSocketPC = new ServerSocket(portPC);			
			serverSocketArduino = new ServerSocket(portArduino);
			//serverSocketAndroid = new ServerSocket(portAndroid);
		}
		catch (IOException e) 
		{
			e.printStackTrace();			
		}	

    }
    
    private boolean createArduinoMessageAssembler()
    {    	
    	if(connectToArduino)
		{
    		connectionArduino = acceptConnection(serverSocketArduino);
    			
    		if(connectionArduino != null) // This function may return without the message assembler created
    		{    		
    			erusView.setArduinoConnected(true);
    			arduinoMessages = new MessageAssembler(connectionArduino);
    			return true;
    		}			
		}
    	
    	return false;
    }
    
    private boolean createPCMessageAssembler()
    {
    	if(connectToPC)
		{
			connectionPC = acceptConnection(serverSocketPC);
			
			if(connectionPC != null) // This function may return without the message assembler created
			{
				erusView.setPCConnected(true);
				pcMessages = new MessageAssembler(connectionPC);
				return true;
			}
		}
    	
    	return false;
    }
    
    private boolean createAndroidMessageAssembler()
    {
    	if(connectToAndroid)
		{/*
			connectionAndroid = acceptConnection(serverSocketAndroid);
			
			if(connectionAndroid != null) // This function may return without the message assembler created
			{
				erusView.setAndroidConnected(true);
				androidMessages = new MessageAssembler(connectionAndroid);
				return true;
			}*/
		}
    	
    	return false;
    }
    
    public void reconectArduino()
    {    
    	pcPrint("Reconectando");
    	
    	try
		{
    		serverSocketArduino.close();
    	    connectionArduino.close();
    		
			serverSocketArduino = new ServerSocket(portArduino);
			
			while(true)
			{
				if(createArduinoMessageAssembler())
				{
					robotBrain.setArduinoConnection(connectionArduino);
					break;
				}
			}
		} 
		catch (IOException e)
		{
			pcPrint(e.toString());
		}
    }
    
    private void listenConnections()
    {
    	if(connectToArduino)
		{
			arduinoMessages.listenConnection(this);
			
			if(arduinoMessages.isDisconnected())
			{
				reconectArduino();
			}
		}
			
		if(connectToPC)
		{
			pcMessages.listenConnection(this);
		}
		
		/*if(connectToAndroid)
		{
			androidMessages.listenConnection(this);
		}*/
    }
    
    private void handlePCMessages()
    {		
		while(connectToPC && pcMessages.messagesAvailable())
		{
			byte[] msg = pcMessages.getNextMessage();
			
			if(isArduinoDestination(msg))
			{
				try 
				{
					if(connectToArduino)
					{
						//pcPrint(msg[0]+"");
						connectionArduino.sendMessage(msg, 0, msg.length);
					}	
				}
				catch (IOException e) 
				{
					pcPrint(e.toString());
					pcPrint("Activity");
					this.reconectArduino();
				}
			}
			else
			{
				handlePCMessage(msg);
			}
		}	
    }
    
    private boolean isArduinoDestination(byte[] msg)
    {
    	if (msg[0] == 0x11 || msg[0] == 0x12 || msg[0] == 0x13 || msg[0] == 0x14)
    	{	
    		return true; 	
    	}
    	else
    	{	
    		return false;
    	}
    }
    
    private void handlePCMessage(byte[] msg)
    {
    	switch(msg[0])
		{
			case Protocol.REQUEST_IMAGE:
			{
				sendSensorDataToPC(pcMessages);
			}break;
			
			case Protocol.REQUEST_IMAGE_BACK:
			{
				if(connectToAndroid)
				{
					//pcPrint("Sending back image request");
					/*
					try {
						connectionAndroid.sendMessage(msg, 0, msg.length);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				}
			}break;
			
			case Protocol.IMG_CALIB_DISK:
			{
				setImgCalibrationDisk(msg);
			}break;
			
			case Protocol.IMG_CALIB_MEM:
			{
				setImgCalibrationMem(msg);
				/*try {
					connectionAndroid.sendMessage(msg, 0, 49);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}break;
		}    	
    }
    
    private void handleArduinoMessages()
    {		
    	while(connectToArduino && arduinoMessages.messagesAvailable())
		{
			byte[] msg = arduinoMessages.getNextMessage();
						
			handleArduinoMessage(msg);
		}			
    }
    
    private void handleArduinoMessage(byte[] msg)
    {
    	//pcPrint(msg[0]+0+"");
    	
    	switch(msg[0])
    	{
	    	case Protocol.ENCODER:
			{
				mEncoder.refresh(msg);
				erusView.setEncoder(mEncoder.getEncRight(), mEncoder.getEncLeft());
			}break;
			
	    	case Protocol.ULTRASOUND:
			{
				mUltraSound.refresh(msg);
				erusView.setUltraSound(mUltraSound.getUs1(), mUltraSound.getUs2(), mUltraSound.getUs3(), mUltraSound.getUs4(), mUltraSound.getUs5(), mUltraSound.getUs6());
				//pcPrint(mUltraSound.getUs1() + " " + mUltraSound.getUs2()  + " " +  mUltraSound.getUs3()  + " " +  mUltraSound.getUs4() + " " +  mUltraSound.getUs5() + " " +  mUltraSound.getUs6());
			}break;
			
	    	case Protocol.BUTTON_START:
	    	{
	    		robotBrain.startButtonPressed();
	    	}break;
	    	
	    	case Protocol.BUTTON_STOP:
	    	{
	    		
	    	}break;
	    	
	    	case Protocol.CLAW_BUTTON:
	    	{
	    		mClawButton.refresh(msg);
	    	}break;

    	}    	
    }
    
    private void handleAndroidMessages()
    {	/*	
    	while(connectToAndroid && androidMessages.messagesAvailable())
		{
			byte[] msg = androidMessages.getNextMessage();
						
			handleAndroidMessage(msg);
		}			
		*/
    }
    
    private void handleAndroidMessage(byte[] msg)
    {
    	//pcPrint("Adroid msg: " + msg[0]+0);
    	
    	switch(msg[0])
    	{
	    	case Protocol.TRASH_POSITION:
			{
				Trash trash = new Trash();
				ByteBuffer bb = ByteBuffer.allocate(4);
				bb.put(msg, 1, 4);
				bb.rewind();
				trash.size = bb.getInt(0);
				bb.rewind();
				bb.put(msg, 4, 4);
				bb.rewind();
				trash.minY = bb.getInt(0);
				bb.rewind();
				bb.put(msg, 8, 4);
				bb.rewind();
				trash.position = new Point();				
				trash.position.x = bb.getFloat(0);				
				bb.rewind();
				bb.put(msg, 12, 4);				
				bb.rewind();
				trash.position.y = bb.getFloat(0);				
				robotBrain.setTrashPosition(trash);
				
				//pcPrint("Trash size: "+trash.size + " minY: " + trash.minY + " x: " + trash.position.x + " y: " + trash.position.y);
			}break;
			
	    	case Protocol.CAMERA_MESSAGE:
	    	{
	    		if(connectToPC)
	    		{
	    			try {
						this.connectionPC.sendMessage(msg, 0, msg.length);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}	    		
	    	}break;
    	}    	
    }
    
    private void initColors()
    {
    	byte[] color = new byte[49];
		mountColor(color);
		erusView.setColor(color);
    }
    
    private void sendInitialDataToPC()
    {
    	if(connectToPC)
		{
    		byte[] color = new byte[49];
    		mountColor(color);
			sendColor2PC(connectionPC, color);
		}
    }
    
    private void sendInitialDataToAndroid()
    {/*
    	if(connectToAndroid)
    	{
    		byte[] color = new byte[49];
    		mountColor(color);    		
    		color[0]=Protocol.IMG_CALIB_MEM;
    		try {
    			connectionAndroid.sendMessage(color, 0, 49);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}*/
    }
    
    private boolean checkStartButton()
    {
    	while(connectToArduino && arduinoMessages.messagesAvailable())
		{
			byte[] msg = arduinoMessages.getNextMessage();
						
			if(msg[0] == Protocol.BUTTON_START)
			{
				return true;
			}
		}
    	
    	return false;
    }
    
    private void waitForButtonOrPCConnection()
    {    	
    	if(pcMessages != null)
    	{
    		initRobotBrain();
    		return;
    	}
    	
    	while(true)
    	{
    		if(connectToPC)
    		{    		
    			if(createPCMessageAssembler())
    			{    		
    				initRobotBrain();
    				return;
    			}
    		}
        	
        	if(connectToArduino)
        	{
        		arduinoMessages.listenConnection(this);
        		
        		
        		if(checkStartButton())
        		{        	
        			initRobotBrain();
        			robotBrain.startButtonPressed();
        			connectToPC = false;
        			return;
        		}
        	}
    	}    	
    }
    
    private void waitForArduinoOrPCConnection()
    {    	
    	while(true)
    	{
    		if(connectToPC)
    		{    		
    			if(createPCMessageAssembler())
    			{   
    				connectToArduino = false;
    				return;    				
    			}
    		}
        	
        	if(connectToArduino)
        	{
        		
        		if(createArduinoMessageAssembler())
    			{    				
    				return;
    			}        		
        	}
    	}   
    }
    
    private void waitForAndroidConnection()
    {    	
    	if(connectToAndroid)
		{		
	    	while(true)
	    	{
    			if(createAndroidMessageAssembler())
    			{   
    				return;    				
    			}
    		}
		}
    }
    
    private void initRobotBrain()
    {
    	robotBrain = new RobotBrain(connectionArduino, connectionPC, erusView);
    }
    
    private void initRobot()
    {
    	initColors();
    	
    	createSockets(); 
    	//waitForAndroidConnection();
    	waitForArduinoOrPCConnection();    	
    	waitForButtonOrPCConnection();
    	    	    	
		sendInitialDataToPC();
		sendInitialDataToAndroid();
    }
    
    private void waitInitialization()
    {
    	while(!cameraProcessor.getStatusSurfaceCreate())
		{
			try
			{
				Thread.sleep(10);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();				
			}
		}
    }
    
	public void run()
	{ 			
		waitInitialization();
				
		running = true;
		
		initRobot();
				
		while(running)
		{ 			
			listenConnections();
									
			handlePCMessages();
			handleArduinoMessages();
			handleAndroidMessages();
			
			robotBrain.process(this, mAccelerometer, mCompass, mEncoder, mUltraSound,mClawButton, cameraProcessor);
			
			//erusView.setTeste(true);
		}	
		
		stopConnections();
	
	}
	
	void setImgCalibrationDisk(byte[] msg)
	{		
		Scalar[] colors1 = new Scalar[4];
		Scalar[] colorsRadius1 = new Scalar[4];
		double[] minContourAreas1 = new double[4];
		ByteBuffer bb = ByteBuffer.allocate(4);
		for(int i = 0; i < 4; i++)
		{
			colors1[i] = new Scalar(msg[1+i*12] & 0xFF,msg[2+i*12] & 0xFF,msg[3+i*12] & 0xFF,msg[4+i*12] & 0xFF);
			colorsRadius1[i] = new Scalar(msg[5+i*12] & 0xFF,msg[6+i*12] & 0xFF,msg[7+i*12] & 0xFF,msg[8+i*12] & 0xFF);
			bb.rewind();
			bb.put(msg, 9+i*12, 4);
			bb.rewind();
			minContourAreas1[i] = (double)bb.getFloat();
		}
		ColorCalibrator c1 = new ColorCalibrator();
		c1.writeCalibrationFile(colors1, colorsRadius1, minContourAreas1);
	}
	
	void setImgCalibrationMem(byte[] msg)
	{
		Scalar[] colors1 = new Scalar[4];
		Scalar[] colorsRadius1 = new Scalar[4];
		double[] minContourAreas1 = new double[4];
		ByteBuffer bb = ByteBuffer.allocate(4);
		for(int i = 0; i < 4; i++)
		{
			colors1[i] = new Scalar(msg[1+i*12] & 0xFF,msg[2+i*12] & 0xFF,msg[3+i*12] & 0xFF,msg[4+i*12] & 0xFF);
			colorsRadius1[i] = new Scalar(msg[5+i*12] & 0xFF,msg[6+i*12] & 0xFF,msg[7+i*12] & 0xFF,msg[8+i*12] & 0xFF); 
			bb.rewind();
			bb.put(msg, 9+i*12, 4);
			bb.rewind();
			minContourAreas1[i] = (double)bb.getFloat();
		}
		cameraProcessor.setRGBColor(colors1, colorsRadius1, minContourAreas1);
	}
	
	public void mountColor(byte color[])
	{
//		byte color[] = new byte[49];
		ByteBuffer bb = ByteBuffer.allocate(4);
		Scalar[] colors=cameraProcessor.getColors();
		Scalar[] colorsRadius=cameraProcessor.getColorsRadius();
		double[] minContourAreas=cameraProcessor.getMinContourAreas();
		
		color[0] = Protocol.IMG_CALIB_CONF_AND;
		
		for(int i=0;i < ColorCalibrator.NUM_COLORS;i++)
		{
			for(int j=0; j < 4;j++)
			{
				color[j+1+i*12]=(byte)colors[i].val[j];
				color[j+5+i*12]=(byte)colorsRadius[i].val[j];
			}
			bb.rewind();
			bb.putFloat((float) minContourAreas[i]);
			for(int k = 0; k < 4; k++)
			{
				bb.rewind();
				color[i*12+k+9]=bb.get(k);
			}
		}
	}
	
	public void sendColor2PC(Connection connectionPC, byte[] color)
	{
/*		byte color[] = new byte[49];
		ByteBuffer bb = ByteBuffer.allocate(4);
		Scalar[] colors=cameraProcessor.getColors();
		Scalar[] colorsRadius=cameraProcessor.getColorsRadius();
		double[] minContourAreas=cameraProcessor.getMinContourAreas();
		
		color[0] = Protocol.IMG_CALIB_CONF_AND;
		
		for(int i=0;i < ColorCalibrator.NUM_COLORS;i++)
		{
			for(int j=0; j < 4;j++)
			{
				color[j+1+i*12]=(byte)colors[i].val[j];
				color[j+5+i*12]=(byte)colorsRadius[i].val[j];
			}
			bb.rewind();
			bb.putFloat((float) minContourAreas[i]);
			for(int k = 0; k < 4; k++)
			{
				bb.rewind();
				color[i*12+k+9]=bb.get(k);
			}
		}*/
		try {
			connectionPC.sendMessage(color, 0, 49);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			pcPrint(e.toString());
		}
	}
	
	@SuppressWarnings("unused")
	public void pcPrint(String str)
	{
		if(connectionPC != null)
		{
			byte[] byteStr = str.getBytes();
			byte[] head = new byte[2];
			head[0] = 0x65;
			head[1] = (byte) byteStr.length;
						
			try {
				connectionPC.sendMessage(head, 0, 2);
				connectionPC.sendMessage(byteStr, 0, byteStr.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
}
