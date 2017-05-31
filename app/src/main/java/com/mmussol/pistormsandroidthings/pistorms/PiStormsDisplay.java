package com.mmussol.pistormsandroidthings.pistorms;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.mmussol.pistormsandroidthings.pistorms.Constants.PS_TAG;

/**
 * This is an AndroidThings port of the following (MANY THANKS TO author sdaubin)
 * https://github.com/saxond/ILI9341Java
 */
public class PiStormsDisplay {

    public final static int ILI9341_TFTWIDTH    = 320;
    public final static int ILI9341_TFTHEIGHT   = 240;

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

    private final int SPI_MAX_SUPPORTED_BYTES = 4096;

    private final I2cDevice mI2cDevice; // i2c device for reading touch screen inputs
    private final SpiDevice mSpiDevice;
    private final Gpio mDcPin; // data/control GPIO pin
    private final Gpio mResetPin; // reset GPIO pin

    private final Bitmap mFrameBitmap;

    // Allocate byte buffer for pixels (2 bytes per pixel)
    // Used to copy pixes from mFrameBitmap into byte array to send to SPI interface
    private ByteBuffer mFrameBuffer = ByteBuffer.allocateDirect((ILI9341_TFTWIDTH * ILI9341_TFTHEIGHT * 2));
    private byte[] mFrameBounceBuffer = new byte[SPI_MAX_SUPPORTED_BYTES];

    // Private constructor for singleton
    protected PiStormsDisplay(PeripheralManagerService manager, I2cDevice i2cDevice) throws IOException {
        mI2cDevice = i2cDevice;

        List<String> deviceList = manager.getSpiBusList();
        if (deviceList.isEmpty()) {
            Log.i(PS_TAG, "No SPI bus available on this device.");
        } else {
            Log.i(PS_TAG, "List of available devices: " + deviceList);
        }
        mSpiDevice = manager.openSpiDevice("SPI0.0");
        mSpiDevice.setMode(SpiDevice.MODE0);
        mSpiDevice.setFrequency(64000000);     // 16MHz
        mSpiDevice.setBitsPerWord(8);          // 8 BPW

        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(PS_TAG, "No GPIO port available on this device.");
        } else {
            Log.i(PS_TAG, "List of available ports: " + portList);
        }
        // Raspberry Pi 3 GPIO24 (Pin #18)
        String dataCommandGpioName = "BCM24";
        mDcPin = manager.openGpio(dataCommandGpioName);
        mDcPin.setActiveType(Gpio.ACTIVE_HIGH);
        mDcPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        String resetGpioName = "BCM25";
        mResetPin = manager.openGpio(resetGpioName);
        mResetPin.setActiveType(Gpio.ACTIVE_HIGH);
        mResetPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        mFrameBitmap = Bitmap.createBitmap(ILI9341_TFTWIDTH, ILI9341_TFTHEIGHT, Bitmap.Config.RGB_565);
        resetDisplay();
        clear(Color.GREEN);
        display();
    }

    protected void clear(int color) {
        mFrameBitmap.eraseColor(color);
    }

    private void display() throws IOException {

        // Set address bounds to entire display.
        command(ILI9341_CASET);     // Column addr set
        sendShort(STATE_DATA, 0);   // x start
        sendShort(STATE_DATA, (ILI9341_TFTWIDTH - 1)); // x end
        command(ILI9341_PASET);     // Row addr set
        sendShort(STATE_DATA, 0);   // y start
        sendShort(STATE_DATA, (ILI9341_TFTHEIGHT - 1)); // y end
        command(ILI9341_RAMWR);     // write to RAM

        // Set DC for data.
        mDcPin.setValue(STATE_DATA);

        // Copy bitmap pixels to ByteBuffer
        mFrameBitmap.copyPixelsToBuffer(mFrameBuffer);

        // Write data out to spi interface (use 4k bounce buffer)
        mFrameBuffer.position(0);
        while (mFrameBuffer.remaining() > 0) {
            int len = Math.min(SPI_MAX_SUPPORTED_BYTES, mFrameBuffer.remaining());
            mFrameBuffer.get(mFrameBounceBuffer, 0, len);
            mSpiDevice.write(mFrameBounceBuffer, mFrameBounceBuffer.length);
        }
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Log.e(PS_TAG, "Sleep interrupted");
        }
    }

    private void resetDisplay() throws IOException {
        // Reset display
        mResetPin.setValue(true);
        sleep(5);
        mResetPin.setValue(false);
        sleep(20);
        mResetPin.setValue(true);
        sleep(150);

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

        command(ILI9341_MADCTL);    // Set Rotation to NONE
        data(MADCTL_MX | MADCTL_BGR);
    }

    private void data(int data) throws IOException {
        sendByte(STATE_DATA, data);
    }

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
        if (state != mDcPin.getValue()) {
            mDcPin.setValue(state);
        }

        if (data.length < SPI_MAX_SUPPORTED_BYTES) {
            mSpiDevice.write(data, data.length);
        } else {
            throw new IOException("Write request too large > " + SPI_MAX_SUPPORTED_BYTES);
        }
    }
}
