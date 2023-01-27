import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

import javafx.application.Application;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.Node;

import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import javafx.geometry.HPos;
import javafx.geometry.Pos;

import javafx.fxml.FXMLLoader;


public class ClientApplication extends Application {

    public static void main(String[] args) {
        Application.launch(ClientApplication.class, args);
    }

    @Override
    public void start(Stage welcomeStage) throws Exception {
    
        String usedFont = "Arial";

        String ip;
        if(getParameters().getUnnamed().size()>0) {
            ip=getParameters().getUnnamed().get(0);
        }
        else ip="127.0.0.1";
        Client client = new Client();
        client.launch(ip);
        
        
//      ___________________ FIRST MAIN STAGE ELEMENTS ___________________

//		Stage title: You are logged in as + titleLabel, where titleLabel contains the identifier that the user will provide during the connection establishment
        HBox chatTitle = new HBox();
        Text titleText = new Text("You are logged in as ");        
        Label titleLabel = new Label();
        
        titleLabel.setId("titleLabel");
        titleText.setId("chatTitle");
        
//		Secondary stage: participantsStage shows the current online participants
        Stage participantsStage = new Stage();

//     	___________________ WELCOME STAGE ___________________

        Stage chatStage = new Stage();
        
//      WELCOME TEXT CONTROLLERS
        
//		Stage title
        Text welcomeText = new Text("Welcome to the Green Chat!");
        welcomeText.setFont(Font.font(usedFont,FontWeight.BOLD,40));
        
//		Getting the user identifier
        Text idRequest = new Text("Please insert your name.");
        idRequest.setFont(Font.font(usedFont, 25));
        
        TextField idField = new TextField();
        idField.setPrefHeight(40);
        idField.setFont(Font.font(usedFont, 25));
        
//		Connecting to server
        Button connect = new Button("Connect!");
        connect.setPrefSize(155,60);
        connect.setDefaultButton(true);
        connect.setOnAction(new EventHandler<ActionEvent>() {

            @Override 
            public void handle(ActionEvent e) {
                byte[] id = idField.getText().getBytes(StandardCharsets.UTF_8);

                if (id.length == 0) {
                    idRequest.setText("Invalid username! Please try again.");// Empty strings as username are rejected
                } else {
                
                    try{
                        if (client.Access(id) == false){	// The given id is already in use
                            idField.clear();
                            idRequest.setText("This name is already used! Please try again.");
                        } else {
	                        System.out.println("Connection established with ID = "+ new String(id, StandardCharsets.UTF_8));
                            welcomeStage.close();

                            chatStage.show();
                            titleLabel.setText(new String(client.GetClientId(),StandardCharsets.UTF_8));
                            VBox titleTextBox = new VBox();
                            titleTextBox.getChildren().add(titleText);
                            titleTextBox.setAlignment(Pos.CENTER);
                            chatTitle.getChildren().addAll(titleTextBox, titleLabel);
                        }   
                        
                    } catch (Exception ex) {
		                welcomeText.setText("Bye bye from the Green Chat!");
		                idRequest.setText("Something went wrong, please try again");
		                ex.printStackTrace();
                    }
                } 
            }
        });
        
        
//		WELCOME SCENE LAYOUT
        
        GridPane welcomeRoot = new GridPane();
        
        welcomeRoot.add(welcomeText,1,4);
        welcomeRoot.add(idRequest,1,6);
        welcomeRoot.add(idField,1,7);
        welcomeRoot.add(connect,1,8);

        welcomeRoot.setHalignment(connect,HPos.CENTER);
        welcomeRoot.setHalignment(idField,HPos.CENTER);
        welcomeRoot.setHalignment(welcomeText,HPos.CENTER);
        welcomeRoot.setVgap(20);
        welcomeRoot.setHgap(50);        
        
        
//      WELCOME SCENE
        
        Scene welcomeScene = new Scene(welcomeRoot);
        welcomeStage.setScene(welcomeScene);
        welcomeScene.getStylesheets().add(getClass().getResource("welcomeStyle.css").toExternalForm());
		welcomeStage.setWidth(740);
        welcomeStage.setHeight(400);
        welcomeStage.setResizable(false);
        welcomeStage.show();
        
        
//      __________________ CHAT STAGE ___________________
       
//      CHAT CONTROLLERS

//		Controllers declaration and initialization
//		See CHAT SCENE section below for further details about the buttons functioning (look at their setOnAction functions)
        Button send = new Button("Send");
        send.setPrefSize(155,50);
        
        Button refresh = new Button("Refresh");
        refresh.setPrefSize(155,50);
        
        Button showParticipants = new Button("Show users");
        showParticipants.setPrefSize(155,50);
        showParticipants.setId("noNews");
        
        Button quit = new Button("Quit");
        quit.setPrefSize(155,50);
        quit.setId("quitbutton");
        
        Button groupChat  = new Button("Group chat");
        groupChat.setPrefSize(155,50);
        groupChat.setId("noNews");
        groupChat.setVisible(false);        

		Label chatLabel = new Label();
		chatLabel.setId("chatLabel");
		chatLabel.setText("Group chat");

		Text chatId = new Text();
		chatId.setId("chatId");
		chatId.setText("You are in the");

		VBox whichChat = new VBox();//	whichChat states which chat we are displaying (group chat or a private chat with some other user)
		whichChat.getChildren().addAll(chatId, chatLabel);
		
        VBox buttonsNode = new VBox(30);//	whichChat & buttons organizer
        buttonsNode.getChildren().addAll(whichChat, groupChat, showParticipants, refresh);
        buttonsNode.setAlignment(Pos.BOTTOM_LEFT);

        
//      CHAT LAYOUT: GRID PANE STRUCTURE BASED

        TextArea messageArea = new TextArea("Write here your message!");
        messageArea.setWrapText(true);
        messageArea.setOnMouseClicked(new EventHandler<MouseEvent>(){        
            @Override 
            public void handle(MouseEvent e){//	When the users clicks on the messageArea, it is cleared from the "Write here your message!" text.
                if (messageArea.getText().equals("Write here your message!")){messageArea.clear();}
            }
        });
    
    	GridPane chatGrid = new GridPane();	// Chat's scene main structure
        chatGrid.setAlignment(Pos.CENTER);
        chatGrid.setHgap(50);
        chatGrid.setVgap(30);

	    VBox chatRoot = new VBox();	// Chat's scene root
        chatRoot.getChildren().add(chatGrid);
        chatRoot.setAlignment(Pos.CENTER);
        
        client.onScroll = client.GetPublicChat();	// Identifies the currently displayed chat among the group chat or a private one

        ScrollPane messageScroll = new ScrollPane(client.onScroll);	// Display area for the chat's messages
        messageScroll.setMinSize(800,600);
        messageScroll.setPrefSize(800,600);
        messageScroll.setHbarPolicy(ScrollBarPolicy.NEVER);    	// removing the horizonatl scrollbar 
		messageScroll.setVvalue(messageScroll.getVmax());
               
        chatGrid.add(chatTitle,0,0);
        chatGrid.add(messageScroll, 0,1);
        chatGrid.add(messageArea,0,2);
        chatGrid.add(quit,1,0);
        chatGrid.add(buttonsNode,1,1);
        chatGrid.add(send,1,2);
        
        
//      CHAT SCENE

        Scene chatScene = new Scene(chatRoot);
        chatScene.getStylesheets().add(getClass().getResource("chatStyle.css").toExternalForm());
        
        chatStage.setWidth(1400);
        chatStage.setHeight(1000);      
        chatStage.setScene(chatScene);
        


//		quit button: performs the user's log-off
        quit.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                try{
                    client.Exit();
                    client.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                chatStage.close();

            }
        });

//		Anomalous quit from chat screen management
//      If the chat screen is closed from the window bar, the socket is forcibly closed and the user is disconnected from the chat 
        chatStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
                try{
                    client.Exit();
                    client.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                chatStage.close();
            }
        });
        
//		Anomalous quit from welcome screen management
//      If the welcome screen is closed from the window bar, the socket is forcibly closed 
        welcomeStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
                try{
                    client.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                welcomeStage.close();
            }
        });
        
        
//		Anomalous quit from participant screen management
//      If the chat stage is clicked, the participants stage will be closed
        chatScene.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                if (participantsStage.isShowing()){participantsStage.close();}
            }
        });
        
        
//		groupChat button: displays the (updated) gropu chat in the messageScroll
        groupChat.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                try{
                    client.onScroll = client.GetPublicChat();
                    client.Update();
                    
                    if (client.newInPrivate){
                    	showParticipants.setId("news");
                	}
            		
            		chatId.setText("You are in the");
					chatLabel.setText("Group chat");
					groupChat.setVisible(false);	// when the group chat is already displayed, the groupChat button is hidden
					groupChat.setId("noNews");	// the notification signal is removed from the groupChat button
					client.newInPublic = false;
                    messageScroll.setContent(client.onScroll);
                    messageScroll.setVvalue(messageScroll.getVmax());
                
                } catch(Exception ex){
                	ex.printStackTrace();
                }
            }
        });


//		Refresh button: performs the chat's update, collecting the new received messages from all chats and checkin if new users have connected. 
        refresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                try{
                    client.Update();
                    messageScroll.setVvalue(messageScroll.getVmax());
                                                             
					if (client.newInPrivate){showParticipants.setId("news");}//	For each user who has sent something, a highlited border will appear in the corresponding participant label, displaying the presence notifications
					if (client.newInPublic && client.onScroll != client.GetPublicChat()){groupChat.setId("news");}// the same is done with the group chat button
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
                
        
//		send button: sends both private and group messages, according to the chat which is currently displayed
        send.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                try{
                    byte[] message = messageArea.getText().getBytes(StandardCharsets.UTF_8);
                    messageArea.clear();//	after the message's acquisition, the messageArea is cleared
                    
                    if (client.onScroll == client.GetPublicChat()){
                        client.SendMessage(message);
                    } else {
                        int index = client.GetPrivateChat().indexOf(client.onScroll);//	retriving the index of the recipient in the corresponing chat participants list (stored in the Client) from the displayed chat
                        client.SendPrivateMessage(message, client.GetParticipants().get(index).getText().getBytes(StandardCharsets.UTF_8));
                    }
                    messageScroll.setVvalue(messageScroll.getVmax());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        

//      ___________________ PARTICIPANTS STAGE ___________________

//		This stage displays who is currently online in the chat
        
		Text participantsTitle = new Text();

//		showParticipants button: displays the participants list freshly updated
        showParticipants.setOnAction(new EventHandler<ActionEvent>() {            
            @Override 
            public void handle(ActionEvent e){
                try{
                	client.Update();
            	} catch (Exception ex){
            		ex.printStackTrace();
        		}
                
                if (client.newInPrivate){showParticipants.setId("news");}//	the notification signal is added to the showParticipants button if new private messages resulted from the update
                
                TilePane participantsList = new TilePane();
                participantsList.setAlignment(Pos.CENTER);
                participantsList.setHgap(20);
                participantsList.setVgap(20);
                participantsList.setPrefColumns(3);
                participantsList.prefColumnsProperty();
                
                participantsTitle.setFont(Font.font(usedFont,FontWeight.BOLD,60));
    			participantsTitle.setStyle("-fx-fill: #58bc64;");
                if(client.GetParticipants().size() == 0){//	a specific message is displayed if nobody else is online
                	participantsTitle.setText("You are alone here!");	
                } else {
                	participantsTitle.setText("Online in Green Chat..");
                
		            for(int i = 0; i<client.GetParticipants().size(); i++){
		            	Label label = client.GetParticipants().get(i);
		                if (client.newInUser.get(i)){
		                    label.setId("newMessages");//	the notification signal is added to the label of each user from which we have received something 
		                } else {
		                	label.setId("noMessages");
		            	}
		              	label.setAlignment(Pos.CENTER);
		              	
//						when a user's label is clicked, the corresponding chat will be displayed
		                label.setOnMouseClicked(new EventHandler<MouseEvent>(){
		            
		                    @Override 
		                    public void handle(MouseEvent e){
		                        try{
				                    int index = client.GetIndexOf(label.getText());
				                    client.PrivateMessageSeen(index);
				                    client.onScroll = client.GetPrivateChat().get(index);
				                    if (!client.newInPrivate){showParticipants.setId("noNews");}
				                    participantsStage.close();
				                    
				            		chatId.setText("You are chatting with");
				            		chatLabel.setText(label.getText());
				            		groupChat.setVisible(true);
				            		if (client.newInPublic && client.onScroll != client.GetPublicChat()){groupChat.setId("news");}
				                    messageScroll.setContent(client.onScroll);
				                    messageScroll.setVvalue(messageScroll.getVmax());
		                        
		                        } catch (Exception ex){
		                        	ex.printStackTrace();
		                    	}
		                    }
		                });
		    		}
        		}

                participantsList.getChildren().addAll(client.GetParticipants());
                
                VBox participantsRoot = new VBox();
                participantsRoot.getChildren().addAll(participantsTitle, participantsList);
                participantsRoot.setAlignment(Pos.CENTER);
                participantsRoot.setSpacing(50);
          
                
//              PARTICIPANTS SCENE

                Scene participantsScene = new Scene(participantsRoot);
                participantsScene.getStylesheets().add(getClass().getResource("participantsStyle.css").toExternalForm()); 
                participantsStage.setWidth(1400);
                participantsStage.setHeight(800);
                
                participantsStage.setScene(participantsScene);
                participantsStage.show();
            }            
        });


    }
}        

