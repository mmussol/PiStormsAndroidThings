package com.mmussol.pistormsandroidthings.pistorms.display;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtils {
    private ImageUtils() { }
    
    /**
     * Generator function to convert an image to 16-bit 565 RGB bytes.
     * @return
     * @throws IOException 
     */
    public static byte[] to565RGBBytes(Bitmap image) throws IOException {

        int width = image.getWidth();
        int height = image.getHeight();
        
        ByteBuffer buffer = ByteBuffer.allocate(width * height * 2);
//        for (int x=width-1;x>= 0;x--) {
//            for (int y=0;y<height;y++) {

        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                
//                int ax = getScreenXFromImageCoords(rotation, x, y, width);
//                int ay = getScreenYFromImageCoords(rotation, x, y, height);
    
                int pixelToSend = getColor565(image.getPixel(x, y));
        
                buffer.putShort((short)pixelToSend);
            }
        }
        return buffer.array();
    }
    
    /**
     * Convert red, green, blue components to a 16-bit 565 RGB value. Components
     * should be values 0 to 255
     */
    public static int getColor565(int c) {
        int val = ((Color.red(c) & 0xF8) << 8) | ((Color.green(c) & 0xFC) << 3) | (Color.blue(c) >> 3);
        return val;
    }

}
