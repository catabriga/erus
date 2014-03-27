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
  motor[0].attach(MOTOR1_PIN);
  motor[1].attach(MOTOR2_PIN);
  motor[2].attach(MOTOR3_PIN);
  motor[3].attach(MOTOR4_PIN);
  Serial.println("Controle iniciado.");
    for(int i=0; i<4; i++) motor[i].writeMicroseconds(MAX_SIGNAL);
  Serial.println("Conecte a bateria.");
  delay(6000);
  Serial.println("Caso tenha tocado dois beeps o procedimento sucedeu.");
  Serial.println("Digite 1 para aceleracao minima, 2 para maxima.");
}

void loop(){
  while(Serial.available())
  {
    int cmd = Serial.read();
    if(cmd==49)
    {
      Serial.println("Escrevendo valor minimo");
      for(int i=0; i<4; i++) motor[i].writeMicroseconds(MIN_SIGNAL);
    }
    if(cmd==50)
    {
      Serial.println("Escrevendo valor maximo");
      for(int i=0; i<4; i++) motor[i].writeMicroseconds(MAX_SIGNAL);
    }
  }    
}
