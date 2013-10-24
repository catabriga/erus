#include "Ultrasound.h"
#include "erus_pins.h"
#include "NewPing.h" 

#define SONAR_NUM     6 // Number or sensors.
#define MAX_DISTANCE 200 // Maximum distance (in cm) to ping.
#define PING_INTERVAL 40 // Milliseconds between sensor pings (29ms is about the min to avoid cross-sensor echo).

static unsigned int cm[SONAR_NUM];         // Where the ping distances are stored.
static unsigned long pingTimes[SONAR_NUM];	// Where the ping times are stored.
static uint8_t currentSensor;          // Keeps track of which sensor is active.
static unsigned long nextPingTime;
static int reading;

static NewPing sonar[SONAR_NUM] = {
	NewPing(US_TRIG_RIGHT, US_ECHO_RIGHT, MAX_DISTANCE),
	NewPing(US_TRIG_LEFT, US_ECHO_LEFT, MAX_DISTANCE),
	NewPing(US_TRIG_CENTER_DOWN, US_ECHO_CENTER_DOWN, MAX_DISTANCE),
	NewPing(US_TRIG_CENTER_UP, US_ECHO_CENTER_UP, MAX_DISTANCE),
	NewPing(US_TRIG_RIGHT_BACK, US_ECHO_RIGHT_BACK, MAX_DISTANCE),
	NewPing(US_TRIG_LEFT_BACK, US_ECHO_LEFT_BACK, MAX_DISTANCE)
};

void setupUltrasound(void)
{
	
	currentSensor = -1;
	reading = 1;
	nextPingTime = millis() + 75;
}

void echoCheck(void)
{
	if (sonar[currentSensor].check_timer())
	{
		pingTimes[currentSensor] = sonar[currentSensor].ping_result;
	}
}

void startUltrasoundCycle(void)
{
	currentSensor = -1;
	reading = 1;
	nextPingTime = millis() + 75;
	
	pingTimes[currentSensor] = 50000; // MAX distance in case nothing returns
	sonar[currentSensor].ping_timer(echoCheck); // Do the ping (processing continues, interrupt will call echoCheck to look for echo).
}


void oneSensorCycle(void) 
{
	reading = 0;
}

void loopUltrasound(void) 
{
	if(reading)
	{
		if (millis() >= nextPingTime)
		{
			if(currentSensor >= 0)
			{
				sonar[currentSensor].timer_stop(); // Make sure previous timer is canceled before starting a new ping (insurance).
			}
			
			currentSensor++;
			nextPingTime += PING_INTERVAL;
					
			if(currentSensor < SONAR_NUM)
			{
				pingTimes[currentSensor] = 50000; // MAX distance in case nothing returns
				sonar[currentSensor].ping_timer(echoCheck); // Do the ping, interrupt will call echoCheck to look for echo).
			}
		
			if (currentSensor >= SONAR_NUM)
			{
				oneSensorCycle();
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
