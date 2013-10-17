import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.swing.*;


public class Fiscal extends JFrame implements KeyListener, MouseListener, Runnable
{
	private Connection connection;
	private MessageAssembler msgAssembler;
	

	private final String IP_ANDROID = "192.168.0.115";
	//private final String IP_ANDROID = "192.168.43.1";	//galaxy
	private final int PORT_ANDROID = 18550;
	
	/*private final byte fr1[] = {0x10,(byte) 0xFF,0};
	private final byte fr2[] = {0x11,(byte) 0xFF,1};	
	private final byte tr1[] = {0x10,(byte) 0xFF,1};
	private final String IP_ANDROID = "192.168.0.115
	private final byte tr2[] = {0x11,(byte) 0xFF,0};
	private final byte esq1[] = {0x10,(byte) 0xFF,0};
	private final byte esq2[] = {0x11,(byte) 0xFF,0};
	private final byte dir1[] = {0x10,(byte) 0xFF,1};
	private final byte dir2[] = {0x11,(byte) 0xFF,1};*/
	private final byte forward[] = {0x11,(byte) 255, 1, 0x12, (byte) 255, 1};
	private final byte backward[] = {0x11,(byte) 255, 0,0x12, (byte) 255, 0};
	private final byte right[] = {0x11,(byte) 255, 0, 0x12, (byte) 255, 1};
	private final byte left[] = {0x11,(byte) 255, 1, 0x12, (byte) 255, 0};
	
	
	private final byte stop1[] = {0x11,0,0,0x12,0,0};
	private final byte stop2[] = {0x11,0,0,0x12,0,0};
	private final byte reqImg[] = {0x61};
	
	private final byte ligaVassoura[] = {0x13,(byte)230,1};
	private final byte desligaVassoura[] = {0x13,0,1};

	private final byte openDeposit[] = {0x14,(byte) 77};
	private final byte closeDeposit[] = {0x14,(byte) 90};
	private final byte offDeposit[] = {0x14,(byte) 90};
	
	private byte imgCalibration[];
	
	private int[] direction;
	private int[] lastDirection;
	
	private int stopping;
	private long stop_time;
	
	private BufferedImage graphicsBuffer;
	private BufferedImage cameraImage;

	private float[] Accelerometer = {0, 0, 0};
	private float[] Compass = {0, 0, 0};
	private int[] Encoder = {0, 0};
	private int[] UltraSound = {0, 0, 0};
	
	private boolean lockRequestImage = false;
	private boolean writeDisk = false;
	private boolean writeMemory = false;
	
	private boolean screen;
	
	private int xClick;
	private int yClick;
	
	private boolean closedClaw = true;
	private boolean openedClaw = false;
	
	private boolean highClaw = true;
	private boolean lowClaw = false;
	
	private boolean openedDeposit = false;
	private boolean closedDeposit = true;
	
	private boolean vassouraRodando = false;
	
	Fiscal()
	{		
		super("Fiscal");
		
		screen = true;
		
		//JFrame f = new JFrame("A JFrame");
		
		imgCalibration = new byte[49];
		
		connection = null;
		
		direction = new int[4];
		lastDirection = new int[4];
		
		for(int i=0; i<4; i++)
		{
			direction[i] = 0;
			lastDirection[i] = 0;
		}
		
		stopping = 0;
		stop_time = 0;
		
		graphicsBuffer = new BufferedImage(800, 400, BufferedImage.TYPE_INT_ARGB);
		cameraImage = null;
	}
	
	public static byte[] toByta(int data) 
	{
	    return new byte[] {
	        (byte)((data >> 24) & 0xff),
	        (byte)((data >> 16) & 0xff),
	        (byte)((data >> 8) & 0xff),
	        (byte)((data >> 0) & 0xff),
	    };
	}
	
	public static byte[] toByta(float data) 
	{
	    return toByta(Float.floatToRawIntBits(data));
	}

	public void connectToAndroid()
	{		
		try
		{
			connection = new Connection(IP_ANDROID, PORT_ANDROID);
			msgAssembler = new MessageAssembler(connection);
		}
		catch (IOException e)
		{
			System.out.println("Couldn't connect to android.");
			e.printStackTrace();
		}
		
	}
	
	public boolean isConnected()
	{
		if(connection == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	private void drawCameraImage(Graphics g)
	{
		if(cameraImage != null)
		{
			g.drawImage(cameraImage, 400, 0, 400, 400, null);
			
			int x = xClick-400;
			int y = yClick;
			
			x = x*cameraImage.getWidth()/400;
			y = y*cameraImage.getHeight()/400;
			
			if(x >= 0 && x < cameraImage.getWidth() && y >= 0 && y < cameraImage.getHeight())
			{				
				int rgb = cameraImage.getRGB(x, y);
				Font f = new Font("arial", Font.PLAIN, 14);
				g.setFont(f);				
				g.setColor(Color.BLACK);
				String colorString = (rgb >> 16 & 0x000000FF) + "-" + (rgb >> 8 & 0x000000FF) + "-" + (rgb & 0x000000FF);
				g.drawString(colorString, 200, 40);
			}
		}
	}
	
	public void paint(Graphics gBad)
	{
		Graphics g = graphicsBuffer.getGraphics();
		
		g.setColor(Color.WHITE);
		
		g.fillRect(0, 0, 400, 400);
		
		drawCameraImage(g);
		
				
		g.setColor(Color.BLACK);
		Font f = new Font("arial", Font.PLAIN, 14);
		g.setFont(f);
		
		if(connection == null)
		{
			g.drawString("Waiting for connection", 20, 60);
			
			g.setColor(Color.RED);			
			g.fillRect(100, 100, 100, 100);
			g.drawString("Accelerometer: "+Accelerometer[0]+" ,"+Accelerometer[1]+" ,"+Accelerometer[2], 20, 240);
			g.drawString("Compass: "+(57.3 * Math.atan2(Compass[1], Compass[2])), 20, 260);
			g.drawString("Encoder: "+Encoder[0]+" ,"+Encoder[1], 20, 280);
			g.drawString("Ultrassound: "+UltraSound[0]+" ,"+UltraSound[1]+" ,"+UltraSound[2], 20, 300);
			
			g.drawString("Cor 1:", 20, 340);
			g.drawString("Cor 2:", 20, 360);
			g.drawString("Cor 3:", 20, 380);
			g.drawString("Cor 4:", 20, 400);
		}
		else
		{
			g.drawString("Connected", 20, 60);
			
			g.setColor(Color.GREEN);			
			g.fillRect(100, 100, 100, 100);
			
			g.setColor(Color.BLACK);
			g.drawString("Accelerometer: "+Accelerometer[0]+" ,"+Accelerometer[1]+" ,"+Accelerometer[2], 20, 240);
			g.drawString("Compass: "+Compass[0]+" ,"+Compass[1]+" ,"+Compass[2]+" ,"+(57.3 * Math.atan2(Compass[1], Compass[2])), 20, 260);
			g.drawString("Encoder: "+Encoder[0]+" ,"+Encoder[1], 20, 280);
			g.drawString("Ultrassound: "+UltraSound[0]+" ,"+UltraSound[1]+" ,"+UltraSound[2], 20, 300);
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.put(imgCalibration, 9, 4);
			bb.rewind();
			g.drawString("Cor 1:"+"("+(imgCalibration[1]&0xFF)+"|"+(imgCalibration[2]&0xFF)+"|"+(imgCalibration[3]&0xFF)+"|"+(imgCalibration[4]&0xFF)+") ("+(imgCalibration[5]&0xFF)+"|"+(imgCalibration[6]&0xFF)+"|"+(imgCalibration[7]&0xFF)+"|"+(imgCalibration[8]&0xFF)+") ("+bb.getFloat()+")", 20, 320);
			g.setColor(new Color(imgCalibration[1]&0xFF, imgCalibration[2]&0xFF, imgCalibration[3]&0xFF));			
			g.fillRect(300, 306, 16, 16);
			g.setColor(Color.BLACK);
			g.drawRect(300, 306, 16, 16);
			
			bb.rewind();
			bb.put(imgCalibration, 21, 4);
			bb.rewind();
			g.drawString("Cor 2:"+"("+(imgCalibration[13]&0xFF)+"|"+(imgCalibration[14]&0xFF)+"|"+(imgCalibration[15]&0xFF)+"|"+(imgCalibration[16]&0xFF)+") ("+(imgCalibration[17]&0xFF)+"|"+(imgCalibration[18]&0xFF)+"|"+(imgCalibration[19]&0xFF)+"|"+(imgCalibration[20]&0xFF)+") ("+bb.getFloat()+")", 20, 340);
			g.setColor(new Color(imgCalibration[13]&0xFF, imgCalibration[14]&0xFF, imgCalibration[15]&0xFF));			
			g.fillRect(300, 326, 16, 16);
			g.setColor(Color.BLACK);
			g.drawRect(300, 326, 16, 16);
			
			bb.rewind();
			bb.put(imgCalibration, 33, 4);
			bb.rewind();
			g.drawString("Cor 3:"+"("+(imgCalibration[25]&0xFF)+"|"+(imgCalibration[26]&0xFF)+"|"+(imgCalibration[27]&0xFF)+"|"+(imgCalibration[28]&0xFF)+") ("+(imgCalibration[29]&0xFF)+"|"+(imgCalibration[30]&0xFF)+"|"+(imgCalibration[31]&0xFF)+"|"+(imgCalibration[32]&0xFF)+") ("+bb.getFloat()+")", 20, 360);
			g.setColor(new Color(imgCalibration[25]&0xFF, imgCalibration[26]&0xFF, imgCalibration[27]&0xFF));			
			g.fillRect(300, 346, 16, 16);
			g.setColor(Color.BLACK);
			g.drawRect(300, 346, 16, 16);
			
			bb.rewind();
			bb.put(imgCalibration, 45, 4);
			bb.rewind();
			g.drawString("Cor 4:"+"("+(imgCalibration[37]&0xFF)+"|"+(imgCalibration[38]&0xFF)+"|"+(imgCalibration[39]&0xFF)+"|"+(imgCalibration[40]&0xFF)+") ("+(imgCalibration[41]&0xFF)+"|"+(imgCalibration[42]&0xFF)+"|"+(imgCalibration[43]&0xFF)+"|"+(imgCalibration[44]&0xFF)+") ("+bb.getFloat()+")", 20, 380);
			g.setColor(new Color(imgCalibration[37]&0xFF, imgCalibration[38]&0xFF, imgCalibration[39]&0xFF));			
			g.fillRect(300, 366, 16, 16);
			g.setColor(Color.BLACK);
			g.drawRect(300, 366, 16, 16);
		}
		
		gBad.drawImage(graphicsBuffer, 0, 0, 800, 400, null);
		
	}
	
	public void start()
	{
		new Thread(this).start();
		new Thread(this).start();
	}
	
	public synchronized void sendMovementMessage()
	{				
		try 
		{
			if(direction[0] == 1 && lastDirection[0] == 0)
			{
				//connection.sendMessage(esq1, 0, 3);
				//connection.sendMessage(esq2, 0, 3);
				connection.sendMessage(left, 0, 6);
				stopping = 0;
				
				//System.out.println("1 "+System.currentTimeMillis());
			}
			else if(direction[1] == 1 && lastDirection[1] == 0)
			{
				//connection.sendMessage(fr1, 0, 3);
				//connection.sendMessage(fr2, 0, 3);
				connection.sendMessage(forward, 0, 6);
				stopping = 0;
				
				//System.out.println("2 "+System.currentTimeMillis());
			}
			else if(direction[2] == 1 && lastDirection[2] == 0)
			{
				//connection.sendMessage(dir1, 0, 3);
				//connection.sendMessage(dir2, 0, 3);				
				connection.sendMessage(right, 0, 6);
				stopping = 0;
				
				//System.out.println("3 "+System.currentTimeMillis());
			}
			else if(direction[3] == 1 && lastDirection[3] == 0)
			{				
				//connection.sendMessage(tr1, 0, 3);
				//connection.sendMessage(tr2, 0, 3);
				connection.sendMessage(backward, 0, 6);
				stopping = 0;
				
				//System.out.println("4 "+System.currentTimeMillis());
			}
			else if(direction[0] == 0 && direction[1] == 0 &&
					direction[2] == 0 && direction[3] == 0)					
			{
				if(stopping == 0)
				{
					stopping = 1;
					stop_time = System.currentTimeMillis();
				}
				else if(stopping == 1 && System.currentTimeMillis() - stop_time > 10)
				{
					stopping = 2;
					connection.sendMessage(stop1, 0, 6);
					connection.sendMessage(stop2, 0, 6);				
				
					//System.out.println("5 "+System.currentTimeMillis());
				}
							
			}
			else
			{
				stopping = 0;	// Fuck you Java, fuck you
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}	
		
		for(int i=0; i<4; i++)
		{
			lastDirection[i] = direction[i];
		}
	}
	
	private BufferedImage retrieveImage(byte[] cameraMsg)
	{
		int code = cameraMsg[0] & 0xFF;
		
		if(code != 0x60)
		{
			System.out.println("This message is not a camera msg!");
		}
		
		ByteBuffer bb = ByteBuffer.allocate(12);	
		bb.put(cameraMsg, 1, 12);
		
		bb.rewind();
		
		int width = bb.getInt();
		int height = bb.getInt();
		//int dataLength = bb.getInt();
		
        int frameSize = width * height;                        
        int[] rgba = new int[frameSize];
        
        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++) 
            {
            	int index = i * width + j;
            	
            	int r = cameraMsg[3 * index + 2 + 13];
            	int g = cameraMsg[3 * index + 1 + 13];
            	int b = cameraMsg[3 * index + 0 + 13];
            	
            	rgba[index] = 0xff000000 + (b << 16) + (g << 8) + r;
            }            
        }
       
        /*
        boolean grayscale = false;
        if (grayscale) 
        {
            for (int i = 0; i < frameSize; i++) 
            {
                int y = (0xff & ((int) cameraMsg[i + 13]));
                rgba[i] = 0xff000000 + (y << 16) + (y << 8) + y;
            }
        } 
        else
        {
            for (int i = 0; i < height; i++)
            {
                for (int j = 0; j < width; j++) 
                {
                	int index = i * width + j;
                	int supply_index = frameSize + (i >> 1) * width + (j & ~1);
                    int y = (0xff & ((int) cameraMsg[index + 13]));
                    int u = (0xff & ((int) cameraMsg[supply_index + 0 + 13]));
                    int v = (0xff & ((int) cameraMsg[supply_index + 1 + 13]));
                    y = y < 16 ? 16 : y;
                    
                    float y_conv = 1.164f * (y - 16);
                    int r = Math.round(y_conv + 1.596f * (v - 128));
                    int g = Math.round(y_conv - 0.813f * (v - 128) - 0.391f * (u - 128));
                    int b = Math.round(y_conv + 2.018f * (u - 128));

                    r = r < 0 ? 0 : (r > 255 ? 255 : r);
                    g = g < 0 ? 0 : (g > 255 ? 255 : g);
                    b = b < 0 ? 0 : (b > 255 ? 255 : b);

                    rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
                }
            }
        }
        */
        		
        //BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //img.setRGB(0, 0, width, height, rgba, 0, width);
        
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, width, height, rgba, 0, width);      
		
        return img;
	}

	private float[] retrieveAccelerometer(byte[] AccelerometerMsg)
	{
		//int code = AccelerometerMsg[0] & 0xFF;
				
		ByteBuffer bb = ByteBuffer.allocate(12);
		
		bb.put(AccelerometerMsg, 1, 12);
		bb.rewind();
		float dataX = bb.getFloat();
		float dataY = bb.getFloat();
		float dataZ = bb.getFloat();
		
		float accel[] = {dataX, dataY, dataZ};
		return accel;
	}

	private float[] retrieveCompass(byte[] CompassMsg)
	{
		//int code = CompassMsg[0] & 0xFF;
		
		ByteBuffer bb = ByteBuffer.allocate(12);
		
		bb.put(CompassMsg, 1, 12);
		bb.rewind();
		float dataX = bb.getFloat();
		float dataY = bb.getFloat();
		float dataZ = bb.getFloat();
		
		float comp[] = {dataX, dataY, dataZ};
		return comp;
	}
	
	private int[] retrieveEncoder(byte[] EncoderMsg)
	{
		//int code = EncoderMsg[0] & 0xFF;
		
		ByteBuffer bb = ByteBuffer.allocate(8);
		
		bb.put(EncoderMsg, 1, 8);
		bb.rewind();
		int dataRight = bb.getInt();
		int dataLeft = bb.getInt();
		
		int comp[] = {dataRight, dataLeft};
		//System.out.println(dataRight+" "+dataLeft);
		return comp;
	}

	private int[] retrieveUltraSound(byte[] usMsg)
	{
		//int code = usMsg[0] & 0xFF;
		System.out.println(usMsg.length);
		ByteBuffer bb = ByteBuffer.allocate(24);
		
		bb.put(usMsg, 1, 12);
		bb.rewind();
		int data1 = bb.getInt();
		int data2 = bb.getInt();
		int data3 = bb.getInt();
		System.out.println(data1 + ", " + data2 + ", " + data3);
		
		int comp[] = {data1, data2, data3};
		//System.out.println(dataRight+" "+dataLeft);
		return comp;
	}
	
	private void printStringMessage(byte[] msg)
	{
		String str = new String(msg, 2, msg.length-2);
		System.out.println(str);		
	}
	
	private void processMessage(byte[] msg)
	{
		int code = msg[0] & 0xFF;
 		
		switch(code)
		{
			case 0x60:
			{
				cameraImage = retrieveImage(msg);
				this.repaint();
			}break;
			case 0x20:
			{
				Accelerometer = retrieveAccelerometer(msg);
			}break;
			case 0x21:
			{
				Compass = retrieveCompass(msg);
			}break;
			case 0x40:
			{
				Encoder = retrieveEncoder(msg);
			}break;
			case 0x31:
			{
				UltraSound = retrieveUltraSound(msg);
			}break;
			case 0x64:
			{
				setColorReceived(msg);
				System.out.println("Foi setado");
			}break;
			case 0x65:
			{
				printStringMessage(msg);
			}break;
		}
	}
	
	public void run() 
	{
		boolean sincScreen = false;
		
		synchronized (this)
		{
			if(screen)
			{
				screen = false;
				sincScreen  = true;
			}
		}
		
		if(sincScreen)
		{
			loopScreen();
		}
		else
		{
			loopTerminal();
		}
	} 
	
	public void loopTerminal()
	{
		Scanner scan = new Scanner(System.in);
		while(true)
		{			
			System.out.println("Digite o comando:");
			String lin = new String(scan.nextLine());
			System.out.println("["+lin+"]");
			if(lin.length() != 0)
			{
				char cmd = lin.charAt(0);
				switch(cmd)
				{
					case 's'://seta imgCalibration
					{
						System.out.println("Digite qual cor deve ser mudada:");
						int i,offset;
						offset = (scan.nextInt()-1)*12;
						ByteBuffer bb = ByteBuffer.allocate(4);
						System.out.println("Digite a cor [R G B a]:");
						for(i = 0; i < 4;i++)
							imgCalibration[1+i+offset] = (byte)scan.nextInt();
						System.out.println("Digite o raio da cor [H S V a]:");
						for(; i < 8;i++)
							imgCalibration[1+i+offset] = (byte)scan.nextInt();
						System.out.println("Digite o minContourArea:");
						bb.putFloat(scan.nextFloat());
						for(; i < 12; i++)
							imgCalibration[1+i+offset] = bb.get(i-8);
						this.repaint();
					}
						break;
				}
			}
		}
	}
	
	public void loopScreen()
	{
		while(true)
		{
			sendMovementMessage();
						
			msgAssembler.listenConnection();
							
			while(msgAssembler.messagesAvailable())
			{
				processMessage(msgAssembler.getNextMessage());
				
				/*
				byte[] msg = msgAssembler.getNextMessage();
				System.out.println("Msg length: "+msg.length);
				
				for(int i=0; i<16 && i<msg.length; i++)
				{
					System.out.print(((int)msg[i] & 0xFF) + " ");
				}
				System.out.println();
				*/
			}	
			
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public static void main(String[] args) 
	{
		Fiscal tela = new Fiscal();
		tela.setSize(800, 400);
		//tela.setLocation(300,200);
		//f.getContentPane().add(BorderLayout.CENTER, new JTextArea(10, 40));
		tela.addKeyListener(tela);
		tela.addMouseListener(tela);
		tela.setVisible(true);
		tela.connectToAndroid();
		tela.repaint();
		tela.start();
		
	}

	public synchronized void keyPressed(KeyEvent event)
	{		
		switch(event.getKeyCode())
		{
			case 37:
			{
				direction[0] = 1;
			}break;
			
			case 38:
			{
				direction[1] = 1;	
			}break;
			
			case 39:
			{
				direction[2] = 1;
			}break;
			
			case 40:
			{
				direction[3] = 1;
			}break;
			case 80://p
			{
				if(!lockRequestImage)
				{
					lockRequestImage = true;
					sendRequestReceiveImage();
				}
			}break;
			case 87://w -> write in disk
			{
				if(!writeDisk)
				{
					writeDisk = true;
					imgCalibration[0] = 0x62;
					sendRequestWriteDisk();
				}
			}break;
			case 83://s -> write in memory
			{
				if(!writeMemory)
				{
					writeMemory = true;
					imgCalibration[0] = 0x63;
					sendRequestWriteMemory();
				}
			}break;
			
			case 76://v -> liga ou desliga a vassoura
			{
				if(vassouraRodando)
				{
					vassouraRodando = false;
					sendRequestDesligaVassoura();
				} else {
					vassouraRodando = true;
					sendRequestLigaVassoura();
				}
				
			}break;
			
			case 69: // 'e'
			{
				if (openedDeposit)
				{
					openedDeposit = false;
					closedDeposit = true;
					sendRequestCloseDeposit();
				}
				
			}break;
			case 70: // 'f' 
			{
				if (closedDeposit)
				{
					openedDeposit = true;
					closedDeposit = false;
					sendRequestOpenDeposit();
				}
			}break;
			
			
		}
		
		//System.out.println(event.getKeyCode() + " " + event.getWhen());
	}

	public synchronized void keyReleased(KeyEvent event) 
	{	
		switch(event.getKeyCode())
		{
			case 37:
			{
				direction[0] = 0; 
			}break;
			
			case 38:
			{
				direction[1] = 0;
			}break;
			
			case 39:
			{
				direction[2] = 0;
			}break;
			
			case 40:
			{
				direction[3] = 0;
			}break;
			case 80://p
			{
				lockRequestImage = false;
			}break;
			case 87://w -> write in disk
			{
				writeDisk = false;
			}break;
			case 83://s -> write in memory
			{
				writeMemory = false;
			}break;
			case 69: // 'e'
			{
				//sendRequestOffDeposit();
				
			}break;
			case 70: // 'f' 
			{
				//sendRequestOffDeposit();
			}break;
			case 76: // 'v' 
			{
			//	vassoura
			}break;
			case 67: // 'c'
			{
			//	sendRequestStopClaw();
			}break;
			case 85: // 'u' 
			{
			//	sendRequestStopClaw2();	
			}break;
			
			case 68: // 'd'
			{
			//	sendRequestStopClaw2();
			}break;
		}			
			
		//System.out.println(event.getKeyCode() + "tete " + event.getWhen());
		
	}

	public void keyTyped(KeyEvent event) 
	{		
	}
	
	public void sendRequestReceiveImage()
	{
		try {
			connection.sendMessage(reqImg, 0, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequestWriteDisk()
	{
		try {
			connection.sendMessage(imgCalibration, 0, 49);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequestWriteMemory()
	{
		try {
			connection.sendMessage(imgCalibration, 0, 49);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void sendRequestOpenDeposit()
	{
		try {
			connection.sendMessage(openDeposit, 0, 2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequestLigaVassoura()
	{
		try {
			connection.sendMessage(ligaVassoura, 0, 3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequestDesligaVassoura()
	{
		try {
			connection.sendMessage(desligaVassoura, 0, 3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequestCloseDeposit()
	{
		try {
			connection.sendMessage(closeDeposit, 0, 2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendRequestOffDeposit()
	{
		try {
			connection.sendMessage(offDeposit, 0, 2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setColorReceived(byte[] msg)
	{
		//ByteBuffer bb = ByteBuffer.allocate(4);
		//bb.put(msg,1,48);
		
		for(int i = 0; i<49; i++)
		{
			imgCalibration[i] = msg[i]; 
		}
	}
	public void buildColor()
	{
		imgCalibration[0] = 0x62;
		
		for (int i = 1; i < 49; i++)
		{
			imgCalibration[i] = 1;
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		xClick = arg0.getX();
		yClick = arg0.getY();
		this.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
