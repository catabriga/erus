#include <Arduino.h>
#include "Pins.h"
#include "Sensors.h"

static int sensorsValues[5];

void setupSensors(void)
{
	pinMode(LIGHT_SENSOR_1, INPUT);
	pinMode(LIGHT_SENSOR_2, INPUT);
	pinMode(LIGHT_SENSOR_3, INPUT);
	pinMode(LIGHT_SENSOR_4, INPUT);
	pinMode(LIGHT_SENSOR_5, INPUT);
}

int* readSensors(void)
{
	sensorsValues[4] = digitalRead(LIGHT_SENSOR_1); //Lógica para linha preta em caminho branco; para linha branca em caminho preto retirar '1 -'
	sensorsValues[3] =  digitalRead(LIGHT_SENSOR_2);
	sensorsValues[2] =  digitalRead(LIGHT_SENSOR_3);
	sensorsValues[1] =  digitalRead(LIGHT_SENSOR_4);
	sensorsValues[0] =  digitalRead(LIGHT_SENSOR_5);
	
	return &(sensorsValues[0]);
}
