package erus.android.erusbot;

public class UltraSound
{
	private int us1 = 0;
	private int us2 = 0;
	private int us3 = 0;
	private int us4 = 0;
	private int us5 = 0;
	private int us6 = 0;
	private int infra = 0;
	
	public UltraSound()
	{		

	}
	
	public void refresh(byte[] msg)
	{	
		us1 = (int)(msg[1] & 0xFF);
		us2 = (int)(msg[2] & 0xFF);
		us3 = (int)(msg[3] & 0xFF);
		us4 = (int)(msg[4] & 0xFF);
		us5 = (int)(msg[5] & 0xFF);
		us6 = (int)(msg[6] & 0xFF);
		infra = (int)(msg[7] & 0xFF);
		
		
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
	
	public int getUs4()
	{
		return this.us4;
	}
	
	public int getUs5()
	{
		return this.us5;
	}
	
	public int getUs6()
	{
		return this.us6;
	}
	
	public int getInfra()
	{
		return this.infra * 4;
	}
}
