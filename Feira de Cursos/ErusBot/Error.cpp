#include <Arduino.h>
#include "Pins.h"
#include "Error.h"

int getError(int* sensors)
{
	int error = 0;
	error = -5*sensors[0] - 1*sensors[1] + 1*sensors[3] + 5*sensors[4];

    return error;


    // TESTE PID JUAN


/*    Serial.print(sensors[0]);
	Serial.print(" / ");
	Serial.print(sensors[1]);
	Serial.print(" / ");
	Serial.print(sensors[2]);
	Serial.print(" / ");
	Serial.print(sensors[3]);
	Serial.print(" / ");
	Serial.print(sensors[4]);
	Serial.println(" / ");*/


/*    if (sensors[0] == 0 && sensors[1] == 1 && sensors[2] == 1 && sensors[3]  == 1 && sensors[4] == 1) return -4;
    else if (sensors[0] == 0 && sensors[1] == 0 && sensors[2]  == 1 && sensors[3] == 1 && sensors[4] == 1) return -3;
    else if (sensors[0] == 1 && sensors[1] == 0 && sensors[2] == 1 && sensors[3] == 1 && sensors[4] == 1) return -2;
    else if (sensors[0] == 1 && sensors[1] == 0 && sensors[2] == 0 && sensors[3] == 1 && sensors[4] == 1) return -1;
    else if (sensors[0] == 1 && sensors[1] == 1 && sensors[2] == 0 && sensors[3] == 1 && sensors[4]== 1) return 0; //OK!
    else if (sensors[0] == 1 && sensors[1] == 1 && sensors[2] == 0 && sensors[3] == 0 && sensors[4] == 1) return 1;
    else if (sensors[0] == 1 && sensors[1] == 1 && sensors[2] == 1 && sensors[3] == 0 && sensors[4] == 1) return 2;
    else if (sensors[0] == 1 && sensors[1] == 1 && sensors[2] == 1 && sensors[3] == 0 && sensors[4] == 0) return 3;
    else if (sensors[0] == 1 && sensors[1] == 1 && sensors[2] == 1 && sensors[3] == 1 && sensors[4] == 0) return 4;*/

}
