#include <Arduino.h>
#include "Pins.h"
#include "Motors.h"
#include "Sensors.h"
#include "PIDControl.h"
#include "Error.h"
#include "LDR.h"

static int defaultVelocity = 170;
static int turnVelocity1 = 90;
static int turnVelocity2 = 170;
static unsigned long lastTime;

void setup(void)
{
	Serial.begin(9600);
	setupSensors();
	setupMotors();
	setupPIDControl(25, 0, 1);
	pinMode(BUTTON, INPUT);
	analogWrite(1, 0);
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
//	int ldr = getLDR();
//	int pid = getPIDControl(error);
	
/*	setMotor(0, defaultVelocity + pid, 1); // Motor 0 -> esquerda
	setMotor(1, defaultVelocity - pid, 1);
	
	Serial.print("Motor 0 = ");
	Serial.println(defaultVelocity + pid);
	Serial.print("Motor 1 = ");
	Serial.println(defaultVelocity - pid);*/
	
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

	
	
	if(sensors[2] == 1)
	{
		setMotor(0, defaultVelocity, 1); // 1 - > Frente
		setMotor(1, defaultVelocity, 1);
	}
	else if(error < 0)
	{
		if(sensors[0] == 0)
		{
			setMotor(0, turnVelocity1, 1);
			setMotor(1, turnVelocity1, 0);
		}
		else
		{
			setMotor(0, turnVelocity2, 1);
			setMotor(1, turnVelocity2, 0);
		}
	}
	else if (error > 0)
	{
		if(sensors[4] == 0)
		{
			setMotor(0, turnVelocity1, 0);
			setMotor(1, turnVelocity1, 1);
		}
		else
		{
			setMotor(0, turnVelocity2, 0);
			setMotor(1, turnVelocity2, 1);
		}
	}
	else
	{
	
		setMotor(0, defaultVelocity, 1); // 1 - > Frente
		setMotor(1, defaultVelocity, 1);
	}
}
