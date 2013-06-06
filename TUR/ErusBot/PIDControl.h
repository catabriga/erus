#ifndef PIDCONTROL_H
#define PIDCONTROL_H

void setupPIDControl(double kpp, double kip, double kdp);
int getPIDControl(int erro);

#endif
