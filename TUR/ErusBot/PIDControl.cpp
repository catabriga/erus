#include <Arduino.h>
#include "Pins.h"
#include "PIDControl.h"

static int kp, ki, kd;
static int lastError;
static long int errorSum;
static long int sataurateConstant;

void setupPIDControl(int kpp, int kip, int kdp)
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
	int p = kp*error;
	int i = (ki*errorSum)>>10;
	int d = kd*(error - lastError);
	
	lastError = error;
	
	return (p+i+d);
}
