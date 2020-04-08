import java.lang.Object;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.BufferedImage;
import java.nio.*;
import javax.crypto.*;
import java.util.Base64;


public class DesImageSolution {
    public static void main(String[] args) throws Exception{
        dothing("SUTD.bmp", "ECB", false);
        dothing("SUTD.bmp", "CBC", false);
        dothing("triangle.bmp", "ECB", false);
        dothing("triangle.bmp", "CBC", false);

        dothing("SUTD.bmp", "ECB", true);
        dothing("SUTD.bmp", "CBC", true);
        dothing("triangle.bmp", "ECB", true);
        dothing("triangle.bmp", "CBC", true);
    }

    public static void dothing(String fileName, String mode, boolean isBtmToTop) throws Exception{
        int image_width = 200;
        int image_length = 200;
        // read image file and save pixel value into int[][] imageArray
        BufferedImage img = ImageIO.read(new File(fileName));
        image_width = img.getWidth();
        image_length = img.getHeight();
        // byte[][] imageArray = new byte[image_width][image_length];
        int[][] imageArray = new int[image_width][image_length];
        for(int idx = 0; idx < image_width; idx++) {
            for(int idy = 0; idy < image_length; idy++) {
                int color = 0;
                if(isBtmToTop){
                    color = img.getRGB(idx, image_length - 1 - idy);
                }else{
                    color = img.getRGB(idx, idy);
                }
                imageArray[idx][idy] = color;
            }
        }
        // TODO: generate secret key using DES algorithm
        SecretKey key = KeyGenerator.getInstance("DES").generateKey();

        // TODO: Create cipher object, initialize the ciphers with the given key, choose encryption algorithm/mode/padding,
        //you need to try both ECB and CBC mode, use PKCS5Padding padding method
        Cipher cipher = Cipher.getInstance("DES/" + mode + "/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);


        // define output BufferedImage, set size and format
        BufferedImage outImage = new BufferedImage(image_width,image_length, BufferedImage.TYPE_3BYTE_BGR);

        for(int idx = 0; idx < image_width; idx++) {
        // convert each column int[] into a byte[] (each_width_pixel)
            byte[] each_width_pixel = new byte[4*image_length];
            for(int idy = 0; idy < image_length; idy++) {
                ByteBuffer dbuf = ByteBuffer.allocate(4);
                dbuf.putInt(imageArray[idx][idy]);
                byte[] bytes = dbuf.array();
                System.arraycopy(bytes, 0, each_width_pixel, idy*4, 4);
            }
            // TODO: encrypt each column or row bytes
            byte[] encryptedByte = cipher.doFinal(each_width_pixel);

            // TODO: convert the encrypted byte[] back into int[] and write to outImage (use setRGB)
            byte[] encrypted_pixel = new byte[4];
            for(int idy = 0; idy < image_length; idy++){
                System.arraycopy(encryptedByte, idy*4, encrypted_pixel, 0, 4);
                ByteBuffer wrapped = ByteBuffer.wrap(encrypted_pixel);
                int newcolor = wrapped.getInt();
                outImage.setRGB(idx, idy, newcolor);
            }
        }
        //write outImage into file
        if(isBtmToTop){
            ImageIO.write(outImage, "BMP",new File("En" + mode + "btmtotop" + fileName));
        }
        else{
            ImageIO.write(outImage, "BMP",new File("En" + mode + fileName));
        }
    }
}
