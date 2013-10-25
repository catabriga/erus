#ifndef ERUS_PINS_H
#define ERUS_PINS_H

//Pinos dos Motores para Movimentação
#define DIR_R1 35 //Direita
#define DIR_R2 37
#define PWM_R 9 
#define DIR_L1 31 //Esquerda
#define DIR_L2 33
#define PWM_L 10

//Pinos do Motor da Vassoura
#define DIR_VAS 34
#define PWM_VAS 11

#define VIB_PIN 36 //VIBRADOR
#define VIB_PWM 12

#define SERVO_PIN 23
#define START_BUTTON 24
#define OBSTACLE_BUTTON 49

// TIMER4 is being used for newping, so cant drive PWM at these pins
#define DONT_USE_PIN_6_FOR_PWM 6
#define DONT_USE_PIN_7_FOR_PWM 7
#define DONT_USE_PIN_8_FOR_PWM 8

//INFRARED
#define INFRARED A0

//Ultrasons
#define US_TRIG_RIGHT 45
#define US_ECHO_RIGHT 43
#define US_TRIG_LEFT 28
#define US_ECHO_LEFT 26
//#define US_TRIG_CENTER_DOWN 47
//#define US_ECHO_CENTER_DOWN 49
#define US_TRIG_CENTER_UP 38
#define US_ECHO_CENTER_UP 40
#define US_TRIG_RIGHT_BACK 30
#define US_ECHO_RIGHT_BACK 32
#define US_TRIG_LEFT_BACK 44
#define US_ECHO_LEFT_BACK 42

#endif
