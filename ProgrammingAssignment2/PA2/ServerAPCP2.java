import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

class ServerAPCP2 {

    private static String certFilename = "certs/example-ffc56550-8207-11ea-ae9d-89114163ae84.crt";
    private static String certPrivateKeyPath = "certs/private_key.der";
    private static PrivateKey privateServerKey;
    private static SecretKey sessionKey;

    public static void main(String[] args) {
        int port = 4321;

        makeFolder("recv_send");

        if (args.length > 0) {
            certFilename = "certs/" + args[0];
            certPrivateKeyPath = "certs/" + args[1];
        }

        try {
            // TODO: Get private key, get public key. get certificate
            privateServerKey = PrivateKeyReader.get(certPrivateKeyPath);

            // TODO: Set up server
            ServerSocket welcomeSocket = new ServerSocket(port);
            Socket connectionSocket = welcomeSocket.accept();
            DataInputStream fromClient = new DataInputStream(connectionSocket.getInputStream());
            DataOutputStream toClient = new DataOutputStream(connectionSocket.getOutputStream());

            while (!connectionSocket.isClosed()) {
                int packetType = fromClient.readInt();

                if (packetType == -1) {
                    System.out.println("Authenticating...");
                    try {
                        Authenticate(toClient, fromClient);
                        System.out.println("Authentication success");
                    } catch (Exception e) {
                        System.out.println("Authentication failed");
                        return;
                    }
                } else if (packetType == 0) {
                    System.out.print("Receiving file ");
                    try {
                        long timeStarted = System.nanoTime();
                        fileIO.receiveEncryptedFile(fromClient, sessionKey);
                        long timeTaken = System.nanoTime() - timeStarted;
                        System.out.println("Upload took: " + timeTaken / 1000000.0 + "ms");
                        System.out.println("File received");
                    } catch (IOException e) {
                        System.out.println("Failed to receive file");
                    }
                } else if (packetType == 1) {
                    System.out.println("Closing connection");
                    fromClient.close();
                    toClient.close();
                    connectionSocket.close();
                    System.out.println("Connection closed");
                } else if (packetType == 2) {   //del
                    // TODO: decrypt encryptedFilename with sessionKey
                    int numBytes = fromClient.readInt();
                    byte[] encryptedFilename = new byte[numBytes];
                    fromClient.readFully(encryptedFilename, 0, numBytes);
                    Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    c.init(Cipher.DECRYPT_MODE, sessionKey);
                    byte[] decryptedFilename = c.doFinal(encryptedFilename);
                    // TODO: delete file if any
                    File file = new File("recv_send/" + new String(decryptedFilename, 0, decryptedFilename.length));
                    if (file.delete()) {
                        System.out.println("File is deleted successfully");
                        // TODO: send message
                        String successMessage = "File deleted successfully";
                        toClient.writeInt(successMessage.getBytes().length);
                        toClient.write(successMessage.getBytes());
                        toClient.flush();
                    } else {
                        String successMessage = "Delete failed successfully";
                        toClient.writeInt(successMessage.getBytes().length);
                        toClient.write(successMessage.getBytes());
                        toClient.flush();
                    }

                } else if (packetType == 3) {   //ls
                    // TODO: getlist
                    File directory = new File("recv_send");
                    String[] f = directory.list();
                    toClient.writeInt(f.length);
                    Cipher ls = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    ls.init(Cipher.ENCRYPT_MODE, sessionKey);
                    for (String s : f) {
                        byte[] encryptedFileName = ls.doFinal(s.getBytes());
                        toClient.writeInt(encryptedFileName.length);
                        toClient.write(encryptedFileName, 0, encryptedFileName.length);
                        toClient.flush();
                    }
                    // TODO: encrypt list
                    // TODO: send encryptedList and length of the list

                } else if (packetType == 4) {   //dl
                    // TODO: decrypt encryptedFilename with sessionKey
                    int numBytes1 = fromClient.readInt();
                    byte[] encryptedFilename = new byte[numBytes1];
                    fromClient.readFully(encryptedFilename, 0, numBytes1);
                    Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    c.init(Cipher.DECRYPT_MODE, sessionKey);
                    byte[] decryptedFilename = c.doFinal(encryptedFilename);
                    // TODO: get file from decryptedFilename
                    String fileToSend = "recv_send/" + new String(decryptedFilename, 0, decryptedFilename.length);
                    // TODO: send file using function
                    System.out.println("Sending file...");
                    try {
                        fileIO.sendEncryptedFile(toClient, fileToSend, sessionKey);
                    } catch (Exception e) {
                        toClient.writeInt(-1);
                    }


                }
            }
        } catch (Exception e) {
            System.out.println("Connection error");
        }

    }

    private static void Authenticate(DataOutputStream toClient, DataInputStream fromClient) throws Exception {
        // TODO: Receive NONCE
        int nonce = fromClient.readInt();
        byte[] bytesNONCE = ByteBuffer.allocate(4).putInt(nonce).array();
        System.out.println("Nonce received: " + nonce);

        // TODO: Encrypt received NONCE with private key
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateServerKey);
        byte[] encryptedNONCE = cipher.doFinal(bytesNONCE);
        System.out.println("nonce encrypted");

        // TODO: Send Encrypted NONCE
        toClient.writeInt(encryptedNONCE.length);
        toClient.write(encryptedNONCE);
        toClient.flush();
        System.out.println("Encrypted NONCE sent");

        // TODO: Send certificate (certificate contains public key)
        System.out.println("Send " + certFilename);
        try {
            fileIO.sendFile(toClient, certFilename, true);
            System.out.println("Cert sent");
        } catch (IOException e) {
            System.out.println("Cert not found!");
        }

        doSessionKey(fromClient);
    }

    private static void doSessionKey(DataInputStream fromClient) throws Exception {
        // TODO: Get encryptedPublicSessionKey
        int numBytes = fromClient.readInt();
        byte[] encryptedPublicSessionKey = new byte[numBytes];
        fromClient.readFully(encryptedPublicSessionKey, 0, numBytes);
        // TODO: Decrypt encryptedPublicSessionKey with privateServerKey
        Cipher deCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        deCipher.init(Cipher.DECRYPT_MODE, privateServerKey);
        byte[] decryptedPublicSessionKey = deCipher.doFinal(encryptedPublicSessionKey);
        sessionKey = new SecretKeySpec(decryptedPublicSessionKey, 0, decryptedPublicSessionKey.length, "AES");
    }

    public static void makeFolder(String path) {

        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println(path + "directory made Successfully!");
            } else {
                System.out.println("Folder making failed successfully!");
            }
        }

    }

}

// extract key from .der file
class PrivateKeyReader {
    public static PrivateKey get(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}

class PublicKeyReader {
    public static PublicKey get(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}

class fileIO {

    public static void sendFile(DataOutputStream toServer, String fileToSend, boolean isCert) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileToSend);
        BufferedInputStream bufferedFileInputStream = new BufferedInputStream(fileInputStream);

        if (!isCert)
            toServer.writeInt(0);
        toServer.writeInt(fileToSend.getBytes().length);
        toServer.write(fileToSend.getBytes());
        toServer.flush();

        int count;
        byte[] Buffer = new byte[1023];
        System.out.println("Sending...");
        while ((count = bufferedFileInputStream.read(Buffer)) > 0) {
            toServer.write(Buffer, 0, count);
            toServer.flush();
        }

        bufferedFileInputStream.close();
        fileInputStream.close();
    }

    public static void receiveEncryptedFile(DataInputStream fromClient, SecretKey sessionKey) throws Exception {
        Cipher deCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        deCipher.init(Cipher.DECRYPT_MODE, sessionKey);

        int numBytes = fromClient.readInt();
        byte[] filename = new byte[numBytes];
        fromClient.readFully(filename, 0, numBytes);
        byte[] decryptedFilename = deCipher.doFinal(filename);
        System.out.println(new String(decryptedFilename, 0, decryptedFilename.length));

        FileOutputStream fileOutputStream = new FileOutputStream("recv_" + new String(decryptedFilename, 0, decryptedFilename.length));
        BufferedOutputStream bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

        byte[] Buffer = new byte[1024];
        while (fromClient.read(Buffer) > 0) {
            byte[] decryptedBuffer = deCipher.doFinal(Buffer);
            int count = fromClient.readInt();
            bufferedFileOutputStream.write(decryptedBuffer, 0, count);
            if (count < 1023)
                break;
        }

        bufferedFileOutputStream.close();
        fileOutputStream.close();
    }

    public static void sendEncryptedFile(DataOutputStream toServer, String fileToSend, SecretKey sessionKey) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(fileToSend);
        BufferedInputStream bufferedFileInputStream = new BufferedInputStream(fileInputStream);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey);

        toServer.writeInt(0);
        // TODO: Encrypt filename
        byte[] encryptedFilename = cipher.doFinal(fileToSend.getBytes());
        // TODO: Send Encrypted filename
        toServer.writeInt(encryptedFilename.length);
        toServer.write(encryptedFilename);
        toServer.flush();

        byte[] Buffer = new byte[1023];
        int count;
        System.out.println("Sending...");
        while ((count = bufferedFileInputStream.read(Buffer)) > 0) {
            byte[] encrytedBuffer = cipher.doFinal(Buffer);
            toServer.write(encrytedBuffer, 0, encrytedBuffer.length);
            toServer.writeInt(count);
            toServer.flush();
        }

        bufferedFileInputStream.close();
        fileInputStream.close();

    }


}
