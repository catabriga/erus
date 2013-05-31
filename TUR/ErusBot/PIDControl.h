#ifndef PIDCONTROL_H
#define PIDCONTROL_H

void setupPIDControl(int kpp, int kip, int kdp);
int getPIDControl(int erro);

#endif
