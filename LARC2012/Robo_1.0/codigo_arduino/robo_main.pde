#include "NewPing.h"
#include <Adb.h>
#include <SPI.h>
#include <Servo.h>
#include "protocolo.h"
#include "Ultrasound.h"
//#include "encoder.h"
#include "erus_pins.h"
#include "MessageAssembler.h"


#define DEBOUNCE_COUNT 4

// Adb connection.
Connection* connection;

MessageAssembler* messageAssembler;
Servo servoMotor;
//int motorPWMPins[4] = {13, 10, 13, 10};
//int motorDirPins[4] = {11, 12, 11, 12};

/* Movimento Horizontal - A 
   Movimento Vertical - B
*/
int motorPWMPins[4] = {PWMA_ROVER, PWMB_ROVER, PWMA_CLAW, PWMB_CLAW};
int motorDirPins[4] = {DIRA_ROVER, DIRB_ROVER, DIRA_CLAW, DIRB_CLAW};
int motorMAXPWM[4] = { 153, 153, 191, 191}; // DO NOT CHANGE!!! YOU MAY DESTROY THE MOTORS!!!

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


void setupButton(void)
{
	pinMode(START_PIN, INPUT);
}

void setupServoMotor(void)
{       
	servoMotor.attach(SERVO_PIN);
	servoMotor.write(90);
}

void setupBuzzer(void)
{
	pinMode(BUZZER_PIN,OUTPUT);
}

void setupClawButton(void)
{
	pinMode(CLAW_BUTTON,INPUT);
}

void setupTurboButton(void)
{
	pinMode(TURBO_PIN,INPUT);

	delay(500);

	int turbo = digitalRead(TURBO_PIN);

	int i;
	for(i=0; i<10; i++)
	{
		int debounce = digitalRead(TURBO_PIN);
	
		if(turbo != debounce)
		{
			turbo = debounce;
			i = 0;
		}

		delay(100);
	}

	if(!turbo)
	{
		motorMAXPWM[0] = 0xFF;
		motorMAXPWM[1] = 0xFF;
	}
}

void setup(void)
{	
	setupMotors();
	setupUltrasound();
	//setupEncoder();
	setupButton();
	setupServoMotor();
	setupClawButton();
	
	digitalWrite(BUZZER_PIN,HIGH);
	setupTurboButton();

	// Initialise serial port
	Serial.begin(9600);

	// Waits 1 second at startup
	delay(1000);

	digitalWrite(BUZZER_PIN,LOW);	
	
	messageAssembler = createMessageAssembler();
	// Initialize the ADB subsystem.  
	ADB::init();
	// Open an ADB stream to the phone's shell. Auto-reconnect
	connection = ADB::addConnection("tcp:4567", true, adbEventHandler); 

	Serial.print("Erusbot started\n\r");
}


void setMotor(int motor, uint8_t velocity, uint8_t direction)
{
	// This protects the motor to the rated volgate. DO NOT CHANGE!!!
	if(velocity > motorMAXPWM[motor])
	{
		velocity = motorMAXPWM[motor];
	}

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


void setServoMotor(uint8_t pos)
{
	servoMotor.write(pos);		
}

void setBuzzer (uint8_t status)
{
	if (status)
	{
		digitalWrite(BUZZER_PIN,HIGH);
	}
	else
	{
		digitalWrite(BUZZER_PIN,LOW);
	}
}

void processMessages()
{
	uint8_t* msg = getMessage(messageAssembler);
        
	if(msg != NULL)
	{		        
		uint8_t msgType = msg[0];

		switch(msgType)
		{
			case MOTOR_FD:
			{
				setMotor(0, msg[1], msg[2]);
			}break;

			case MOTOR_FE:
			{
				setMotor(1, msg[1], msg[2]);
			}break;

			case MOTOR_TD:
			{
				setMotor(0, msg[1], msg[2]);
			}break;

			case MOTOR_TE:
			{
				setMotor(1, msg[1], msg[2]);
			}break;
			
			case MOTOR_UNICO:
			{
				setMotor(0, msg[1], msg[2]);
				setMotor(1, msg[3], msg[4]);				
			}break;

			case CLAW_HORIZONTAL:
			{
				setMotor(2, msg[1], msg[2]);
			}break;	

			case CLAW_VERTICAL:
			{
				setMotor(3, msg[1], msg[2]);
			}break;
			
			case SERVO:
			{
				setServoMotor(msg[1]);
			}break;	
			
			case BUZZER:
			{
				setBuzzer(msg[1]);
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
	
	data[0] = 0x30;
	data[1] = (uint8_t) values[0];
	data[2] = (uint8_t) values[1];
	data[3] = (uint8_t) values[2];
	data[4] = (uint8_t) values[3];
	data[5] = (uint8_t) values[4];
	data[6] = (uint8_t) values[5];

	connection->write(7, data);
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


void toByte(int data, uint8_t* aux) 
{
    
    aux[0] = (uint8_t) 0;
    aux[1] = (uint8_t) 0;
    aux[2] = (uint8_t)((data >> 8) & 0xFF);
    aux[3] = (uint8_t)((data >> 0) & 0xFF);
    
	if(aux[2] & 0x80)
	{
		aux[0] = 0xFF;
		aux[1] = 0xFF;
	}

	//if(aux[2] >= 128)
	//{
	//	aux[0] = 0x70;
	//}
}

/*
void sendEncoderMessage(int* values)
{
	int i = 0;
	uint8_t data[9];
	uint8_t aux[4];
        
	data[0] = 0x40;
	toByte(values[0], aux);
	for(i=0;i<4;i++)
	{
		data[i+1] = aux[i];
	}
	toByte(values[1], aux);
	for(i=0;i<4;i++)
	{
		data[i+5] = aux[i];
	} 
   
	connection->write(9, data);
        
}


void handleEncoder(void)
{
	static int lastValues[2] = {0, 0};

	int eValues[2];	
	getEncoderValues(eValues);

	if(lastValues[0] != eValues[0] || lastValues[1] != eValues[1])
	{
		sendEncoderMessage(eValues);
		lastValues[0] = eValues[0];
		lastValues[1] = eValues[1];		
	}
}
*/

void sendButtonMessage(int state)
{
	uint8_t data[1];
	
	if(state)
	{
		data[0] = 0x50;
	}
	else
	{
		data[0] = 0x51;
	}

	Serial.print("Button: ");
	Serial.print(data[0]);
	Serial.print("\n\r");

	connection->write(1, data);
}

void handleButton(void)
{
	static int lastButtonState = 0;
	static int stateCount = 0;

	int buttonState = digitalRead(START_PIN);

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

void handleClawButton(void)
{
	static int lastButtonState = 0;
	static int stateCount = 0;

	int buttonState = digitalRead(CLAW_BUTTON);

	if(buttonState != lastButtonState)
	{		
		stateCount = 0;
	}
	else if(stateCount <= DEBOUNCE_COUNT)
	{
		if(stateCount == DEBOUNCE_COUNT)
		{
			sendClawButtonMessage(buttonState);
		}
		stateCount++;
	}

	lastButtonState = buttonState;	
}


void sendClawButtonMessage(int state)
{

	uint8_t data[2];
	
	data[0] = 0x42;
	data[1] = state;

	Serial.print("Claw Button: ");
	Serial.print(data[0]);
	Serial.print("\n\r");

	connection->write(2, data);
}


void loop()
{	
	ADB::poll();

	processMessages();

	handleButton();
	handleUltrasound();
	//handleEncoder();
	handleClawButton();
	
}

