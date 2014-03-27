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
  delay(3000);
  motor[0].attach(MOTOR1_PIN);
  motor[1].attach(MOTOR2_PIN);
  motor[2].attach(MOTOR3_PIN);
  motor[3].attach(MOTOR4_PIN);
  Serial.println("Controle iniciado.");
    for(int i=0; i<4; i++) motor[i].writeMicroseconds(MAX_SIGNAL);
  Serial.println("Conecte a bateria.");
  delay(3000);
  Serial.println("Aperte 1 apos os beeps de confirmacao.");
}

void loop(){
  int panic = Serial.read();
  if(panic == 49)
  {
    for(int j=0; j<4; j++) motor[j].writeMicroseconds(MIN_SIGNAL);
    Serial.println("Calibrado");
    delay(100000);
  }
}
