#define BYTES_IN_WORD   4
#define WORDS_IN_KEY    4
#define NR_ROUNDS      10
#define BLOCK_SIZE     16
#define NRMAX		  100

#include <stdlib.h>
#include <stdio.h>
#include <inttypes.h>

uint8_t xtime(uint8_t x);
uint8_t multiplyF_256(uint8_t a, uint8_t b);
void addRoundKey(uint8_t r, uint8_t state[4][4], int id);
void subBytes(uint8_t state[4][4]);
void mixColumns(uint8_t state[4][4]);
void shiftRows(uint8_t state[4][4]);
void printState(uint8_t state[4][4]);


void roundKeyGen(uint8_t roundKey[NR_ROUNDS+1][WORDS_IN_KEY][BYTES_IN_WORD], uint8_t Key[BLOCK_SIZE]);

void AESSetup(int id, uint8_t Key[]);

void encryptAES(uint8_t buf[], int id);

void XOR(uint8_t buf[BLOCK_SIZE], uint8_t vec[BLOCK_SIZE]);

void encryptCFB(uint8_t buf[] ,int inlength, int id);

void decryptCFB(uint8_t buf[], int inlength, int id);

void print8bit(uint8_t byte);

