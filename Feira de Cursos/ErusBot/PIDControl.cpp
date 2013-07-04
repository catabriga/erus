#include <Arduino.h>
#include "Pins.h"
#include "PIDControl.h"

static double kp, ki, kd;
static int lastError;
static long int errorSum;
static long int sataurateConstant;

void setupPIDControl(double kpp, double kip, double kdp)
{
	lastError = 0;
	errorSum = 0;
	kp = kpp;
	ki = kip;
	kd = kdp;
	sataurateConstant = 10000;
}

static long int saturate(long int errorSum)
{
	if(errorSum > sataurateConstant)
	{
		return sataurateConstant;
	}
	
	if(errorSum < -sataurateConstant)
	{
		return -sataurateConstant;
	}
	return errorSum;
}

int getPIDControl(int error)
{
	errorSum = errorSum + error;
	errorSum = saturate(errorSum);

	int p = (int)kp*error;
	int i = (int)ki*errorSum;
	int d = (int)kd*(error - lastError);
	
	lastError = error;
	
	return (p+i+d);
}
