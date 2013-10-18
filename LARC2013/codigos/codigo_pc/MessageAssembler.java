

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MessageAssembler
{
	private static final int BUFFER_SIZE = 2000000;
	
	private Connection connection;
		
	private byte[] connectionBuffer;
	private int bufferEnd;
	
	private List<byte[]> msgBuffer;
	
	private int[] msgSizeTable;
	
	MessageAssembler(Connection connection)
	{
		this.connection = connection;
		
		msgBuffer = new ArrayList<byte[]>();
		connectionBuffer = new byte[BUFFER_SIZE];
		bufferEnd = 0;
		
		initMsgSizeTable();
	}
	
	private void initMsgSizeTable()
	{
		msgSizeTable = new int[255];
		
		for(int i=0; i<255; i++)
		{
			msgSizeTable[i] = 0;
		}
		
		msgSizeTable[0x11] = 3;	// MOTOR_D
		msgSizeTable[0x12] = 3;	// MOTOR_E
		msgSizeTable[0x13] = 3;	// MOTOR_VAS
		msgSizeTable[0x14] = 2;	// SERVO
		msgSizeTable[0x15] = 2;	// VIBRADOR
		msgSizeTable[0x31] = 7;// ULTRASOM
		msgSizeTable[0x32] = 2;// BUTTON
		msgSizeTable[0x64] = 49; //IMG_CALIB_CONF_ANDR
	}
	
	private int getMessageSize(int messageType)
	{
		int size = msgSizeTable[messageType];
		
		if(messageType == 0x60)
		{			
			if(bufferEnd >= 13)
			{
				ByteBuffer bb = ByteBuffer.allocate(4);
				bb.put(connectionBuffer, 9, 4);
				bb.rewind();
				size = bb.getInt() + 13;
			}
			else
			{
				size = -1;
			}
		}
		else if(messageType == 0x65)
		{
			if(bufferEnd > 1)
			{
				size = (connectionBuffer[1] & 0xFF) + 2;
			}
			else
			{
				size = -1;
			}
		}
		
		return size;
	}
	
	public boolean messagesAvailable()
	{
		if(msgBuffer.size() > 0)
		{
			return true;
		}
		
		return false;
	}
	
	public synchronized byte[] getNextMessage()
	{
		if(msgBuffer.size() > 0)
		{
			return msgBuffer.remove(0);
		}		
		
		return null;
	}
	
	private void moveMsgToBuffer(int size)
	{
		byte[] newMsg = new byte[size];
		
		for(int i=0; i<size; i++)
		{
			newMsg[i] = connectionBuffer[i];
		}
		
		synchronized(this)
		{
			msgBuffer.add(newMsg);
		}
		
		bufferEnd = bufferEnd - size;
		for(int i=0; i<bufferEnd; i++)
		{
			connectionBuffer[i] = connectionBuffer[i+size];
		}
		
	}
	
	private void discardBytes(int numberBytes)
	{
		bufferEnd = bufferEnd - numberBytes;
		for(int i=0; i<bufferEnd; i++)
		{
			connectionBuffer[i] = connectionBuffer[i+numberBytes];
		}
	}
	
	private void processMessages()
	{
		boolean limited = false;
		while(bufferEnd > 0 && !limited)
		{			
			int type = connectionBuffer[0] & 0xFF;	// Isto converte bytes negativos para positivo
			
			int size = getMessageSize(type);
			if(size == 0)
			{
				discardBytes(1);
			}
			else if(size == -1)
			{
				limited = true;
			}
			else
			{
				if(bufferEnd > size-1)
				{
					moveMsgToBuffer(size);
				}
				else
				{
					limited = true;
				}
			}			
		}
	}
	
	public void listenConnection()
	{			
		try
		{
			int readSize = connection.readMessageNonBlocking(connectionBuffer, bufferEnd, BUFFER_SIZE-bufferEnd);
			bufferEnd += readSize;
			
			processMessages();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	
}
