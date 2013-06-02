#include <Arduino.h>
#include <math.h>
#include "Pins.h"
#include "Accelerometer.h"

static double x,y,z;

static int minVal;
static int maxVal;

static int xAng,yAng,zAng;

void setupAccelerometer()
{
	x = y = z = 0;
	xAng = yAng = zAng = 0;
	minVal = 265;
	maxVal = 402;
}

double getAccX()
{
	int xRead = analogRead(ACC_X_AXIS);
	xAng = map(xRead, minVal, maxVal, -90, 90);
	x = RAD_TO_DEG * (atan2(-yAng, -zAng) + M_PI);
	
	return x;
}

double getAccY()
{
	int yRead = analogRead(ACC_Y_AXIS);
	yAng = map(yRead, minVal, maxVal, -90, 90);
	y = RAD_TO_DEG * (atan2(-xAng, -zAng) + M_PI);
	
	return y;
}

double getAccZ()
{
	int zRead = analogRead(ACC_Z_AXIS);
	zAng = map(zRead, minVal, maxVal, -90, 90);
 	z = RAD_TO_DEG * (atan2(-yAng, -xAng) + M_PI);
 	
 	return z;
}
