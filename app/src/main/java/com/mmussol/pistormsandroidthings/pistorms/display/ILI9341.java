package com.mmussol.pistormsandroidthings.pistorms.display;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.mmussol.pistormsandroidthings.pistorms.Constants.PS_TAG;

/**
 * Use the {@link ILI9341Builder} to create an instance of this class.
 * 
 * This is a java port of 
 * https://github.com/adafruit/Adafruit_Python_ILI9341/blob/master/Adafruit_ILI9341/ILI9341.py
 * 
 * I also referenced this:
 * https://github.com/adafruit/Adafruit_ILI9341/blob/master/Adafruit_ILI9341.cpp
 * 
 * @author sdaubin
 *
 */
public class ILI9341 {
    
    public final static int ILI9341_TFTWIDTH    = 240;
    public final static int ILI9341_TFTHEIGHT   = 320;
    
    private final static byte ILI9341_NOP         = 0x00;
    private final static byte ILI9341_SWRESET     = 0x01;
    private final static byte ILI9341_RDDID       = 0x04;
    private final static byte ILI9341_RDDST       = 0x09;

    private final static byte ILI9341_SLPIN       = 0x10;
    private final static byte ILI9341_SLPOUT      = 0x11;
    private final static byte ILI9341_PTLON       = 0x12;
    private final static byte ILI9341_NORON       = 0x13;

    private final static byte ILI9341_RDMODE      = 0x0A;
    private final static byte ILI9341_RDMADCTL    = 0x0B;
    private final static byte ILI9341_RDPIXFMT    = 0x0C;
    private final static byte ILI9341_RDIMGFMT    = 0x0A;
    private final static byte ILI9341_RDSELFDIAG  = 0x0F;

    private final static byte ILI9341_INVOFF      = 0x20;
    private final static byte ILI9341_INVON       = 0x21;
    private final static byte ILI9341_GAMMASET    = 0x26;
    private final static byte ILI9341_DISPOFF     = 0x28;
    private final static byte ILI9341_DISPON      = 0x29;

    private final static byte ILI9341_CASET       = 0x2A;
    private final static byte ILI9341_PASET       = 0x2B;
    private final static byte ILI9341_RAMWR       = 0x2C;
    private final static byte ILI9341_RAMRD       = 0x2E;

    private final static byte ILI9341_PTLAR       = 0x30;
    private final static byte ILI9341_MADCTL      = 0x36;
    private final static byte ILI9341_PIXFMT      = 0x3A;

    private final static int ILI9341_FRMCTR1     = 0xB1;
    private final static int ILI9341_FRMCTR2     = 0xB2;
    private final static int ILI9341_FRMCTR3     = 0xB3;
    private final static int ILI9341_INVCTR      = 0xB4;
    private final static int ILI9341_DFUNCTR     = 0xB6;

    private final static int ILI9341_PWCTR1      = 0xC0;
    private final static int ILI9341_PWCTR2      = 0xC1;
    private final static int ILI9341_PWCTR3      = 0xC2;
    private final static int ILI9341_PWCTR4      = 0xC3;
    private final static int ILI9341_PWCTR5      = 0xC4;
    private final static int ILI9341_VMCTR1      = 0xC5;
    private final static int ILI9341_VMCTR2      = 0xC7;

    private final static int ILI9341_RDID1       = 0xDA;
    private final static int ILI9341_RDID2       = 0xDB;
    private final static int ILI9341_RDID3       = 0xDC;
    private final static int ILI9341_RDID4       = 0xDD;

    private final static int ILI9341_GMCTRP1     = 0xE0;
    private final static int ILI9341_GMCTRN1     = 0xE1;

    private final static int ILI9341_PWCTR6      = 0xFC;

    private final static int ILI9341_BLACK       = 0x0000;
    private final static int ILI9341_BLUE        = 0x001F;
    private final static int ILI9341_RED         = 0xF800;
    private final static int ILI9341_GREEN       = 0x07E0;
    private final static int ILI9341_CYAN        = 0x07FF;
    private final static int ILI9341_MAGENTA     = 0xF81F;
    private final static int ILI9341_YELLOW      = 0xFFE0; 
    private final static int ILI9341_WHITE       = 0xFFFF;
    
    private final static int MADCTL_MY   = 0x80;
    private final static int MADCTL_MX   = 0x40;
    private final static int MADCTL_MV   = 0x20;
    private final static int MADCTL_ML   = 0x10;
    private final static int MADCTL_RGB  = 0x00;
    private final static int MADCTL_BGR  = 0x08;
    private final static int MADCTL_MH   = 0x04;

    private final static boolean STATE_DATA = true;
    private final static boolean STATE_COMMAND = false;

    private final Gpio dcPin;
    private final Gpio resetPin;
    private Gpio csPin;
    private final Bitmap buffer;
    private final SpiDevice spiDevice;
    private final Point dimension;

    ILI9341(Gpio dcPin, Gpio resetPin, SpiDevice spiDevice, Bitmap image, ImageRotation imageRotation) throws IOException {
        
        this.dcPin = dcPin;
        this.resetPin = resetPin;
        this.spiDevice = spiDevice;
        buffer = image;
        this.dimension = new Point();
        this.dimension.x = ILI9341.ILI9341_TFTWIDTH; 
        this.dimension.y = ILI9341.ILI9341_TFTHEIGHT;

        begin();
        
        setRotation(imageRotation);
    }

    private void setRotation(ImageRotation imageRotation) throws IOException {
        
        final int flags = MADCTL_BGR;
        command(ILI9341_MADCTL);
        switch (imageRotation) {
        case NONE:
            data(MADCTL_MX | flags);
            return;
        case RIGHT_90:
            data(MADCTL_MV | flags);
            return;
        case RIGHT_180:
            data(MADCTL_MY | flags);
            return;
        case LEFT_90:
            data(MADCTL_MX | MADCTL_MY | MADCTL_MV | flags);
            return;
        }
    }

    private void begin() throws IOException {
        reset();
        
        command(0xEF);
        data(0x03);
        data(0x80);
        data(0x02);
        command(0xCF);
        data(0x00);
        data(0XC1);
        data(0X30);
        command(0xED);
        data(0x64);
        data(0x03);
        data(0X12);
        data(0X81);
        command(0xE8);
        data(0x85);
        data(0x00);
        data(0x78);
        command(0xCB);
        data(0x39);
        data(0x2C);
        data(0x00);
        data(0x34);
        data(0x02);
        command(0xF7);
        data(0x20);
        command(0xEA);
        data(0x00);
        data(0x00);
        command(ILI9341_PWCTR1);    // Power control 
        data(0x23);                 // VRH[5:0] 
        command(ILI9341_PWCTR2);    // Power control 
        data(0x10);                 // SAP[2:0];BT[3:0] 
        command(ILI9341_VMCTR1);    // VCM control 
        data(0x3e);
        data(0x28);
        command(ILI9341_VMCTR2);    // VCM control2 
        data(0x86);                 // --
        command(ILI9341_MADCTL);    //  Memory Access Control 
        data(0x48);
        command(ILI9341_PIXFMT);
        data(0x55);
        command(ILI9341_FRMCTR1);
        data(0x00);
        data(0x18);
        command(ILI9341_DFUNCTR);   //  Display Function Control 
        data(0x08);
        data(0x82);
        data(0x27);
        command(0xF2);              //  3Gamma Function Disable 
        data(0x00);
        command(ILI9341_GAMMASET);  // Gamma curve selected 
        data(0x01);
        command(ILI9341_GMCTRP1);   // Set Gamma
        data(0x0F);
        data(0x31);
        data(0x2B);
        data(0x0C);
        data(0x0E);
        data(0x08);
        data(0x4E);
        data(0xF1);
        data(0x37);
        data(0x07);
        data(0x10);
        data(0x03);
        data(0x0E);
        data(0x09);
        data(0x00);
        command(ILI9341_GMCTRN1);   // Set Gamma 
        data(0x00);
        data(0x0E);
        data(0x14);
        data(0x03);
        data(0x11);
        data(0x07);
        data(0x31);
        data(0xC1);
        data(0x48);
        data(0x08);
        data(0x0F);
        data(0x0C);
        data(0x31);
        data(0x36);
        data(0x0F);
        
        command(ILI9341_SLPOUT);    // Exit Sleep
        sleep(120);
        command(ILI9341_DISPON);    // Display on
    }

    private void data(int data) throws IOException {
        sendByte(STATE_DATA, data);
    }
    
    /**
     * Write a byte or array of bytes to the display as command data.
     * @param i
     * @throws IOException 
     */
    private void command(int data) throws IOException {
        sendByte(STATE_COMMAND, data);
    }
    
    void sendByte(boolean state, int data) throws IOException {        
        sendBytes(state, (byte)data);
    }
    
    void sendShort(boolean state, int data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort((short) data);
        sendBytes(state, buffer.array());
    }

    /**
     * Write a byte or array of bytes to the display. 
     */
    void sendBytes(boolean state, byte... data) throws IOException {
        
        // Set DC low for command, high for data.
        if (state != dcPin.getValue()) {
            Log.i(PS_TAG, "sendBytes: changing state to: " + (state ? "DATA" : "COMMAND"));
            dcPin.setValue(state);
        }

        int maxLength = 4096;
        // SpiDevice.MAX_SUPPORTED_BYTES
        if (data.length < maxLength) {
            Log.i(PS_TAG, "Writing length " + data.length);
            spiDevice.write(data, data.length);
        } else {
            int length;
            // Write data a chunk at a time.
            for (int start = 0; start < data.length; start+= maxLength) {
                length = Math.min(maxLength, data.length - start);
                //Log.i(PS_TAG, "Writing length " + length);
                spiDevice.write(data, length); //TODO offset
            }
        }
    }

    public void reset() {
        try {
            if (resetPin != null) {
                resetPin.setValue(true);
                sleep(5);
                resetPin.setValue(false);
                sleep(20);
                resetPin.setValue(true);
                sleep(150);
            }
        } catch (IOException e) {

        }
    }
    
    public void invertDisplay(boolean inverted) throws IOException {
        command(inverted ? ILI9341_INVON : ILI9341_INVOFF);
    }

    private void sleep(int milliseconds) {
        try {
            Log.i(PS_TAG, "Sleeping milliseconds = " + milliseconds);
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Write the provided image to the hardware.  If no
     * image parameter is provided the display buffer will be written to the
     * hardware.  If an image is provided, it should be RGB format and the
     * same dimensions as the display hardware.
     * @param image
     */
    private void display(Bitmap image) throws IOException {
        // Set address bounds to entire display.
        Log.i(PS_TAG, "display: setting window");
        setWindow(0, 0, -1, -1);

        Log.i(PS_TAG, "display: converting bytes");
        byte[] data = ImageUtils.to565RGBBytes(image);

        Log.i(PS_TAG, "display: sending bytes");
        sendBytes(STATE_DATA, data);
    }
    
    /**
     * Write the display buffer to the hardware.
     * @throws IOException
     */
    public void display() throws IOException {
        display(buffer);
    }
    
    public Bitmap getBufferedImage() {
        return buffer;
    }
    
    public void clear(int color) {
        buffer.eraseColor(color);
    }

    /**
     * Set the pixel address window for proceeding drawing commands. x0 and
     *  x1 should define the minimum and maximum x pixel bounds.  y0 and y1 
     *  should define the minimum and maximum y pixel bound.  If no parameters 
     *  are specified the default will be to update the entire display from 0,0
     *  to 239,319.
     * @throws IOException 
     */
    private void setWindow(int x0, int y0, int x1, int y1) throws IOException {
        if (x1 < 0) {
            x1 = (int)dimension.x - 1;
        }
        if (y1 < 0) {
            y1 = (int)dimension.y - 1;
        }
        command(ILI9341_CASET);     // Column addr set
        sendShort(STATE_DATA, x0);
        sendShort(STATE_DATA, x1);
        command(ILI9341_PASET);     // Row addr set
        sendShort(STATE_DATA, y0);
        sendShort(STATE_DATA, y1);
        command(ILI9341_RAMWR);     // write to RAM
    }
    

    public Point getDimension() {
        return new Point(buffer.getWidth(), buffer.getHeight());
    }
}
