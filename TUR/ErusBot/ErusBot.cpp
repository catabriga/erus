#include <Arduino.h>
#include "Pins.h"
#include "Motors.h"
#include "Sensors.h"
#include "PIDControl.h"
#include "Error.h"
#include "LDR.h"
#include "Leds.h"

#define STOP_TIME 14000

static int defaultVelocity = 190;
static int turnVelocity1 = 150;
static int turnVelocity2 = 170;

static int leftSpeed = 170;
static int rightSpeed = 170;
static int curveCounter = 0;

static unsigned long initialTime;
static unsigned long currentTime;

static int initialLight = 0;
static long defaultLight;

static bool inTunnel = false;
static bool lastMoment = false;

static int errorCount = 0;

static int rightSensor;
static int leftSensor;

static int checkPoints = 0;

void setup(void)
{
	Serial.begin(9600);
	setupSensors();
	setupMotors();
	setupPIDControl(0.7, 0, 0);
	pinMode(BUTTON, INPUT);
	pinMode(SIGNAL_LED, OUTPUT);

	
	defaultLight = getDefaultLight();
}

int checkInit(void)
{
	static int init = 0;
	if(!init)
	{
		if(digitalRead(BUTTON))
		{
			init = 1;
            delay(1800);
            
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
	int* leds = readLeds();
	int ldr = getLDR();

	rightSensor = analogRead(RIGHT_SENSOR);
	leftSensor = analogRead(LEFT_SENSOR);

    if (ldr > 3 * defaultLight){
    
           inTunnel = true;
           digitalWrite(SIGNAL_LED,HIGH);
           
    }else if (inTunnel == true && ldr < 1.5 * defaultLight){
        initialTime = millis();
        inTunnel = false;
        lastMoment = true;
        digitalWrite(SIGNAL_LED,LOW);
	}
	

    if ( millis() - initialTime > STOP_TIME && lastMoment == true){
        setMotor(0,0,1);
        setMotor(1,0,1);
        
        return;
    }

    if(sensors[2] == 1 && sensors[0] == 0 && sensors[4] == 0){
		setMotor(0, defaultVelocity, 1); 
		setMotor(1, defaultVelocity, 1);
		
	}else if (sensors[0] == 1 && sensors[1] == 1 && sensors[4] == 0){ 

	    setMotor(0, defaultVelocity, 1); 
		setMotor(1, defaultVelocity, 0);


	}else if (sensors[4] == 1 && sensors[3] == 1 && sensors[0] == 0){

		setMotor(1, defaultVelocity, 1); 
		setMotor(0, defaultVelocity, 0);
		
	}else if(error < 0){
	
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
	else if (error > 0){
	    
		if(sensors[4] == 0)
		{
			setMotor(0, turnVelocity1, 0);
			setMotor(1, turnVelocity1, 1);

			errorCount = errorCount + 1;

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

		errorCount = 0;
	}
}
