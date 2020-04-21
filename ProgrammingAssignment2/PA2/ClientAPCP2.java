import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.Scanner;
import java.security.Key;

class ClientAPCP2 {
    private static String cacseFilename = "certs/cacse.crt";
    private static String signedCertFilename;
    private static PublicKey publicServerKey;
    private static SecretKey sessionKey;

    public static void main(String[] args) {

        makeFolder("recv_certs");
        makeFolder("recv_recv_send");

        // TODO: Set up client to server connection
        String serverAddress = "localhost";
        int port = 4321;

        // TODO: Generate NONCE (random integer)
        Random r = new Random();
        int nonce = r.nextInt();

        long timeStarted = System.nanoTime();
        try {

            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            Socket clientSocket = new Socket(serverAddress, port);
            DataOutputStream toServer = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream fromServer = new DataInputStream(clientSocket.getInputStream());

            int decryptedNONCEasInt = 0;
            System.out.println("Authenticating...");
            try {
                decryptedNONCEasInt = Authenticate(nonce, toServer, fromServer);
            } catch (Exception e) {
                System.out.println("Authentication failed");
            }

            // TODO: Compare NONCE with decryptedNONCE
            if (decryptedNONCEasInt != nonce) {
                // TODO: Not equal, end connection
                System.out.println("Non Authentic Server!");
                return;
            }
            System.out.println("Authentic Server!");
            doSessionKey(toServer);
            // TODO: If equal, is authentic, start file transfers
            // TODO: Infinite loop prompt for file, exit only when user prompt
            while (true) {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
                Cipher deCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                deCipher.init(Cipher.DECRYPT_MODE, sessionKey);
                Scanner in = new Scanner(System.in);
                System.out.print("> ");
                String input = in.nextLine();
                String[] inSplit = input.split(" ");
                if (inSplit[0].equals("exit")) {
                    toServer.writeInt(1);
                    return;
                } else if (inSplit[0].equals("send")) {
                    String fileToSend = null;
                    if (inSplit.length < 2) {
                        System.out.println("send <filename>");
                    } else {
                        fileToSend = "send/" + inSplit[1];
                    }
                    if(fileToSend != null){
                        try {
                            // will send int 0
                            fileIO.sendEncryptedFile(toServer, fileToSend, sessionKey);
                            System.out.println("File Sent");
                        } catch (IOException e) {
                            System.out.println("File not found!");
                        }
                    }
                } else if (inSplit[0].equals("del")) {
                    // TODO: check arg not empty
                    String fileToDel = null;
                    if (inSplit.length < 2) {
                        System.out.println("del <filename>");
                    } else {
                        fileToDel = inSplit[1];
                    }
                    if (fileToDel != null) {
                        // TODO: writeInt 2
                        toServer.writeInt(2);
                        // TODO: encrypt filename with sessionKey
                        byte[] encrypteFileToDel = cipher.doFinal(fileToDel.getBytes());
                        // TODO: Send encryptedFilename of file to del
                        toServer.writeInt(encrypteFileToDel.length);
                        toServer.write(encrypteFileToDel);
                        toServer.flush();
                        // TODO: Receive message
                        int numBytes = fromServer.readInt();
                        byte[] Buffer = new byte[numBytes];
                        fromServer.readFully(Buffer, 0, numBytes);
                        System.out.println(new String(Buffer, 0, numBytes));
                    }

                } else if (inSplit[0].equals("ls")) {
                    // TODO: writeInt 3
                    toServer.writeInt(3);
                    // TODO: receive list size
                    int loopTimes = fromServer.readInt();
                    while (loopTimes > 0) {
                        // TODO: receive filename
                        int numBytes = fromServer.readInt();
                        byte[] encryptedFilename = new byte[numBytes];
                        fromServer.readFully(encryptedFilename, 0, numBytes);
                        // TODO: decrypt encryptedFilename with sessionKey
                        byte[] decryptedFilename = deCipher.doFinal(encryptedFilename);
                        // TODO: print filename
                        System.out.println(new String(decryptedFilename, 0, decryptedFilename.length));
                        loopTimes--;
                    }
                } else if (inSplit[0].equals("dl")) {
                    // TODO: check arg not empty
                    String fileToDL = null;
                    if (inSplit.length < 2) {
                        System.out.println("dl <filename>");
                    } else {
                        fileToDL = inSplit[1];
                    }
                    if (fileToDL != null) {
                        // TODO: writeInt 4
                        toServer.writeInt(4);
                        // TODO: encrypt filename with sessionKey
                        byte[] encryptedFileToDL = cipher.doFinal(fileToDL.getBytes());
                        // TODO: Send encryptedFilename of file to download
                        toServer.writeInt(encryptedFileToDL.length);
                        toServer.write(encryptedFileToDL);
                        toServer.flush();
                        // TODO: Receive encryptedFile if any
                        int isExist = fromServer.readInt();
                        if (isExist == 0) {
                            // TODO: Decrypt encryptedFile with sessionKey
                            fileIO.receiveEncryptedFile(fromServer, sessionKey);
                            System.out.println("File downloaded");
                        } else {
                            System.out.println("File does not exist in server...");
                        }
                    }

                } else {
                    System.out.println("Invalid input...");
                }
            }

        } catch (Exception e) {
            System.out.println("connection error");
        }

        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken / 1000000.0 + "ms to run");

    }

    public static void makeFolder(String path) {

        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println(path + " directory made Successfully!");
            } else {
                System.out.println("Folder making failed successfully!");
            }
        }

    }

    private static int Authenticate(int nonce, DataOutputStream toServer, DataInputStream fromServer) throws Exception {
        // TODO: Request authentication
        toServer.writeInt(-1);

        // TODO: Send NONCE
        toServer.writeInt(nonce);
        System.out.println("nonce sent: " + nonce);

        // TODO: Receive encryptedNONCE
        int numBytes = fromServer.readInt();
        byte[] encryptedNONCE = new byte[numBytes];
        fromServer.readFully(encryptedNONCE, 0, numBytes);
        System.out.println("Received encrypted nonce");

        // TODO: Receive certificate
        System.out.print("Receiving ");
        // receive filename

        numBytes = fromServer.readInt();
        byte[] filename = new byte[numBytes];
        fromServer.readFully(filename, 0, numBytes);
        System.out.println(new String(filename, 0, numBytes));
        signedCertFilename = "recv_" + new String(filename, 0, numBytes);

        FileOutputStream fileOutputStream = new FileOutputStream(signedCertFilename);
        BufferedOutputStream bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

        byte[] Buffer = new byte[1023];
        int count;
        while ((count = fromServer.read(Buffer)) > 0) {
            bufferedFileOutputStream.write(Buffer, 0, count);
            if (count < 1023) {
                break;
            }
        }

        bufferedFileOutputStream.close();
        fileOutputStream.close();

        System.out.println("Cert received");

        // TODO: Obtain public key from CA certificate
        InputStream fis = new FileInputStream(cacseFilename);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate CAcert = (X509Certificate) cf.generateCertificate(fis);
        PublicKey keyCA = CAcert.getPublicKey();

        // TODO: Verify signedCert with the public key from CA certificate
        InputStream fis2 = new FileInputStream(signedCertFilename);
        X509Certificate signedCert = (X509Certificate) cf.generateCertificate(fis2);
        //signedCert.checkValidity();
        signedCert.verify(keyCA);

        // TODO: Get public key from signedCert
        publicServerKey = signedCert.getPublicKey();

        // TODO: Decrypt encryptedNONCE using public key
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicServerKey);
        byte[] decryptedNONCE = cipher.doFinal(encryptedNONCE);
        return ByteBuffer.wrap(decryptedNONCE).getInt();
    }

    private static void doSessionKey(DataOutputStream toServer) throws Exception {
        // TODO: Create Session Key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        sessionKey = keyGen.generateKey();
        // TODO: Encrypt publicSessionKey with publicServerKey
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicServerKey);
        // TODO: Send encryptedPublicSessionKey to Server
        byte[] encryptedPublicSessionKey = cipher.doFinal(sessionKey.getEncoded());
        toServer.writeInt(encryptedPublicSessionKey.length);
        toServer.write(encryptedPublicSessionKey);
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
