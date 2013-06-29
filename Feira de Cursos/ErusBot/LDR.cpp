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

long getDefaultLight(void)
{
    unsigned long time = millis();
    unsigned long sum = 0;
    int n = 0;
    
    while(millis() - time < 200)
    {
        sum += getLDR();
        n++;
    }
    return sum/n;
}
