package erus.android;

import java.nio.ByteBuffer;

public class Encoder
{
	private int encRight = 0;
	private int encLeft = 0;
	
	public Encoder()
	{		
		//Request encoder info from arduino.
	}
	
	public void refresh(byte[] msg)
	{	
		ByteBuffer bb = ByteBuffer.allocate(8);	
		bb.put(msg, 1, 8);
		
		bb.rewind();
		
		encLeft = bb.getInt();
		encRight = bb.getInt();
		
	}

	public int getEncRight()
	{
		return this.encRight;
	}
	
	public int getEncLeft()
	{
		return this.encLeft;
	}

}
