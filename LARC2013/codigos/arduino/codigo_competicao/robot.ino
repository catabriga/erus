#include "Adb.h"
#include "SPI.h"
#include "Servo.h"
#include "erus_pins.h"
#include "protocol.h"

Servo servo;
uint8_t Estado[6]; //Vetor de Estados
int motorPWMPins[3] = {PWM_R, PWM_L, PWM_VAS};
int motorDirPins[3] = {DIR_R, DIR_L, DIR_VAS};


void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data);


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

Connection * connection;

void setup()
{

	Serial.begin(57600); //inicia a porta serial 
  
	pinMode(DIR_R,OUTPUT);
	pinMode(PWM_R,OUTPUT);
	pinMode(DIR_L,OUTPUT);
	pinMode(PWM_L,OUTPUT);
	pinMode(DIR_VAS,OUTPUT);
	pinMode(PWM_VAS,OUTPUT);
	pinMode(PIN_VIB, OUTPUT);
	servo.attach(2);
	
	setMotor(0, 0, 1);
	setMotor(1, 0, 1);
	setMotor(2, 0, 1);
	digitalWrite(PIN_VIB, LOW);
	servo.write(90);
 
	ADB::init();
	connection = ADB::addConnection("tcp:4568", true, adbEventHandler);  
}

void loop()
{

	ADB::poll();
	
	
}

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
	if (event == ADB_CONNECTION_RECEIVE)   // If recieve data
	{
		switch(data[0])
		{
			case MOTOR_D:
			{
				setMotor(0, data[1], data[2]);
				break;
			}
			
			case MOTOR_E:
			{
				setMotor(1, data[1], data[2]);
				break;
			}
			
			case MOTOR_VAS:
			{
				setMotor(2, data[1], data[2]);
				break;
			}
			
			case SERVO:
			{
				servo.write(data[1]);
				break;
			}
		}
	}
}
		

