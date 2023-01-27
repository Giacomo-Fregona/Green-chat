import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

public class Server {

    private ArrayList<ServerThread> clientServerThreads;//  List of the running ServerThreads (one for each client)
    private ArrayList<byte[]> clientIds;//  each ID is the byte representation of the user name. The element of the i-th position and clientServerThreads[i] correspond to the same user
    
//  Adding a new client to the server attributes
    public synchronized void AddClient(byte[] id, ServerThread st){
        clientServerThreads.add(st);
        clientIds.add(id);
    }
    
//  Removing a client from the server attributes
    public synchronized void RemoveClient(byte[] id) throws Exception {
        int index = GetIndexOf(id);
        this.clientServerThreads.remove(index);
        this.clientIds.remove(index);
    }
    
    public ServerThread GetServerThread(byte[] id) throws Exception {
        return this.clientServerThreads.get(GetIndexOf(id));
    }
    
    public ServerThread GetServerThread(int i) throws Exception {
        return this.clientServerThreads.get(i);
    }
    
    public ArrayList<byte[]> GetClientIds(){
        return this.clientIds;
    }
    
    public byte[] GetClientId(int i){
        return this.clientIds.get(i);
    }
    
    
    public int GetIndexOf(byte[] id) throws Exception {
    
//      Finding the index of the sender in our ArrayList
        for(int i = 0; i<clientIds.size(); i++){
            if(Arrays.equals(id, clientIds.get(i))){
			    return i;
            }
        }
        
        throw new Exception("Element "+ new String(id, StandardCharsets.UTF_8) +"not in clientIds!");
    }
    
//  Launching the server
    public void launch() {
    
        try {
//          Enabling connection in gate 2023
            ServerSocket ss = new ServerSocket(2023);
            
//          Always ready for a new connection!
            while(true){
                ServerThread st = new ServerThread(this, ss.accept());//    Each time a new client connects, we Construct a new ServerThread running in a new Thread
                Thread t = new Thread(st);
                t.start();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args){
    
//      Initialising server attributes
        Server server = new Server();
        server.clientServerThreads = new ArrayList<ServerThread>();
        server.clientIds = new ArrayList<byte[]>();
        
//      Launching our server!
        server.launch();
        
    }

}
