import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.crypto.*;
import java.util.Base64;


public class DesSolution {
    public static void main(String[] args) throws Exception {
        String fileName = "shorttext.txt";
        String data = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader( new FileReader(fileName));
        while((line= bufferedReader.readLine())!=null){
            data = data +"\n" + line;
        }
        System.out.println("Original content: "+ data);

        //TODO: generate secret key using DES algorithm
        SecretKey key = KeyGenerator.getInstance("DES").generateKey();

        //TODO: create cipher object, initialize the ciphers with the given key, choose encryption mode as DES
        Cipher enCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        enCipher.init(Cipher.ENCRYPT_MODE, key);

        //TODO: do encryption, by calling method Cipher.doFinal().
        byte[] output = enCipher.doFinal(data.getBytes());

        //TODO: print the length of output encrypted byte[], compare the length of file shorttext.txt and longtext.txt
        // Shorttext = 1480
        // Longtest = 17360
        System.out.println(output.length);
        System.out.println(new String(output));

        //TODO: do format conversion. Turn the encrypted byte[] format into base64format String using Base64
        String base64format = Base64.getEncoder().encodeToString(output);

        //TODO: print the encrypted message (in base64format String format)
        System.out.println(base64format);

        //TODO: create cipher object, initialize the ciphers with the given key, choose decryption mode as DES
        Cipher deCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        deCipher.init(Cipher.DECRYPT_MODE, key);

        //TODO: do decryption, by calling method Cipher.doFinal().
        byte[] output2 = deCipher.doFinal(output);

        //TODO: do format conversion. Convert the decrypted byte[] to String, using "String a = new String(byte_array);"
        String a = new String(output2);

        //TODO: print the decrypted String text and compare it with original text
        System.out.println(a);
        if(a.equals(data)){
            System.out.println("same");
        }else{
            System.out.println("different");
        }

    }
}
