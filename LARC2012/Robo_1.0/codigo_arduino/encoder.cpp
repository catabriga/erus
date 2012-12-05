#include "encoder.h"

#include "NewPing.h"
#include "erus_pins.h"

int interruptRightNum = 0;	// Interrupt 0 is on Pin 2! (INTERRUPT_RIGHT)
int pinDirectionRight = DIRECTION_RIGHT;
volatile int countRight;

int interruptLeftNum = 1;	// Interrupt 1 is on Pin 3!
int pinDirectionLeft = DIRECTION_LEFT;
volatile int countLeft;

void interruptRight(void)
{
	if(digitalRead(pinDirectionRight) == HIGH)
	{
		countRight++;
	}
	else
	{
		countRight--;
	}
}

void interruptLeft(void)
{
	if(digitalRead(pinDirectionLeft) == HIGH)
	{
		countLeft++;
	}
	else
	{
		countLeft--;
	}
}

void setupEncoder()
{
	pinMode(pinDirectionRight, INPUT);
	pinMode(pinDirectionLeft, INPUT);
	
	attachInterrupt(interruptRightNum, interruptRight, RISING);
	attachInterrupt(interruptLeftNum, interruptLeft, RISING);  

	countRight = 0;
	countLeft = 0;
}

void getEncoderValues(int* values)
{
	// This function must disable interrups or the values of the counters could
	// change while they were being read.

	uint8_t SaveSREG = SREG;   // save interrupt flag
	cli();   // disable interrupts

	values[0] = countRight;
	values[1] = countLeft;

	SREG = SaveSREG;
}


