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


Connection * connection;


void setup()
{
  Serial.begin(57600); //inicia a porta serial 
  
	ADB::init();
	connection = ADB::addConnection("tcp:4568", true, adbEventHandler);  
}

void loop()
{
	ADB::poll();

}

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
	if (event == ADB_CONNECTION_RECEIVE)   // If recieve data
	{
	}
}


