package com.mmussol.pistormsandroidthings.pistorms.display;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.List;

import static com.mmussol.pistormsandroidthings.pistorms.Constants.PS_TAG;

public class ILI9341Builder {
           
    // Raspberry Pi 3 GPIO24 (Pin #18)
    private String dc = "BCM24";
    private String rst = "BCM25";
    private ImageRotation imageRotation = ImageRotation.NONE;

    private Bitmap image;
    
    private ILI9341Builder() {}
    
    /**
     * Creates a new {@link ILI9341} instance.
     * @throws IOException 
     */
    public ILI9341 build(SpiDevice spiDevice) throws IOException {
        if (null == image) {
            Point dimension = imageRotation.getDimension(ILI9341.ILI9341_TFTWIDTH, ILI9341.ILI9341_TFTHEIGHT);
            image = Bitmap.createBitmap((int)dimension.x, (int)dimension.y, Bitmap.Config.RGB_565);
        }
        
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(PS_TAG, "No GPIO port available on this device.");
        } else {
            Log.i(PS_TAG, "List of available ports: " + portList);
        }
        Gpio dcPin = manager.openGpio(dc);
        // High voltage is considered active
        dcPin.setActiveType(Gpio.ACTIVE_HIGH);
        // Initialize the pin as a high output
        dcPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);


        Gpio resetPin = null;

        if (rst != null) {
            resetPin = manager.openGpio(rst);
            // High voltage is considered active
            resetPin.setActiveType(Gpio.ACTIVE_HIGH);
            // Initialize the pin as a high output
            resetPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        }


        ILI9341 display = new ILI9341(dcPin, resetPin, spiDevice, image, imageRotation);
        return display;
    }

    public static ILI9341Builder newBuilder() {
        return new ILI9341Builder();
    }

    /**
     * Set the DC pin.  Remember that p4j uses the wiring pin numbers, not the BCI or 
     * physical pins.
     * @param dc
     * @return
     */
    public ILI9341Builder setDcPin(String dc) {
        this.dc = dc;
        return this;
    }

    public ILI9341Builder setResetPin(String rst) {
        this.rst = rst;
        return this;
    }

    public ILI9341Builder setImageRotation(ImageRotation imageRotation) {
        this.imageRotation = imageRotation;
        return this;
    }
/*
    public ILI9341Builder setGpioController(GpioController gpioController) {
        this.gpioController = gpioController;
        return this;
    }
  */
}
