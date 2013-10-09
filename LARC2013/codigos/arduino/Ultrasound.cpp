#include "Ultrasound.h"
#include "NewPing.h" 

#define SONAR_NUM     2 // Number or sensors.
#define MAX_DISTANCE 200 // Maximum distance (in cm) to ping.
#define PING_INTERVAL 40 // Milliseconds between sensor pings (29ms is about the min to avoid cross-sensor echo).

#define US_TRIG_RIGHT 45
#define US_ECHO_RIGHT 43
#define US_TRIG_LEFT 44
#define US_ECHO_LEFT 42
/*#define US_TRIG_FRONT 
#define US_ECHO_FRONT */

static unsigned long pingTimer[SONAR_NUM]; // Holds the times when the next ping should happen for each sensor.
unsigned int cm[SONAR_NUM];         // Where the ping distances are stored.
unsigned long pingTimes[SONAR_NUM];	// Where the ping times are stored.
uint8_t currentSensor;          // Keeps track of which sensor is active.

NewPing sonar[SONAR_NUM];

int reading;

void setPingTimes(int initialDelay)
{
	pingTimer[0] = millis() + initialDelay;

	for (uint8_t i = 1; i < SONAR_NUM; i++) // Set the starting time for each sensor.
	{
		pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;
	}
}

void setupUltrasound(void)
{
	sonar[0] = NewPing(US_TRIG_RIGHT, US_ECHO_RIGHT, MAX_DISTANCE);
	sonar[1] = NewPing(US_TRIG_LEFT, US_ECHO_LEFT, MAX_DISTANCE);

	currentSensor = 0;
	reading = 1;
	setPingTimes(75); // First ping starts at 75ms, gives time for the Arduino to chill before starting.
}

void startUltrasoundCycle(void)
{
	currentSensor = 0;
	reading = 1;
	setPingTimes(75);
}


void echoCheck(void)
{
	if (sonar[currentSensor].check_timer())
	{
		pingTimes[currentSensor] = sonar[currentSensor].ping_result;
	}
}

void oneSensorCycle(void) 
{
	reading = 0;
}

void loopUltrasound(void) 
{
	if(reading)
	{
		for (uint8_t i = 0; i < SONAR_NUM; i++) 
		{
			if (millis() >= pingTimer[i])
			{
				pingTimer[i] += PING_INTERVAL * SONAR_NUM;  // Set next time this sensor will be pinged.
				if (i == 0 && currentSensor == SONAR_NUM - 1)
				{
					oneSensorCycle(); // Sensor ping cycle complete do something with the results.
					return;
				}

				sonar[currentSensor].timer_stop(); // Make sure previous timer is canceled before starting a new ping (insurance).
				currentSensor = i; // Sensor being accessed.
				pingTimes[currentSensor] = 50000; // MAX distance in case nothing returns
				sonar[currentSensor].ping_timer(echoCheck); // Do the ping (processing continues, interrupt will call echoCheck to look for echo).
			}
		}
	}
}

int ultrasoundReadingReady(void)
{
	return (!reading);
}

unsigned int* getUltrasoundValues(void)
{
	int i;
	for(i = 0; i < SONAR_NUM; i++)
	{
		cm[i] = sonar[i].convert_cm(pingTimes[i]);
	}

	return cm;
}
