# Welcome to the Green Chat! :green_circle:
Hi! We are [Valentina Astore](https://github.com/Valentina-Astore) :tipping_hand_woman: and [Giacomo Fregona](https://github.com/Giacomo-Fregona) :red_haired_man: and this is Green Chat, our project submission for the course "Advance Programming of Cryptographic Methods", 2022-2023, UniTN :mountain::mountain_snow:. The content of this repository is the result of team work: we equally contributed to its realization .

## What is Green Chat? :green_circle:

It is a simple chat application based on a client-server architecture for group or private communication. The focus of our work is on the confidentiality of the exchanged messages, that are secured using modern cryptographic primitives :closed_lock_with_key: (CRYSTALS-Kyber, AES). We have also designed a simple and greenish JavaFX interface, we hope you can find it nice looking :wink:.

We have written a small [report](https://github.com/Giacomo-Fregona/Green-chat/blob/main/Report.pdf) containing more details about our specifications, in particular regarding security issues and our simple network protocols.

	
## INSTALLATION INSTRUCTIONS

1.	Make sure you have the [Bouncy Castle](https://www.bouncycastle.org/latest_releases.html) :european_castle: crypto provider installed in your machine (we have adopted the 1.72 version).
2.  Modify the last line of the Makefile replacing our linux path with that of the machine which is going to run the project.
3.  Compile the entire structure using the edited Makefile.
4.  Run the KeyGeneration file.	Two files PrivateKey.key and PublicKey.key :key: will be genrated and they will respectively contain the parameters to retrive the private key and the public key. Store the PrivateKey.key file in the machine which is going to run the Server, while share the PubliceKey.key file with each user that is going to run the Client. In both cases the files must remain or be placed in the project's folder.
5.	Make compilable the RunServer and RunClientApplication files :nerd_face: by

 	chmod +x RunServer
	
	chmod +x RunClientApplication


## PROGRAM LAUNCHING INSTRUCTIONS :keyboard:

1.	SERVER:
	./RunServer
	
2.	CLIENT:
	If Client and Server are run over the same machine, do
	./RunClienApplication
	
	Otherwise, modify the RunClienApplication file by adding the ip of the server.
	Example: java -Djava.library.path=. ClientApplication 127.0.0.1
	Then do
	./RunClienApplication
