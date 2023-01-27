import static java.lang.Math.ceil;
import static java.lang.Math.pow;
import java.security.SecureRandom;

public class AEScipher {

//  Number of already created AEScipher objects. Same idea of arc4JNI seen in class
    static int counter=0;
    
//  ID of the created object in the machine
    private int id;
    
    public AEScipher(){       
        id = CreateId();
    }
    
//  Creates a new id for a new object 
    private synchronized int CreateId(){
        counter++;
        return counter - 1 ;
    }
    
    public int GetId(){return this.id;}
    
//  Creates the variable where to store the state of the cipher. The state of the cipher is used to keep aligned encryption and decryption in client end and server end of the connection
    public native void Setup(byte[] Key);
    
    public native void Encrypt(byte[] Plaintext);//encryption
    
    public native void Decrypt(byte[] Ciphertext);//decryption

    static {
	System.loadLibrary("AESLib");
    }
    
//  Padding for our messages. It has the following structure: l+random bytes + message where l is the length of the message written in 3 bytes
    public byte[] Padding(byte[] Plaintext){
    
//      Computing the output's length in bytes
        int l = Plaintext.length;
        int numBlocks = (int) Math.ceil((l+3.0)/16.0);
        byte[] out = new byte[numBlocks*16];
        
//      Placing in the first three bytes of the output the original plaintext's length in bytes
        for(int i = 0; i<3; i++){
            out[2-i] = (byte)(l % ((int)Math.pow(2,8)));
            l /= (int) Math.pow(2,8);
        }
        l = Plaintext.length; 
        
//      Placing the random bytes
        int residuals = numBlocks*16-l;
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[residuals-3];
        random.nextBytes(bytes);
        for(int i = 3; i<residuals; i++)out[i] = bytes[i-3];
        
//      Placing the plaintext
        for(int i=0; i<l; i++){
            out[numBlocks*16-i-1] = Plaintext[l-i-1];
        }
        
        return out;
    }
    
//  Removes the padding from a received message
    public byte[] Depadding(byte[] PaddedPlaintext, int l){//l is the length of the message from which we would like to remove the padding
        byte[] out = new byte[l];
        for(int i=0; i<l; i++){
            out[l-i-1] = PaddedPlaintext[PaddedPlaintext.length-i-1];
        }
        return out;
    }
    
//  reads the lenght of the original message analizing the first 3 bits of the padded message
    public int ReadLengthFromFirstBlock(byte[] PaddedPlaintext){
        int out = 0;
        for(int i = 2; i>=0; i--){
            out += (int) PaddedPlaintext[2-i]*((int)Math.pow(2,8*i));
        }
        return out;
    }
}
