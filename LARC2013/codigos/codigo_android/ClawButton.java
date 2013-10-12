package erus.android.erusbot;


public class ClawButton
{
	private int state;
	
	public ClawButton()
	{
		state = 0;
	}
	public void refresh(byte[] msg)
	{
		state = msg[1];
	}
	public int getState()
	{
		return this.state;
	}

}