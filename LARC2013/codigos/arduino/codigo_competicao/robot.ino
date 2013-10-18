#include "Adb.h"
#include "SPI.h"
#include "Servo.h"
#include "erus_pins.h"
#include "protocol.h"
#include "Ultrasound.h"
#include "MessageAssembler.h"

#define DEBOUNCE_COUNT 4

int ligado = 0;
int desligado = 0;

Servo servoMotor;
int motorPWMPins[3] = {PWM_R, PWM_L, PWM_VAS};
int motorDirPins[5] = {DIR_R1, DIR_R2, DIR_L1, DIR_L2, DIR_VAS};

//Adb Connection
Connection * connection;

MessageAssembler* messageAssembler;

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data);


void setupMotors(void)
{
	//DIREITA
	pinMode(motorPWMPins[0], OUTPUT);
	pinMode(motorDirPins[0], OUTPUT);
	pinMode(motorDirPins[1], OUTPUT);
	analogWrite(motorPWMPins[0], 0);
	digitalWrite(motorDirPins[0], HIGH);
	digitalWrite(motorDirPins[1], LOW);
	
	//ESQUERDA
	pinMode(motorPWMPins[1], OUTPUT);
	pinMode(motorDirPins[2], OUTPUT);
	pinMode(motorDirPins[3], OUTPUT);
	analogWrite(motorPWMPins[1], 0);
	digitalWrite(motorDirPins[2], HIGH);
	digitalWrite(motorDirPins[3], LOW);
	
	//VASSOURA
	pinMode(motorPWMPins[2], OUTPUT);
	pinMode(motorDirPins[4], OUTPUT);
	analogWrite(motorPWMPins[2], 0);
	digitalWrite(motorDirPins[4], HIGH);
	
}

void setupButton(void)
{
	pinMode(START_BUTTON, INPUT);
	digitalWrite(START_BUTTON, HIGH);
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
	if(motor == 0) // direita
	{
		if(direction) //para o motorshield novo
		{
			digitalWrite(motorDirPins[0], HIGH);
			digitalWrite(motorDirPins[1], LOW);
		}
		else
		{
			digitalWrite(motorDirPins[0], LOW);
			digitalWrite(motorDirPins[1], HIGH);
		}
	} else if (motor == 1){ // esquerda
		if(direction) //para o motorshield novo
		{
			digitalWrite(motorDirPins[2], LOW);
			digitalWrite(motorDirPins[3], HIGH);
		}
		else
		{
			digitalWrite(motorDirPins[2], HIGH);
			digitalWrite(motorDirPins[3], LOW);
		}
	} else {
		if(direction)
		{
			digitalWrite(motorDirPins[4], HIGH);
		}
		else // vassoura
		{
			digitalWrite(motorDirPins[4], LOW);
		}
	}	

	analogWrite(motorPWMPins[motor], velocity);
}

void setup()
{

	Serial.begin(57600); //inicia a porta serial 
  
	setupMotors();
	setupServoMotor();
	setupVibrationMotor();
	setupUltrasound();
	setupButton();
	
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
				setMotor(2, msg[1], msg[2]);
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
	uint8_t data[7];
	
	data[0] = 0x31;
	if(values[0] > 255)
	{
		data[1] = (uint8_t) 255; //limite da distancia
	} else {
		data[1] = (uint8_t) values[0];
	}
	if(values[1] > 255)
	{
		data[2] = (uint8_t) 255; //limite da distancia
	} else {
		data[2] = (uint8_t) values[1];
	}
	if(values[2] > 255)
	{
		data[3] = (uint8_t) 255; //limite da distancia
	} else {
		data[3] = (uint8_t) values[2];
	}
	if(values[3] > 255)
	{
		data[4] = (uint8_t) 255; //limite da distancia
	} else {
		data[4] = (uint8_t) values[3];
	}
	if(values[4] > 255)
	{
		data[5] = (uint8_t) 255; //limite da distancia
	} else {
		data[5] = (uint8_t) values[4];
	}
	if(values[5] > 255)
	{
		data[6] = (uint8_t) 255; //limite da distancia
	} else {
		data[6] = (uint8_t) values[5];
	}
	
	connection->write(7, data);
}

void handleUltrasound(void)
{
	loopUltrasound();
	if(ultrasoundReadingReady())
	{
		unsigned int* uValues = getUltrasoundValues();
		sendUltrasoundMessage(uValues);
		/*Serial.print(uValues[0]);
		Serial.print(" ,");
		Serial.print(uValues[1]);
		Serial.print(" ,");
		Serial.println(uValues[2]);*/

		startUltrasoundCycle();
	}
}

void sendButtonMessage(int state)
{
	uint8_t data[2] = {0x32, 0};
	
	if(!state)
	{
		data[1] = 1;
	}

	Serial.print("Button: ");
	Serial.println(data[1]);
	
	connection->write(2, data);
}

void handleButton(void)
{
	static int lastButtonState = 0;
	static int stateCount = 0;

	int buttonState = digitalRead(START_BUTTON);

	if(buttonState != lastButtonState)
	{		
		stateCount = 0;		
	}
	else if(stateCount <= DEBOUNCE_COUNT)
	{
		if(stateCount == DEBOUNCE_COUNT)
		{
			sendButtonMessage(buttonState);
			
		}
		stateCount++;
	}

	lastButtonState = buttonState;
}

void loop()
{
	ADB::poll();
	
	processMessages();
	handleUltrasound();
	handleButton();

}		

