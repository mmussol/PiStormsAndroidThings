package com.mmussol.pistormsandroidthings.pistorms.display;

import android.graphics.Color;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.List;

import static com.mmussol.pistormsandroidthings.pistorms.Constants.PS_TAG;

public class PiStormsDisplayBuilder {
    private ImageRotation rotation = ImageRotation.LEFT_90;

    private PiStormsDisplayBuilder() {}
    
    public static PiStormsDisplayBuilder newBuilder() {
        return new PiStormsDisplayBuilder();
    }
    
    public PiStormsDisplayBuilder setImageRotation(ImageRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public PiStormsDisplay build(I2cDevice i2cDevice) throws IOException {

        // Create LCD display class.
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> deviceList = manager.getSpiBusList();
        if (deviceList.isEmpty()) {
            Log.i(PS_TAG, "No SPI bus available on this device.");
        } else {
            Log.i(PS_TAG, "List of available devices: " + deviceList);
        }

        //SpiDevice spiDevice = SpiFactory.getInstance(
        //        SpiChannel.CS0, 64000000, SpiMode.MODE_0);
        SpiDevice spiDevice = manager.openSpiDevice("SPI0.0");
        // Low clock, leading edge transfer
        spiDevice.setMode(SpiDevice.MODE0);
        spiDevice.setFrequency(64000000);     // 16MHz
        spiDevice.setBitsPerWord(8);          // 8 BPW

        ILI9341 display = ILI9341Builder.newBuilder().
                // Raspberry Pi 3 GPIO24 (Pin #18)
                setDcPin("BCM24").
                // Raspberry Pi 3 GPIO25 (Pin #22)
                setResetPin("BCM25").
                setImageRotation(rotation).
                build(spiDevice);
        
        /*  MIKEM
        NotifyingTouchScreen touchScreen = new NotifyingTouchScreen(
            TouchScreenBuilder.newBuilder().setImageRotation(rotation).createTouchScreen(i2cDevice),
            eventBus, 100, TimeUnit.MILLISECONDS);
        */
        
        display.clear(Color.CYAN);
        display.display();
        
        /*  MIKEM
        Bitmap bufferedImage = display.getBufferedImage();
        final Graphics graphics = bufferedImage.createGraphics();

        JPanel panel = new JPanel();
        panel.setSize(display.getDimension());
        new ComponentDisplayAdapter(panel, graphics, eventBus);
        
        final PiStormsDisplayImpl displayImpl = new PiStormsDisplayImpl(touchScreen, panel, display, eventBus,
                new DisplayRepaintManager(display, eventBus, 100, TimeUnit.MILLISECONDS));
        
        Runtime.getRuntime().addShutdownHook(new Thread(
            new Runnable() {
                
                @Override
                public void run() {
                    try {
                        displayImpl.close();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }));
        
        */
        final PiStormsDisplayImpl displayImpl = new PiStormsDisplayImpl(display);
        return displayImpl;
    }
    
    private static final class PiStormsDisplayImpl implements AutoCloseable, PiStormsDisplay {
/*
        private final NotifyingTouchScreen touchScreen;
        private final JPanel panel;
        private final ILI9341 display;
        private final EventBus eventBus;
        private final DisplayRepaintManager displayRepaintManager;
        
        public PiStormsDisplayImpl(NotifyingTouchScreen touchScreen, JPanel panel, ILI9341 display, EventBus eventBus, DisplayRepaintManager displayRepaintManager) {
            this.touchScreen = touchScreen;
            this.panel = panel;
            this.display = display;
            this.eventBus = eventBus;
            this.displayRepaintManager = displayRepaintManager;
        }

        @Override
        public NotifyingTouchScreen getTouchScreen() {
            return touchScreen;
        }

        @Override
        public JPanel getPanel() {
            return panel;
        }
*/
        private final ILI9341 display;
        
        public PiStormsDisplayImpl(ILI9341 display) {
            this.display = display;
        }

/*
        @Override
        public Display getDisplay() {
            return display;
        }

        @Override
        public EventBus getEventBus() {
            return eventBus;
        }
*/

        @Override
        public void close() throws Exception {
            display.clear(Color.BLACK);
            display.display();
            
/*
            touchScreen.close();
            displayRepaintManager.close();
*/
        }
    }
}
