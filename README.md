# APCM Exam Project
# Valentina Astore e Giacomo Fregona

## TESTING COMPUTERS SPECIFICATIONS

The submitted project has been tested on both our computers, whose specifications are:
	
(Giacomo)                     OS: Linux Mint 21 Cinnamon Processor: Intel® Core™ i5-8250U CPU @ 1.60GHz × 4

(Valentina, Virtual Machine)  OS: Ubuntu 22.04.1 LTS     Processor: Intel® Core™ i5-7200U CPU @ 2.50GHz × 2
	
	
## INSTALLATION INSTRUCTIONS

1.	Make sure you have the Bouncy Castle provider installed in your machine (we have adopted the 1.72 version).
2.  Modify the last line of the Makefile replacing our linux path with that of the machine which is going to run the project.
3.  Compile the entire structure using the edited Makefile.
4.  Run the KeyGeneration file.
	Two files PrivateKey.key and PublicKey.key will be genrated and they will respectively contain the parameters to retrive the private key and the public key. Store the PrivateKey.key file in the machine which is going to run the Server, while share the PubliceKey.key file with each user that is going to run the Client. In both cases the files must remain or be placed in the project's folder.
5.	Make compilable the RunServer and RunClientApplication files by
 	chmod +x RunServer
	chmod +x RunClientApplication


## PROGRAM LAUNCHING INSTRUCTIONS
1.	SERVER:
	./RunServer
	
2.	CLIENT:
	If Client and Server are run over the same machine, do
	./RunClienApplication
	
	Otherwise, modify the RunClienApplication file by adding the ip of the server.
	Example: java -Djava.library.path=. ClientApplication 127.0.0.1
	Then do
	./RunClienApplication
