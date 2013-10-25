package erus.android.erusbot;

public class Protocol
{
	public static final byte MOTOR_D = 0x11;
	public static final byte MOTOR_E = 0x12;
	public static final byte MOTOR_VASSOURA = 0x13;
	public static final byte SERVO = 0x14;
	public static final byte MOTOR_VIBRATOR = 0x15;
	
	public static final byte ULTRASOUND = 0x31;
	public static final byte BUTTON_START = 0x32;
	public static final byte OBSTACLE_BUTTON= 0x33;
	public static final byte CAMERA_MESSAGE = 0x60;
	public static final byte REQUEST_IMAGE = 0x61;
	public static final byte IMG_CALIB_DISK = 0x62;
	public static final byte IMG_CALIB_MEM = 0x63;
	public static final byte IMG_CALIB_CONF_AND = 0x64;
}
