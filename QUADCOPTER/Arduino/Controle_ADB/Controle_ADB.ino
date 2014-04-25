#include <Servo.h>
     
#define MAX_SIGNAL 2000
#define MIN_SIGNAL 700
#define MOTOR1_PIN 3
#define MOTOR2_PIN 5
#define MOTOR3_PIN 6 
#define MOTOR4_PIN 9

Servo motor[4];
     
void setup() {
  Serial.begin(9600);

//Inicialização das Portas PWM
  motor[0].attach(MOTOR1_PIN);
  motor[1].attach(MOTOR2_PIN);
  motor[2].attach(MOTOR3_PIN);
  motor[3].attach(MOTOR4_PIN);

//Inicialização do ESC
  Serial.println("Controle iniciado.");
    for(int i=0; i<4; i++) motor[i].writeMicroseconds(MIN_SIGNAL);
  Serial.println("Conecte a bateria.");
  delay(10000);
  
}

void loop(){

    motor[/*INDICE DO MOTOR*/].writeMicroseconds(/*VELOCIDADE DE 700 A 2000*/);

}
