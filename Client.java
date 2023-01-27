import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

public class Client {

//  ----------- ATTRIBUTES ----------- //
    
    public VBox onScroll;
    
//	Chat items
    private byte[] clientId;
    private VBox publicChat;
    private ArrayList<VBox> privateChat;//     Each ArrayList<VBox> corresponds to a user different from the client user itself
    private ArrayList<Label> participants;//   Arraylist of ids. partcipants[i] and privateChat[i] refers to the same user.
    private boolean firstUpdate = true;
    public ArrayList<Boolean> newInUser = new ArrayList<Boolean>();  //  Each boolean value corresponds to a user. True = we have new messages to see. False = no new messages
    public boolean newInPublic = false;//   True = we have new messages to see in the group chat. False = no new messages in the group chat
    public boolean newInPrivate = false;//   True = we have new messages to see in a private chat. False = no new messages in any private chat

//  Cipher
    private AEScipher cipher;
    private byte Key[];
    
//  Writing/Reading from the Socket
    private DataOutputStream writer;
    private DataInputStream reader;
    
    
//  ----------- METHODS ----------- //
    
    public ArrayList<Label> GetParticipants(){return participants;}
    
    public byte[] GetClientId(){return clientId;}
    
    public VBox GetPublicChat(){return publicChat;}
    
    public ArrayList<VBox> GetPrivateChat(){return privateChat;}
    
//  Getting the index of the specified user ID
    public int GetIndexOf(String stringId) throws Exception {
		for(int i = 0; i<participants.size(); i++){
            if(stringId.equals(participants.get(i).getText())){
                return i;
            }
        }
        throw new Exception("Element "+ stringId +"not in participants!");
    }
    
        
//  Updates the values of newInPrivate and newInUser when visualizing a new private message
    public void PrivateMessageSeen(int index){
        if(newInPrivate) {
            newInUser.set(index, false);
            newInPrivate = false;
            for(int i = 0; i< newInUser.size(); i++){newInPrivate = newInPrivate || newInUser.get(i);}
        }
    }
    
//  Sending a message through the Socket using proper padding and encryption functions
    private void Send(byte[] message)throws Exception{
        byte[] paddedMessage = cipher.Padding(message);
        cipher.Encrypt(paddedMessage);
        
        for(int i =0; i<paddedMessage.length; i++){
        	this.writer.writeByte(paddedMessage[i]);
        }
    }
    
//  Receiving a message from the channel
    private byte[] Receive() throws Exception {
        byte[] block = new byte[16];//  Buffer for the block in process
        
//      Processing the first block
        for(int i=0; i<16; i++){
            block[i] = this.reader.readByte();
        }
        cipher.Decrypt(block);
        
//      Reading the length of the message stored in the padding bits
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
    
    
//  Connecting to the server
    public boolean Access(byte[] id) throws Exception {
        
        byte[] hello = new byte[id.length+1];
        for(int i = 0; i<id.length; i++){hello[i+1] = id[i];}
//      The input message "hello" is b+id, where b is a random byte to overwrite with the encoding of the action to perform and id is the user's identifier
        hello[0] = (byte)0x00;
        Send(hello);
        
        if (Receive()[0] == (byte)1) {	//Receive is 1 if the access succeeded and 0 otherwise
        	this.clientId = id;
        	Update();
        	firstUpdate = false;
        	return true;
        }
        return false;
    }
    
//  Getting news from our feed stored in the server
    public void Update() throws Exception {
    
		byte[] query = {(byte)1};
        Send(query);//  Asking for news
        
//      Receiving the number of the updates that will be sent
        byte[] len = Receive();
        int feedLength = 0;
        for(int i = 2; i>=0; i--){
            feedLength += (int) len[3-i]*((int)Math.pow(2,8*i));
        }
//    	Receiving and storing feed elements Last-In-First-Out
		ArrayList<byte[]> feed = new ArrayList<byte[]>();
		for(int k = 0; k<feedLength; k++){
			byte[] feedElement = Receive();
			feed.add(feedElement);
		}
		
//		Processing the feed
		for(int k = 0; k<feedLength; k++){
			byte[] feedElement = feed.remove(feed.size()-1);
			byte flag = feedElement[0];
		    switch(flag){
		        case (byte)0://     A new user entered the chat!
		            {
//		                Getting its ID
		                byte[] id = new byte[feedElement.length -1];
		                for(int j = 1; j<feedElement.length; j++){
		                    id[j-1] = feedElement[j];
		                }
		                
		                if(firstUpdate){// this is the first update of the client.
		                
//		                    Adding its ID to the list of the participants
		                	participants.add(new Label(new String(id, StandardCharsets.UTF_8)));
		                	newInUser.add(false);
		                	
//		                	Adding in the group chat a notification that we have already users in the chat
		                	TextArea ta = new TextArea(new String(id, StandardCharsets.UTF_8)+" is in the chat.");
		                	ta.setWrapText(true);
                            ta.setId("oldUser");
							ta.setEditable(false);
							ta.setPrefWidth(500);
                            HBox h = new HBox();
                            h.setPrefWidth(840);
                            h.setMinWidth(840);
                            h.getChildren().add(ta);
                            h.setAlignment(Pos.CENTER);
							publicChat.getChildren().add(h);
							
//							Creating a new place where to store the private messages with the new user
							VBox chatToAdd = new VBox(5);
							chatToAdd.setPrefWidth(840);
                            chatToAdd.setMinWidth(840);
							privateChat.add(chatToAdd);
							
		                } else {//This code is executed in the other updates
		                
//		                    Adding its ID to the list of the participants   
		                	participants.add(new Label(new String(id, StandardCharsets.UTF_8)));
		                	newInUser.add(false);
		                	
//		                	Adding in the group chat a notification that we have a new user
		                	TextArea ta = new TextArea(new String(id, StandardCharsets.UTF_8)+" joined the chat.");
		                	ta.setWrapText(true);
                            ta.setId("newUser");
							ta.setEditable(false);
							ta.setPrefWidth(500);
                            HBox h = new HBox();
                            h.getChildren().add(ta);
                            h.setAlignment(Pos.CENTER);
							publicChat.getChildren().add(h);
							
//							Creating a new place where to store the private messages with the new user
							VBox chatToAdd = new VBox(5);
							chatToAdd.setPrefWidth(840);
                            chatToAdd.setMinWidth(840);
							privateChat.add(chatToAdd);
		                }
		            }
		            break;
		           
		            
		        case (byte)2://     We have a new message in the public chat!
		            {      
//		                Extracting the sender ID from the received bytearray
	            	    int l = 0;
		                for(int i = 2; i>=0; i--){
		                    l += (int) feedElement[3-i]*((int)Math.pow(2,8*i));
		                }
		                byte[] sender = new byte[l];
		                for(int i = 0; i<l; i++){
		                    sender[i] = feedElement[4+i];
		                }
		                
//		                Retrieving the message
		                byte[] message = new byte[feedElement.length-l-4];
		                for(int i = 0; i< feedElement.length-4-l; i++){
		                    message[i] = feedElement[4+l+i];
		                }

//                      Adding the message to the chat
						TextArea ta = new TextArea(new String(message, StandardCharsets.UTF_8));
                        ta.setWrapText(true);
                        ta.setPrefWidth(500);
                        ta.setId("receivedMessage");
                        ta.setEditable(false);
                        VBox titleV = new VBox();
                        Label senderLabel = new Label(new String(sender, StandardCharsets.UTF_8));
                        senderLabel.setId("receivedLabel");
                        titleV.getChildren().addAll(senderLabel, ta);
                        HBox h = new HBox();
                        h.getChildren().add(titleV);
                        h.setAlignment(Pos.TOP_LEFT); 
                        publicChat.getChildren().add(h);
                        
//                      We have potentially unseen messages in the public chat. We update the variable newInPublic so that they can be highlighted in the interface
                        if (onScroll != publicChat){newInPublic = true;}
		            }
		            break;

		            
		        case (byte)3://     We have a new message in a private chat!
		            {
//		                Extracting the sender ID from the received bytearray
	            	    int l = 0;
		                for(int i = 2; i>=0; i--){
		                    l += (int) feedElement[3-i]*((int)Math.pow(2,8*i));
		                }
		                byte[] sender = new byte[l];
		                for(int i = 0; i<l; i++){
		                    sender[i] = feedElement[4+i];
		                }
		                
//		                Retrieving the message
		                byte[] message = new byte[feedElement.length-l-4];
		                for(int i = 0; i< feedElement.length-4-l; i++){
		                    message[i] = feedElement[4+l+i];
		                }
                    	
                    	//  Adding the message in the corresponding chat
                    	TextArea ta = new TextArea(new String(message, StandardCharsets.UTF_8));
                        ta.setWrapText(true);
                        ta.setPrefWidth(500);
                        ta.setId("receivedMessage");
                        ta.setEditable(false);
                        VBox titleV = new VBox();
                        Label senderLabel = new Label(new String(sender, StandardCharsets.UTF_8));
                        senderLabel.setId("receivedLabel");
                        titleV.getChildren().addAll(senderLabel, ta);
                        HBox h = new HBox();
                        h.getChildren().add(titleV);
                        h.setAlignment(Pos.TOP_LEFT); 
                        int index = GetIndexOf(new String(sender, StandardCharsets.UTF_8));
                        privateChat.get(index).getChildren().add(h);
                        
//                      We have potentially unseen messages in the private chat. We update the variable newInPublic so that they can be highlighted in the interface
                        if(onScroll != privateChat.get(index)){newInUser.set(index, true); newInPrivate = true;}
                	}
		            break;

		            
		        case (byte)0xff://     A user left the chat!		        
					{   
//					    Obtaining its ID
				    	byte[] sender = new byte[feedElement.length-1];
			            for(int i = 0; i<feedElement.length-1; i++){
			                sender[i] = feedElement[1+i];
			            }
			                
//			            Removing the user from our lists and adding the bye-bye message to the public chat 
		                int i = GetIndexOf(new String(sender, StandardCharsets.UTF_8));
	                	participants.remove(i);
	                	privateChat.remove(i);
	                	newInUser.remove(i);				
						
//						Notifying the user the new
	                	TextArea ta = new TextArea(new String(sender, StandardCharsets.UTF_8)+" left the chat.");
	                	ta.setWrapText(true);
                        ta.setId("newUser");
						ta.setEditable(false);
						ta.setPrefWidth(500);
                        HBox h = new HBox();
                        h.getChildren().add(ta);
                        h.setAlignment(Pos.CENTER);	                	
						publicChat.getChildren().add(h);               	
	                }
		            break;

		        default:
		            ;
		    }
		}
		
    }
    
//  Sending a new message in the public chat
    public void SendMessage(byte[] message) throws Exception {
            byte[] mess = new byte[message.length+1];
            mess[0] = (byte)2;
            
            for(int i = 0; i<message.length; i++){mess[i+1]=message[i];}
            
//			Adding the sent message to the publicChat           
            TextArea ta = new TextArea(new String(message, StandardCharsets.UTF_8));
            ta.setWrapText(true);
            ta.setPrefWidth(500);
            ta.setId("sentMessage");
            ta.setEditable(false);
            VBox titleV = new VBox();
            Label senderLabel = new Label("You");
            senderLabel.setId("sentLabel");
            titleV.getChildren().addAll(senderLabel, ta);
            HBox h = new HBox();
            h.getChildren().add(titleV);
            h.setAlignment(Pos.TOP_RIGHT); 
            publicChat.getChildren().add(h);
            
            Send(mess); //the sent message is b+m, where b is the 0x02 byte with the encoding of the action to perform and m is the message to be sent in byte        
    }

//  Sending a new message in the private chat of a specified recipient
    public void SendPrivateMessage(byte[] message, byte[] recipient) throws Exception {
            
//          Constructing the message to be sent in the form 0x03 + length of the recipient ID written as 3 byte number + recipient ID + content of the message
            byte[] mess = new byte[4+recipient.length+message.length];
            mess[0] = (byte)3;
            int l = recipient.length;
            for(int i = 0; i<3; i++){
                mess[2-i+1] = (byte)(l % ((int)Math.pow(2,8)));
                l /= (int) Math.pow(2,8);
            }
            for(int i = 0; i< recipient.length; i++){
                mess[i+4] = recipient[i];
            }
            for(int i = 0; i< message.length; i++){
                mess[i+4+recipient.length] = message[i];
            }
            
//			Adding the sent message to the privateChat
            TextArea ta = new TextArea(new String(message, StandardCharsets.UTF_8));
            ta.setWrapText(true);
            ta.setPrefWidth(500);
            ta.setId("sentMessage");
            ta.setEditable(false);
            VBox titleV = new VBox();
            Label senderLabel = new Label("You");
            senderLabel.setId("sentLabel");
            titleV.getChildren().addAll(senderLabel, ta);
            HBox h = new HBox();
            h.getChildren().add(titleV);
            h.setAlignment(Pos.TOP_RIGHT); 
            privateChat.get(GetIndexOf(new String(recipient, StandardCharsets.UTF_8))).getChildren().add(h);

            Send(mess);         
             
    }

//  Exiting from the chat sending a bye bye message
    public void Exit() throws Exception {
    	byte[] byeBye = {(byte) 0xff};
    	Send(byeBye);
    }
    
//  Closes the Socket stream objects
    public void close(){
        try{
            writer.close();
            reader.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
//  Launching our Client
    public void launch(String ip){
        try {
//          Connecting the server
            Socket socket = new Socket(ip,2023);
            
//          Initialization of channel stream writer/reader
            this.writer = new DataOutputStream(socket.getOutputStream());
            this.reader = new DataInputStream(socket.getInputStream());
            
//          Symmetric key generation. We use Kyber KEM for gerating AES 128 keys
            this.Key = new byte[16];
            byte[][] KeyEncapsulation = KeyGeneration.AESencapsulation();
            Key = KeyEncapsulation[0];
            byte[] encapsulatedKey = KeyEncapsulation[1];
            
//          Sending the encapsulated key
            byte[] len = KeyGeneration.wLen(encapsulatedKey);
            
            for(int i = 0; i < 3; i++){
                writer.writeByte(len[i]);
            }
            
            for(int i = 0; i < encapsulatedKey.length; i++){
                writer.writeByte(encapsulatedKey[i]);
            }
            
//          Initialization and setup of our cipher
            this.cipher = new AEScipher();
            this.cipher.Setup(Key);
            
//          Initialization of variables regarding the chat
            this.publicChat = new VBox(5);
            publicChat.setPrefWidth(840);
            publicChat.setMinWidth(840);
            this.privateChat = new ArrayList<VBox>();
            this.participants = new ArrayList<Label>();
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
