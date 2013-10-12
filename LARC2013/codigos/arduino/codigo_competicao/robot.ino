#include "Adb.h"
#include "SPI.h"
#include "Servo.h"
#include "erus_pins.h"
#include "protocol.h"
#include "Ultrasound.h"
#include "MessageAssembler.h"

Servo servoMotor;
int motorPWMPins[3] = {PWM_R, PWM_L, PWM_VAS};
int motorDirPins[3] = {DIR_R, DIR_L, DIR_VAS};

//Adb Connection
Connection * connection;

MessageAssembler* messageAssembler;

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data);


void setupMotors(void)
{
	int i;
	for(i=0; i<4; i++)
	{
		pinMode(motorPWMPins[i], OUTPUT);
		pinMode(motorDirPins[i], OUTPUT);

		analogWrite(motorPWMPins[i], 0);
		digitalWrite(motorDirPins[i], HIGH);
	}
}

void setupServoMotor(void)
{       
	servoMotor.attach(SERVO_PIN);
	servoMotor.write(90);
}

void setupVibrationMotor(void)
{       
	pinMode(VIB_PIN, OUTPUT);	
	digitalWrite(VIB_PIN, LOW);
}

void setMotor(int motor, uint8_t velocity, uint8_t direction)
{

	if(direction)
	{
		digitalWrite(motorDirPins[motor], HIGH);
	}
	else
	{
		digitalWrite(motorDirPins[motor], LOW);
	}	

	analogWrite(motorPWMPins[motor], velocity);
}

void setup()
{

	Serial.begin(57600); //inicia a porta serial 
  
	setupMotors();
	setMotor(0, 0, 1);
	setMotor(1, 0, 1);
	setupServoMotor();
	setupVibrationMotor();
	setupUltrasound();
	
	messageAssembler = createMessageAssembler();
 
	ADB::init();
	connection = ADB::addConnection("tcp:4567", true, adbEventHandler);  
}

void setServoMotor(uint8_t velocity)
{
	servoMotor.write(velocity);		
}

void processMessages()
{
	uint8_t* msg = getMessage(messageAssembler);
        
	if(msg != NULL)
	{		        
		uint8_t msgType = msg[0];

		switch(msgType)
		{
			case MOTOR_D:
			{
				setMotor(0, msg[1], msg[2]);
			}break;

			case MOTOR_E:
			{
				setMotor(1, msg[1], msg[2]);
			}break;
			
			case MOTOR_VAS:
			{
				setMotor(3, msg[1], msg[2]);
			}break;
			
			case SERVO:
			{
				setServoMotor(msg[1]);
			}break;	
																		
		}
		removeMessage(messageAssembler);
	}
}

void adbEventHandler(Connection* connection, adb_eventType event, uint16_t length, uint8_t* data)
{	
	if (event == ADB_CONNECTION_RECEIVE)
	{
		//Serial.print("Message received. Length: ");
		//Serial.print(length);
		//Serial.print("\n\r");

		int lengthMessage = addBytes(messageAssembler, data, length);

		//Serial.print(lengthMessage);


		if(lengthMessage == length)//TODO
		{
		}
		else
		{
		}
	}

}

void sendUltrasoundMessage(unsigned int* values)
{
	uint8_t data[4];
	
	data[0] = 0x31;
	data[1] = (uint8_t) values[0];
	data[2] = (uint8_t) values[1];
	data[3] = (uint8_t) values[2];
	
	connection->write(4, data);
}

void handleUltrasound(void)
{
	loopUltrasound();
	if(ultrasoundReadingReady())
	{
		unsigned int* uValues = getUltrasoundValues();
		sendUltrasoundMessage(uValues);

		startUltrasoundCycle();
	}
}

void loop()
{
	ADB::poll();
	//setMotor(0, 0, 1);
	//setMotor(1, 0, 1);
	processMessages();
	handleUltrasound();

}		

