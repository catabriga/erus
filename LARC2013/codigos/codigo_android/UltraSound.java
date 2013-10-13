package erus.android.erusbot;

public class UltraSound
{
	private int us1 = 0;
	private int us2 = 0;
	private int us3 = 0;
	
	public UltraSound()
	{		

	}
	
	public void refresh(byte[] msg)
	{	
		us1 = (int)(msg[1] & 0xFF);
		us2 = (int)(msg[2] & 0xFF);
		us3 = (int)(msg[3] & 0xFF);
	}

	public int getUs1()
	{
		return this.us1;
	}
	
	public int getUs2()
	{
		return this.us2;
	}

	public int getUs3()
	{
		return this.us3;
	}
}
