#include <Arduino.h>
#include "Pins.h"
#include "PIDControl.h"

static int kp, ki, kd;
static int lastError;
static int errorSum;

void setupPIDControl(void)
{
	lastError = 0;
	errorSum = 0;
	kp = 1;
	ki = 0;
	kd = 0;
}

int getPIDControl(int error)
{
	errorSum = errorSum + error;

	int p = kp*error;
	int i = ki*errorSum;
	int d = kd*(error - lastError);
	
	lastError = error;
	
	return (p+i+d);
}
