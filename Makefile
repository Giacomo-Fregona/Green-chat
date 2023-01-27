all: Server.class ClientApplication.class 

KeyGeneration.class: KeyGeneration.java
	javac KeyGeneration.java

ClientApplication.class: ClientApplication.java Client.class KeyGeneration.class
	javac ClientApplication.java

Client.class: Client.java AEScipher.class 
	javac Client.java
	
ServerThread.class: ServerThread.java AEScipher.class KeyGeneration.class
	javac ServerThread.java

Server.class: Server.java ServerThread.class
	javac Server.java

AEScipher.class: AEScipher.java AEScipher.h AESLib.o libAESLib.so
	javac AEScipher.java 	

AEScipher.h: AEScipher.java
	javah AEScipher

AESLib.o: AESLib.c
	gcc -c -fPIC AESLib.c	

libAESLib.so: AEScipher.c AESLib.o AEScipher.h
	gcc -I/usr/lib/jvm/jdk-8u202-linux-x64/include -I/usr/lib/jvm/jdk-8u202-linux-x64/include/linux -fPIC -shared -z noexecstack\
		AEScipher.c AESLib.o -o  libAESLib.so

