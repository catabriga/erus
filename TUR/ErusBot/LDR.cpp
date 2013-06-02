#include <Arduino.h>
#include "Pins.h"
#include "Motors.h"

static int ldrValue;

void setupLDR(void)
{
	ldrValue = 0;
}

int getLDR(void)
{
	ldrValue = analogRead(LDR_PIN);
	return ldrValue;
}
