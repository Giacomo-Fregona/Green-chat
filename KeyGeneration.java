import javax.crypto.Cipher;
import org.bouncycastle.pqc.crypto.crystals.kyber.*;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import java.security.SecureRandom;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import java.nio.charset.StandardCharsets;

import java.io.FileOutputStream;
import java.io.FileInputStream;

public class KeyGeneration{

//  Write len of the parameter in 3 bytes format
    public static byte[] wLen(byte[] param){
        int l = param.length;
        byte[] out = new byte[3];        
        for(int i = 0; i<3; i++){
            out[2-i] = (byte)(l % ((int)Math.pow(2,8)));
            l /= (int) Math.pow(2,8);
            }
        return out;
    }
    
//  Read len of the parameter in 3 bytes format
    public static int rLen(byte[] out){
        int l= 0;
        for(int i = 2; i>=0; i--){
            l += (int) out[2-i]*((int)Math.pow(2,8*i));
            }
        return l;
    }

//  Generates the new PrivateKey and PublicKey files so that they can be used from Server and Client
    public static void main(String[] args) throws Exception {
           
        KyberKeyGenerationParameters kkgp = new KyberKeyGenerationParameters(new SecureRandom(), KyberParameters.kyber512);
        KyberKeyPairGenerator kkpg = new KyberKeyPairGenerator();
        kkpg.init(kkgp);
        AsymmetricCipherKeyPair keyPair = kkpg.generateKeyPair();
        
        
//      Saving private key in a file
        KyberPrivateKeyParameters privateKey =  (KyberPrivateKeyParameters) keyPair.getPrivate();
        
//      Extracting the various key parameters
        byte[] private_s =  privateKey.getS();
        byte[] private_hpk =  privateKey.getHPK();
        byte[] private_nonce =  privateKey.getNonce();
        byte[] private_t =  privateKey.getT();
        byte[] private_rho =  privateKey.getRho();
        
//      Writing the parameters to file in the format :
//      private_s.length in 3 bytes + private_s + private_hpk.length in 3 bytes + private_hpk + ... + private_rho.length in 3 bytes + private_rho
        FileOutputStream privateKeyWriter = new FileOutputStream("PrivateKey.key");
        privateKeyWriter.write(wLen(private_s));
        privateKeyWriter.write(private_s);
        privateKeyWriter.write(wLen(private_hpk));
        privateKeyWriter.write(private_hpk);
        privateKeyWriter.write(wLen(private_nonce));
        privateKeyWriter.write(private_nonce);
        privateKeyWriter.write(wLen(private_t));
        privateKeyWriter.write(private_t);
        privateKeyWriter.write(wLen(private_rho));
        privateKeyWriter.write(private_rho);
        privateKeyWriter.close();
                
//      Saving public key in a file
        KyberPublicKeyParameters publicKey =  (KyberPublicKeyParameters) keyPair.getPublic();
        
//      Extracting the various key parameters
        byte[] public_t =  publicKey.getT();
        byte[] public_rho =  publicKey.getRho();

//      Writing the parameters to file
        FileOutputStream publicKeyWriter = new FileOutputStream("PublicKey.key");
        publicKeyWriter.write(wLen(public_t));
        publicKeyWriter.write(public_t);
        publicKeyWriter.write(wLen(public_rho));
        publicKeyWriter.write(public_rho);
        publicKeyWriter.close();
        
        
        
        
        
//      TESTING THE NEW METHODS
//        
//        byte[] kb1 = new byte[16];
//        byte[][] kb1_enc = AESencapsulation();
//        kb1 = kb1_enc[0];
//        byte[] enc = kb1_enc[1];
//        
//        
//        byte[] kb2 = new byte[16];
//        kb2 = AESdecapsulation(enc);
//        
//        System.out.println(new String(kb1, StandardCharsets.UTF_8)+"   confrontata con     "+new String(kb2, StandardCharsets.UTF_8));
        
        
        
    }
    
    
//  Function designed to be used by the server. Returns the extracted AES key from the encapsulation
    public static byte[] AESdecapsulation(byte[] encapsulation)throws Exception{
    
        //      Reading the key parameters from file
        FileInputStream privateKeyReader = new FileInputStream("PrivateKey.key");
    
        byte[] private_s;
        byte[] out = new byte[3];
        privateKeyReader.read(out);
        int l = rLen(out);
        private_s = new byte[l];
        privateKeyReader.read(private_s);
        
        byte[] private_hpk;
        privateKeyReader.read(out);
        l = rLen(out);
        private_hpk = new byte[l];
        privateKeyReader.read(private_hpk);

        byte[] private_nonce;
        privateKeyReader.read(out);
        l = rLen(out);
        private_nonce = new byte[l];
        privateKeyReader.read(private_nonce);

        byte[] private_t;
        privateKeyReader.read(out);
        l = rLen(out);
        private_t = new byte[l];
        privateKeyReader.read(private_t);

        byte[] private_rho;
        privateKeyReader.read(out);
        l = rLen(out);
        private_rho = new byte[l];
        privateKeyReader.read(private_rho);
        
        privateKeyReader.close();
        
//      Reconstructing the private key from the parameters
        KyberPrivateKeyParameters reconstructedPrivateKey = new KyberPrivateKeyParameters(KyberParameters.kyber512, private_s, private_hpk, private_nonce, private_t, private_rho);
        
//      Extracting the AES key from the encapsulation
        KyberKEMExtractor extractor = new KyberKEMExtractor(reconstructedPrivateKey);

//      Returning the extracted key
        return extractor.extractSecret(encapsulation);
        
    }
    
    
    
//  Function designed to be used by the client. Generates and returns a new AES key. It also returns the encapsulation of the AES key to be sent to the Server 
    public static byte[][] AESencapsulation()throws Exception{
    
//      Reading the key parameters from file
        FileInputStream publicKeyReader = new FileInputStream("PublicKey.key");
        
        byte[] public_t;
        byte[] out = new byte[3];
        publicKeyReader.read(out);
        int l = rLen(out);
        public_t = new byte[l];
        publicKeyReader.read(public_t);
        
        byte[] public_rho;
        publicKeyReader.read(out);
        l = rLen(out);
        public_rho = new byte[l];
        publicKeyReader.read(public_rho);
        
        publicKeyReader.close();
        
//      Reconstructing the public key from the parameters
        KyberPublicKeyParameters reconstructedPublicKey = new KyberPublicKeyParameters(KyberParameters.kyber512, public_t, public_rho);
        
//      Generating a new AES key and its encapsulation
        KyberKEMGenerator encapsulator = new KyberKEMGenerator(new SecureRandom());
        SecretWithEncapsulation s_e = encapsulator.generateEncapsulated(reconstructedPublicKey);
        
//      Returning the encapsulation key 
        byte[][] toReturn = {s_e.getSecret(), s_e.getEncapsulation()};
        return toReturn;

    }

}
