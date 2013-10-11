/* 
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

//  Motor B
int dir1PinA = 1;
int dir2PinA = 0;
int speedPinA = 5;

//  Motor B
int dir1PinB = 2;
int dir2PinB = 3;
int speedPinB = 6;

//  Velocidade (0 a 255)
int velocidade;

//Variavel que armazena os Estados
uint8_t Estado; 

//Conexao com o Android
Connection * connection;  

//direcoes 0 anti-horario e 1 horario
void setMotor(int velocidadeMotorA, int velocidadeMotorB, int direcaoMotorA, int direcaoMotorB)
{
   analogWrite(speedPinA,velocidadeMotorA); 
   analogWrite(speedPinB,velocidadeMotorB);

   if(direcaoMotorA){
      digitalWrite(dir1PinA,LOW);
      digitalWrite(dir2PinA,HIGH);
   }
   else{
      digitalWrite(dir1PinA,HIGH);
      digitalWrite(dir2PinA,LOW); 
   }

   if(direcaoMotorB){
      digitalWrite(dir1PinB,LOW);
      digitalWrite(dir2PinB,HIGH);
   }
   else{
      digitalWrite(dir1PinB,HIGH);
      digitalWrite(dir2PinB,LOW); 
   }

}

void setup()
{
  //Definindo pinos de saida 
  pinMode(dir1PinA,OUTPUT);
  pinMode(dir2PinA,OUTPUT);
  pinMode(speedPinA,OUTPUT);
  pinMode(dir1PinB,OUTPUT);
  pinMode(dir2PinB,OUTPUT);
  pinMode(speedPinB,OUTPUT);
  
  velocidade = 255;
  
  //Inicializando thread do ADB
  ADB::init();
  connection = ADB::addConnection("tcp:4568", true, adbEventHandler);  
}

void loop()
{

  ADB::poll();
  	
  if(Estado == 0)
    setMotor(0,0,0,0);		
  else if(Estado == 1)
    setMotor(velocidade,velocidade,0,0);
  else if(Estado == 2)
    setMotor(velocidade,velocidade,1,1);
  else if(Estado == 3)
    setMotor(velocidade,velocidade/2,0,0);
  else if(Estado == 4)
    setMotor(velocidade/2,velocidade,0,0);		    		    		
  else if(Estado == 5)
    setMotor(velocidade,velocidade,0,1);		    		    		
  else if(Estado == 6)
    setMotor(velocidade,velocidade,1,0);
  else if(Estado == 7)
    setMotor(velocidade,velocidade/2,1,1);    
  else 
    setMotor(velocidade/2,velocidade,1,1);    
}

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
	if (event == ADB_CONNECTION_RECEIVE)   // If recieve data
	{
 		Estado = data[1];
	}
}
