/* Argumento[0]: Funcionamento do Robo (0 desligada, 1 ligada)
 * Argumento[1]: Estado do robo
 * Argumento[2]: Velocidade (0 a 5)
 * Argumento[3]: Corresponde ao funcionamento da vassoura (0 desligada, 1 ligada)
 * Argumento[4]: Corresponde ao despejo das latas (0 parado, 1 abrindo, 2 fechando)
 * Argumento[5]: Corresponde ao vibrador (0 desligada, 1 ligada)
 
 * LISTA DE ESTADOS DO ROBO
 * 0 Totalmente parado
 * 1 Andando para frente
 * 2 Andando para traz
 * 3 Curvando para esquerda andando pra frente
 * 4 Curvando para direita andando pra frente
 * 5 Girando (no mesmo lugar - 360º) para esquerda
 * 6 Girando (no mesmo lugar - 360º) para direita
 * 7 Curvando para esquerda andando pra traz
 * 8 Curvando para direita andando pra traz

*/


#include "Adb.h"
#include "SPI.h"
//#include "Servo.h"

/*
//Pinos dos Motores para Movimentação
#define DIR_R 46 //Direita
#define PWM_R 9 
#define DIR_L 48 //Esquerda
#define PWM_L 10

//Pinos do Motor da Vassoura
#define DIR_VAS 34
#define PWM_VAS 11

#define PIN_VIB 23 //VIBRADOR
#define PIN_SERVO 2

Servo servo;
uint8_t Estado[6]; //Vetor de Estados
int motorPWMPins[3] = {PWM_R, PWM_L, PWM_VAS};
int motorDirPins[3] = {DIR_R, DIR_L, DIR_VAS};


void setMotor(int motor, uint8_t velocity, uint8_t direction)
{

	if(direction)
	{
		digitalWrite(motorDirPins[motor], HIGH);
	}
	else
	{
		digitalWrite(motorDirPins[motor], LOW);
	}	

	analogWrite(motorPWMPins[motor], velocity);
}
*/

Connection * connection;

void setup()
{
  /*
  Serial.begin(57600); //inicia a porta serial 
  
	pinMode(DIR_R,OUTPUT);
	pinMode(PWM_R,OUTPUT);
	pinMode(DIR_L,OUTPUT);
	pinMode(PWM_L,OUTPUT);
	pinMode(DIR_VAS,OUTPUT);
	pinMode(PWM_VAS,OUTPUT);
	pinMode(PIN_VIB, OUTPUT);
	servo.attach(2);
	
	setMotor(0, 0, 1);
	setMotor(1, 0, 1);
	setMotor(2, 0, 1);
	digitalWrite(PIN_VIB, LOW);
	servo.write(90);
 */
	ADB::init();
	connection = ADB::addConnection("tcp:4568", true, adbEventHandler);  
}

void loop()
{
//	uint8_t velocity;
	ADB::poll();
/*
	if(Estado[0])
	{
		//Motores para movimento
		velocity = 63*Estado[2];
		
		if(Estado[1] == 0)
		{
			setMotor(0, 0, 1);
			setMotor(1, 0, 1);
		}
		if(Estado[1] == 1)
		{
			setMotor(0, velocity, 1);
			setMotor(1, velocity, 1);
		}
		if(Estado[1] == 2)
		{
			setMotor(0, velocity, 0);
			setMotor(1, velocity, 0);
		}
		if(Estado[1] == 3)
		{
			setMotor(0, velocity, 1);
			setMotor(1, velocity/2, 1);
		}
		if(Estado[1] == 4)
		{
			setMotor(0, velocity/2, 1);
			setMotor(1, velocity, 1);
		}
		if(Estado[1] == 5)
		{
			setMotor(0, velocity, 1);
			setMotor(1, velocity, 0);
		}
		if(Estado[1] == 6)
		{
			setMotor(0, velocity, 0);
			setMotor(1, velocity, 1);
		}
		if(Estado[1] == 7)
		{
			setMotor(0, velocity/2, 0);
			setMotor(1, velocity, 0);
		}
		if(Estado[1] == 8)
		{
			setMotor(0, velocity, 0);
			setMotor(1, velocity/2, 0);
		}
		
		//vassoura
		if(Estado[3] == 1)
			setMotor(2, 250, 1);
		else
			setMotor(2, 0, 1);
			
		//comporta
		if(Estado[4] == 0 || Estado[4] == 2)
			servo.write(90);
		if(Estado[4] == 1)
			servo.write(77);
			
		//vibrador
		if(Estado[5] == 1)
			digitalWrite(PIN_VIB, HIGH);
		else
			digitalWrite(PIN_VIB, LOW);
	} else {
		setMotor(0, 0, 1);
		setMotor(1, 0, 1);
		setMotor(2, 0, 1);
		digitalWrite(PIN_VIB, LOW);
		servo.write(90);
	}
*/
}

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
	if (event == ADB_CONNECTION_RECEIVE)   // If recieve data
	{
/*		for(int i = 0; i < 6; i++)
			Estado[i] = data[i];
        Serial.print(Estado[0]);
		Serial.print(" , ");
		Serial.print(Estado[1]);
		Serial.print(" , ");
		Serial.print(Estado[2]);
		Serial.print(" , ");
		Serial.print(Estado[3]);
		Serial.print(" , ");
		Serial.print(Estado[4]);
		Serial.print(" , ");
		Serial.println(Estado[5]);
*/
	}
}
		

