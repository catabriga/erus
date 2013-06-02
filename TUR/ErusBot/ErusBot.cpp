#include <Arduino.h>
#include "Pins.h"
#include "Motors.h"
#include "Sensors.h"
#include "PIDControl.h"
#include "Error.h"

static int defaultVelocity = 100;
static int turnVelocity = 50;
static unsigned long lastTime;

void setup(void)
{
	Serial.begin(9600);
	setupSensors();
	setupMotors();
	setupPIDControl(3, 0, 0);
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
			delay(2000);
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
	//int pid = getPIDControl(error);
	/*Serial.print(sensors[0]);
	Serial.print(" / ");
	Serial.print(sensors[1]);
	Serial.print(" / ");
	Serial.print(sensors[2]);
	Serial.print(" / ");
	Serial.print(sensors[3]);
	Serial.print(" / ");
	Serial.print(sensors[4]);
	Serial.println(" / ");
	Serial.println(error);*/
	if(error == 0)
	{
		setMotor(0, defaultVelocity, 1); // 1 - > Frente
		setMotor(1, defaultVelocity, 1);
	}
	else if(error < 0)
	{
		setMotor(0, turnVelocity, 1);
		setMotor(1, turnVelocity, 0);
	}
	else
	{
		setMotor(0, turnVelocity, 0);
		setMotor(1, turnVelocity, 1);
	}
}
