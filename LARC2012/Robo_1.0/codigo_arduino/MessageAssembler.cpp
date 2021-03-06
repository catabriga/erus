#include "MessageAssembler.h"

#define BUFFERSIZE 255

struct messageAssembler
{
	uint8_t connectionBuffer[BUFFERSIZE];
	int nextBuffer;
	int msgSizeTable[255];
};

void initMsgSizeTable(int *msgSizeTable)
{
	int i;

	for (i = 0; i < 255; i++)
	{
		msgSizeTable[i] = 1;
	}

	msgSizeTable[0x10] = 3;	// MOTOR_FD
	msgSizeTable[0x11] = 3;	// MOTOR_FE
	msgSizeTable[0x12] = 3;	// MOTOR_TD
	msgSizeTable[0x13] = 3;	// MOTOR_TE
	msgSizeTable[0x14] = 5;	// MOTOR_UNICO	
	msgSizeTable[0x31] = 3; // CLAW_HORIZONTAL
	msgSizeTable[0x32] = 3; // CLAW_VERTICAL
	msgSizeTable[0x33] = 2; // SERVO
	msgSizeTable[0x42] = 2; // CLAW_BUTTON
	msgSizeTable[0x45] = 2; // BUZZER
}

MessageAssembler *createMessageAssembler(void)
{
	MessageAssembler* messageAssembler = (MessageAssembler*)malloc(sizeof(MessageAssembler));

	messageAssembler->nextBuffer = 0;
	initMsgSizeTable(messageAssembler->msgSizeTable);

	return messageAssembler;
}

int messagesAvaliable(MessageAssembler *message)
{
	if((message->nextBuffer > 0) && (message->nextBuffer >= message->msgSizeTable[message->connectionBuffer[0]]))
	{
		
		return 1;
	}

	return 0;
}

uint8_t* getMessage(MessageAssembler *message)
{
	if(messagesAvaliable(message))
	{
		return message->connectionBuffer;
	}

	return NULL;
}

void removeMessage(MessageAssembler *message)
{
	if(messagesAvaliable(message))
	{
		int size = message->msgSizeTable[message->connectionBuffer[0]];
		for(int i = 0; i < message->nextBuffer - size; i++)
		{
			message->connectionBuffer[i] = message->connectionBuffer[i+size];
		}
                message->nextBuffer = message->nextBuffer - size;
	}
}

int addBytes(MessageAssembler *message, uint8_t *data, int length)
{
	int i;

	if(length + message->nextBuffer <= BUFFERSIZE)
	{
		for (i = 0; i < length; i++)
		{
			message->connectionBuffer[i + message->nextBuffer] = data[i];
		}

		message->nextBuffer += length;

		return length;
	}
	else
	{
		return 0;
	}
}
