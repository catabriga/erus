#ifndef ULTRASOUND_h
#define ULTRASOUND_h

void setupUltrasound(void);
void startUltrasoundCycle(void);

void loopUltrasound(void);

int ultrasoundReadingReady(void);
unsigned int* getUltrasoundValues(void);

#endif
