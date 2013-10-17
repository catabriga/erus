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

#define VIB_PIN 23 //VIBRADOR
#define SERVO_PIN 2
#define START_BUTTON 24

// TIMER4 is being used for newping, so cant drive PWM at these pins
#define DONT_USE_PIN_6_FOR_PWM 6
#define DONT_USE_PIN_7_FOR_PWM 7
#define DONT_USE_PIN_8_FOR_PWM 8

//Ultrasons
#define US_TRIG_RIGHT 45
#define US_ECHO_RIGHT 43
#define US_TRIG_LEFT 44
#define US_ECHO_LEFT 42
#define US_TRIG_CENTER 47
#define US_ECHO_CENTER 49

#endif
