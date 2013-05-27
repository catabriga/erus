#include <Arduino.h>
#include "Pins.h"
#include "Error.h"

int getError(int* sensors)
{
	int error = 0;
	error = -1*sensors[1] + 1*sensors[3];
	return error;
}
