#include <Arduino.h>
#include "Pins.h"
#include "Motors.h"
#include "Sensors.h"
#include "PIDControl.h"
#include "Error.h"

static int defaultVelocity = 173;

void setup(void)
{
	Serial.begin(9600);
	setupSensors();
	setupMotors();
	pinMode(BUTTON, INPUT);
}

int checkInit(void)
{
	static int init = 0;
	if(!init)
	{
		if(digitalRead(BUTTON))
		{
			init = 1;
		}
	}
	
	return init;
}

void loop(void)
{
	if(!checkInit())
	{
		return;
	}
	
	int* sensors = readSensors();
	int error = getError(sensors);
	int pid = getPIDControl(error);
	setMotor(0, defaultVelocity - pid, 1);
	setMotor(1, defaultVelocity + pid, 1);
}
