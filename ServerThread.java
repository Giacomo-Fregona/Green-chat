import java.io.*;
import java.net.*;
import java.util.ArrayList;
import static java.lang.Math.ceil;
import static java.lang.Math.pow;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ServerThread implements Runnable {

//  ------------ ATTRIBUTES ------------
    
    private byte[] id;//    The ID of the corresponding Client
    private ArrayList<byte[]> feed;//   The feed of the corresponding Client, i.e. the (transient) list of all the news that will be sent to it in the next Update 

    private AEScipher cipher;
    private byte Key[];

//  Connection attributes
    private Socket socket;
    private Server server;
    private DataOutputStream writer;
    private DataInputStream reader;


//  ------------ CONSTRUCTORS ------------
    
    public ServerThread(){
    }
    
    public ServerThread (Server s, Socket so){
        this.server = s;
        this.socket = so;
    }
    
    
//  ------------ METHODS ------------

//  Sending a bytearray through the channel using our padding and encryption procedure
    public void Send(byte[] message) throws Exception {
        byte[] paddedMessage = cipher.Padding(message);
        cipher.Encrypt(paddedMessage);
        for(int i =0; i<paddedMessage.length; i++){this.writer.writeByte(paddedMessage[i]);}
    }
    
    
//  Receiving a message from the client
    private byte[] Receive() throws Exception {
    
        byte[] block = new byte[16];//  Buffer for the block in process
        
//      Processing the first block
        for(int i=0; i<16; i++){
            block[i] = this.reader.readByte();
        }
        
//      Reading the length of the message stored in the padding bits
        cipher.Decrypt(block);
        int l = cipher.ReadLengthFromFirstBlock(block);
        int numBlocks = (int) Math.ceil((l+3.0)/16.0);  
        
        byte[] paddedMessage = new byte[16*numBlocks];

//      Writing the first block
        for(int i=0; i<16; i++){
            paddedMessage[i] = block[i];
        }
        
//      Processing the other blocks
        for (int j=1; j<numBlocks; j++){
            for(int i=0; i<16; i++){
                block[i] = this.reader.readByte();
            }
            cipher.Decrypt(block);
            for(int i=0; i<16; i++){
                paddedMessage[j*16+i] = block[i];
            }   
        }
        
        return cipher.Depadding(paddedMessage,l);
    }
    
//  Processing the received message got from the client . The return is a feedback (false only if the user leaves the chat or if there are some errors in the transmission)
    public boolean Do(byte[] receivedMessage) throws Exception {
    
        byte flag = receivedMessage[0];//   The first byte of each received message is a flag value that specifies how to read the content of the message. Are we looking to an access request (0x00), a query from the client looking for news (0x01), a public (0x02) or private (0x03) message or is the user leaving the chat(0xff)?
        
//      Utility
        byte[] fail = {(byte)0};
        byte[] success = {(byte)1};
    
//      We behave depending on the flag value
        switch(flag){
        
            case (byte)0x00://     The client entered our chat
                {   
                    System.out.println("\nA new user is trying to connect..");
//                  Getting its ID
                    byte[] message = new byte[receivedMessage.length -1];
                    for(int i = 1; i<receivedMessage.length; i++){
                        message[i-1] = receivedMessage[i];
                    }

//                  Verifying the ID is not actually the name of another user
                    ArrayList<byte[]> clientIds = server.GetClientIds();
                    boolean temp = true;
                    try{
                    	server.GetIndexOf(message);
                    } catch (Exception ex) {
                    	temp = false;
                    }
                    
                    if(temp && clientIds.size() != 0){
                        Send(fail);//   The ID is already in use! Please try again to autenticate
                        System.out.println("ID refused! Connection failure.");
                    } else {
                    
//                      We can add the new user to the server variables and the access response to the client
                        Send(success);
                        server.AddClient(message, this);
                        
                        id = message;// We set the ServerThread ID
                        
                        System.out.println(String.format("New user accepted with ID = %s.", new String(id, StandardCharsets.UTF_8)));
                        
//                      We notify to each already in the chat user that we havce a new partcipant
                        for(int i = 0; i<clientIds.size()-1; i++){
                            server.GetServerThread(i).feed.add(receivedMessage);
                        }
                        
//                      We add the paerticipants list to the feed of the user
                        for(int i = 0; i<clientIds.size()-1; i++){
                            byte[] feedElement = new byte[server.GetClientId(i).length+1];
                            feedElement[0] = (byte) 0x00;
                            for(int j = 0; j< server.GetClientId(i).length; j++){
                                feedElement[1+j] = server.GetClientId(i)[j];
                            }
                            server.GetServerThread(id).feed.add(feedElement);
                        }
                    }
                    
                    return true;
                }
                
                
            case (byte)1://     The client asks for news
				{   
				    System.out.println("\nStarting Update of user "+ new String(id, StandardCharsets.UTF_8)+"..");
				    
//				    We send to the client the length of the feed we are goint to push through the channel in format feedLength = 0x01 + length written with 3 bytes
		        	int l = feed.size();
					byte[] feedLength = new byte[4];
					System.out.println(String.format("We have %d available updates!",l));
					feedLength[0] = (byte)1;
					for(int i = 0; i<3; i++){
						feedLength[3-i] = (byte)(l % ((int)Math.pow(2,8)));
						l /= (int) Math.pow(2,8);
					}
					Send(feedLength);
					
		        
//		        	We send the feed elements in the same order as we remove them from the Arraylist (Last-In-First-Out)
		        	l = feed.size();//  restores the original value of l
		        	for(int i = l-1; i>=0; i--){
		        	    System.out.println(String.format("Sending update n° %d..",i));
		        		Send(feed.remove(i));
		        	}
		        	
		        	System.out.println("End of Update procedure.");
		            return true;
                }
                  
                                
            case (byte)2://     The client sends a public message
                {   
                    System.out.println("\nUser "+ new String(id, StandardCharsets.UTF_8)+" sent the message: '"+ new String(Arrays.copyOfRange(receivedMessage, 1, receivedMessage.length), StandardCharsets.UTF_8)+"'."); 
                    
//                  We construct the array mess in the following format: 0x02 + sender ID length in 3 bytes + sender ID + content of the message        
                    byte[] mess = new byte[3+id.length+receivedMessage.length];
                    mess[0] = (byte)2;
                    int l = id.length;
                    for(int i = 0; i<3; i++){
                        mess[2-i+1] = (byte)(l % ((int)Math.pow(2,8)));
                        l /= (int) Math.pow(2,8);
                    }
                    for(int i = 0; i< id.length; i++){
                        mess[i+4] = id[i];
                    }
                    for(int i = 1; i< receivedMessage.length; i++){
                        mess[i+3+id.length] = receivedMessage[i];
                    }
                    
//                  We add mess to the feed of each client (excluding the client that sent the message)
                    ArrayList<byte[]> clientIds = server.GetClientIds();
                    for(int i = 0; i<clientIds.size(); i++){
                        if(id != clientIds.get(i)) server.GetServerThread(i).feed.add(mess);
                    }
                    
                   	return true;
                }
                
                
            case (byte)3://     The client sends a private message
                {   
//                  We extract the recipient from the message
                    int l = 0;
                    for(int i = 2; i>=0; i--){
                        l += (int) receivedMessage[3-i]*((int)Math.pow(2,8*i));
                    }
                    byte[] recipient = new byte[l];
                    for(int i = 0; i<l; i++){
                        recipient[i] = receivedMessage[4+i];
                    }
                    
//                  We construct the array mess in the following format: 0x03 + sender ID length in 3 bytes + sender ID + content of the message 
                    byte[] mess = new byte[id.length+receivedMessage.length-l];
                    mess[0] = (byte)3;
                    int len = id.length;
                    for(int i = 0; i<3; i++){
                        mess[2-i+1] = (byte)(len % ((int)Math.pow(2,8)));
                        len /= (int) Math.pow(2,8);
                    }
                    for(int i = 0; i< id.length; i++){
                        mess[i+4] = id[i];
                    }
                    for(int i = 0; i< receivedMessage.length-4-l; i++){
                        mess[i+4+id.length] = receivedMessage[4+l+i];
                    }
                    
//                  We add mess to the feed of the recipient
                    server.GetServerThread(recipient).feed.add(mess);   
                    
                    System.out.println("\nUser "+ new String(id, StandardCharsets.UTF_8)+"sent the private message: '"+ new String(Arrays.copyOfRange(receivedMessage, 1, receivedMessage.length), StandardCharsets.UTF_8)+"' to user "+ new String(recipient, StandardCharsets.UTF_8)+"."); 
                
                	return true;
                }
                
                
            case (byte)0xff://      The client is leaving the chat
            	{   
            	    System.out.println("\nBye bye received from user " + new String(id, StandardCharsets.UTF_8)+ ".");
            	    
//            	    We construct a bye bye message to inform the other users that our client is leaving the chat. The bye bye message has format: 0xff + ID of the leaving client
            		byte[] byeBye = new byte[id.length+1];
            		byeBye[0] = (byte)0xff;
            		for(int i = 0; i<id.length; i++){
            			byeBye[i+1] = id[i];
            		}
            		
//            		We add the bye bye message to the feed of each connected user
            		ArrayList<byte[]> clientIds = server.GetClientIds();
                    for(int i = 0; i<clientIds.size(); i++){
                        if(id != clientIds.get(i)) server.GetServerThread(i).feed.add(byeBye);
                    }
            		
            		server.RemoveClient(id);
            		System.out.println("User disconnected successfully.");
            		
					return false;            
        		}
        
        
            default:
                return false;
        }    
    }


    public void run(){

        try {
          
            this.writer = new DataOutputStream(socket.getOutputStream());
            this.reader = new DataInputStream(socket.getInputStream());
            
                        
//          Reading the encapsulated key sent by the client. We are using Kyber KEM for sharing AES 128 keys
//          Reading the length of the encapsulated key
            byte[] len = new byte[3];
            for(int i = 0; i < 3; i++){
                len[i] = reader.readByte();
            }
            
//          Reading the encapsulated key
            int l = KeyGeneration.rLen(len);
            byte[] encapsulatedKey = new byte[l];
            for(int i = 0; i < l; i++){
                encapsulatedKey[i] = reader.readByte();
            }
            
//          Decapsulating the key
            this.Key = KeyGeneration.AESdecapsulation(encapsulatedKey);
            
//          Initialization and setup of our cipher
            this.cipher = new AEScipher();
            this.cipher.Setup(Key);
            
//          Initialization of variables regarding the chat
            this.feed = new ArrayList<byte[]>();
            
//          Staying ready to react to the client requests. Only a disconnection or an exception will stop the while loop
            boolean ok = true;
            while(ok){
            	byte[] received = Receive();
            	ok = Do(received);
            }

//          Closing the Socket channel objects
            writer.close();
            reader.close();

        } catch(Exception ex) {
            System.out.println("Forced disconnection of user "+new String(id, StandardCharsets.UTF_8));
        }

    }

}
