#include <Arduino.h>
#include "Pins.h"
#include "Error.h"

int getError(int* sensors)
{
	int error = 0;
	error = -5*sensors[0] - 1*sensors[1] + 1*sensors[3] + 5*sensors[4];
	return error;
}
