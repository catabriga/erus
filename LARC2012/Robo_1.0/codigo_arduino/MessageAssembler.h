#ifndef MESSAGEASSEMBLER_h
#define MESSAGEASSEMBLER_h

#include <stdint.h>
#include <stdlib.h>

typedef struct messageAssembler MessageAssembler;

void initMsgSizeTable(int *msgSizeTable);
MessageAssembler *createMessageAssembler(void);
int messagesAvaliable(MessageAssembler *message);
uint8_t* getMessage(MessageAssembler *message);
void removeMessage(MessageAssembler *message);
int addBytes(MessageAssembler *message, uint8_t *data, int length);

#endif
