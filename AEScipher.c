#include <stdlib.h>
#include <stdio.h>
#include <inttypes.h>
#include "AESLib.h"

#include "AEScipher.h"

//Setup method
JNIEXPORT void JNICALL Java_AEScipher_Setup (JNIEnv *env, jobject obj, jbyteArray keyin){
    
//  Getting the key from the java method
    jbyte *key = (*env)->GetByteArrayElements(env,keyin,0);

//  Getting the id of the AEScipher java object
    jint jid;
    jfieldID fid;
    jclass cls = (*env)->GetObjectClass(env, obj);
    fid = (*env)->GetFieldID(env, cls, "id","I");
    if (fid == NULL) {
        return;
    };
    jid = (*env)->GetIntField(env, obj, fid);

//  Setup defined in AESLib. The keyschedule is already computed by the Setup procedure and the round keys stored. The key will not be taken again in input by the encryption and decryption functions
    AESSetup(jid, key);

    (*env)->ReleaseByteArrayElements(env, keyin, key, 0);
    
    return;   
}

//Encryption method
JNIEXPORT void JNICALL Java_AEScipher_Encrypt (JNIEnv *env, jobject obj, jbyteArray text){
    
//  Getting the java input plaintext 
    jbyte *plaintext = (*env)->GetByteArrayElements(env,text,0);
    jsize plaintextLength = (*env)->GetArrayLength(env,text);
    
//  Getting the id of the AEScipher java object
    jint jid;
    jfieldID fid;
    jclass cls = (*env)->GetObjectClass(env, obj);
    fid = (*env)->GetFieldID(env, cls, "id","I");
    if (fid == NULL) {
        return;
    };
    jid = (*env)->GetIntField(env, obj, fid);

//  encryptCFB defined in AESLib
    encryptCFB(plaintext, plaintextLength/16, jid);

    (*env)->ReleaseByteArrayElements(env, text, plaintext, 0);
    
    return; 
}


JNIEXPORT void JNICALL Java_AEScipher_Decrypt (JNIEnv *env, jobject obj, jbyteArray text){

//  Getting the java input ciphertext 
    jbyte *ciphertext = (*env)->GetByteArrayElements(env,text,0);
    jsize ciphertextLength = (*env)->GetArrayLength(env,text);
    
//  Getting the id of the AEScipher java object
    jint jid;
    jfieldID fid;
    jclass cls = (*env)->GetObjectClass(env, obj);
    fid = (*env)->GetFieldID(env, cls, "id","I");
    if (fid == NULL) {
        return;
    };
    jid = (*env)->GetIntField(env, obj, fid);

//  decryptCFB defined in AESLib
    decryptCFB(ciphertext, ciphertextLength/16, jid);

    (*env)->ReleaseByteArrayElements(env, text, ciphertext, 0);
    
    return;
}

