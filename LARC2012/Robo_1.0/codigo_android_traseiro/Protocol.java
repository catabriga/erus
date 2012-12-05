package erus.android;

public class Protocol
{
	public static final byte MOTOR_FD = 0x10;
	public static final byte MOTOR_FE = 0x11;
	public static final byte MOTOR_TD = 0x12;
	public static final byte MOTOR_TE = 0x13;
	public static final byte MOTOR_UNICO = 0x14;
	public static final byte ULTRASOUND = 0x30;
	public static final byte CLAW_HORIZONTAL = 0x31;
	public static final byte CLAW_VERTICAL = 0x32;
	public static final byte SERVO = 0x33;
	public static final byte ENCODER = 0x40;
	public static final byte CLAW_BUTTON = 0x42;
	public static final byte BUZZER = 0x45;
	public static final byte BUTTON_START = 0x50;
	public static final byte BUTTON_STOP = 0x51;
	public static final byte CAMERA_MESSAGE = 0x60;
	public static final byte REQUEST_IMAGE = 0x61;
	public static final byte IMG_CALIB_DISK = 0x62;
	public static final byte IMG_CALIB_MEM = 0x63;
	public static final byte IMG_CALIB_CONF_AND = 0x64;
	public static final byte TRASH_POSITION = 0x66;
	public static final byte REQUEST_TRASH_POSITION = 0x67;
	public static final byte REQUEST_IMAGE_BACK = 0x68;
}
