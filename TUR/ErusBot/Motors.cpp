#include <Arduino.h>
#include "Pins.h"
#include "Motors.h"

int motorPWMPins[2] = {PWM_LEFT, PWM_RIGHT};
int motorDirPins[2] = {DIR_LEFT, DIR_RIGHT};
int motorMAXPWM[2] = {255, 255}; // DO NOT CHANGE!!! YOU MAY DESTROY THE MOTORS!!!

void setupMotors(void)
{
	int i;
	for(i=0; i<2; i++)
	{
		pinMode(motorPWMPins[i], OUTPUT);
		pinMode(motorDirPins[i], OUTPUT);

		analogWrite(motorPWMPins[i], 0);
		digitalWrite(motorDirPins[i], HIGH);
	}
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
		digitalWrite(motorDirPins[motor], LOW);
	}
	else
	{
		digitalWrite(motorDirPins[motor], HIGH);
	}
		
	analogWrite(motorPWMPins[motor], velocity);
}
