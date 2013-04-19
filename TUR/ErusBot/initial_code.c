int LDR1_Pin = A0; //analog pin 0
int LDR2_Pin = A1; //analog pin 0
int LDR3_Pin = A2; //analog pin 0
int LDR4_Pin = A3; //analog pin 0
int LDR5_Pin = A4; //analog pin 0
 
void setup(){
    Serial.begin(9600);

    pinMode(13,OUTPUT);
}
 
void loop(){
    int LDR1 = analogRead(LDR1_Pin);
    int LDR2 = analogRead(LDR2_Pin);
    int LDR3 = analogRead(LDR3_Pin);
    int LDR4 = analogRead(LDR4_Pin);
    int LDR5 = analogRead(LDR5_Pin);

    int LDR12 = map(LDR1, 0, 50, 0, 1);
    int LDR22 = map(LDR2, 0, 50, 0, 1);
    int LDR32 = map(LDR3, 0, 50, 0, 1);
    int LDR42 = map(LDR4, 0, 50, 0, 1);
    int LDR52 = map(LDR5, 0, 50, 0, 1);

    digitalWrite(13,HIGH);
    Serial.print(LDR12);
    Serial.print(" / ");
    Serial.print(LDR22);
    Serial.print(" / ");
    Serial.print(LDR32);
    Serial.print(" / ");
    Serial.print(LDR42);
    Serial.print(" / ");
    Serial.println(LDR52);
    delay(100);
}
