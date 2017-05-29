package com.mmussol.pistormsandroidthings.pistorms;

import android.graphics.Color;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.mmussol.pistormsandroidthings.pistorms.display.ImageRotation;
import com.mmussol.pistormsandroidthings.pistorms.display.PiStormsDisplay;
import com.mmussol.pistormsandroidthings.pistorms.display.PiStormsDisplayBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PiStorms implements Constants {

    private static PiStorms mPiStorms;
    PeripheralManagerService mManager;
    private Bank mBankA, mBankB;
    protected final PiStormsDisplay mPiStormsDisplay;
    private Motor[] mMotors = new Motor[4];
    private Sensor[] mSensors = new Sensor[4];
    private I2cDevice[] mI2cSensors = new I2cDevice[4];
    private byte mKeyPressCount;
    private boolean mRunning = false;
    private List<PiStormsListener> listeners = new LinkedList<PiStormsListener>();

    // I2C Device Name (Bus)
    private static final String I2C_DEVICE_NAME = "I2C1";

    // Private constructor for singleton
    private PiStorms() throws IOException {
        mManager = new PeripheralManagerService();

        I2cDevice devA = mManager.openI2cDevice(PiStorms.I2C_DEVICE_NAME, PS_A_ADDRESS >> 1);
        mBankA = new Bank(mManager, PS_A_ADDRESS, devA);

        I2cDevice devB = mManager.openI2cDevice(PiStorms.I2C_DEVICE_NAME, PS_B_ADDRESS >> 1);
        mBankB = new Bank(mManager, PS_B_ADDRESS, devB);

        mPiStormsDisplay = PiStormsDisplayBuilder.newBuilder().setImageRotation(ImageRotation.NONE).build(devA);

        // Poll button count in background thread
        Thread goButtonThread = new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   resetKeyPressCount();
                   while (true) {
                       synchronized (this) {
                           byte count = mBankA.readByte(PS_KEY1_COUNT);
                           if (count > mKeyPressCount) {
                               Log.d(PS_TAG, "GO button count: " + count);
                               if (mRunning) {
                                   stop();
                               } else {
                                   start();
                               }
                               mKeyPressCount = count;
                               // Reset count well before 255 wrap-around point
                               if (count > 200) {
                                   resetKeyPressCount();
                               }
                           }
                       }
                       Thread.sleep(500);
                   }
               } catch (Exception e) {
                   Log.e(PS_TAG, "Go button thread exception: ", e);
               }
           }
        });
        goButtonThread.start();
    }

    public static synchronized PiStorms getPiStorms() throws IOException {
        if (mPiStorms == null) {
            mPiStorms = new PiStorms();
        }
        return mPiStorms;
    }

    public void addListener(PiStormsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(PiStormsListener listener) {
        listeners.remove(listener);
    }

    public void addMotor(Motor motor, PS_Port port) {
        switch (port) {
            case BAM1:
                mMotors[0] = motor;
                break;
            case BAM2:
                mMotors[1] = motor;
                break;
            case BBM1:
                mMotors[2] = motor;
                break;
            case BBM2:
                mMotors[3] = motor;
                break;
        }
    }

    public void addI2CSensor(Sensor sensor, PS_Port port, int i2cAddr)
            throws IOException, InterruptedException {

        I2cDevice i2cDevice = mManager.openI2cDevice(I2C_DEVICE_NAME, i2cAddr >> 1);;
        byte[] buffer = new byte[8];

        switch (port) {
            case BAS1:
                mBankA.writeByte(PS_SENSOR_MODE_1, (byte) PS_SENSOR_TYPE_I2C);
                mI2cSensors[0] = i2cDevice;
                mSensors[0] = sensor;
                break;
            case BAS2:
                mBankA.writeByte(PS_SENSOR_MODE_2, (byte) PS_SENSOR_TYPE_I2C);
                mI2cSensors[1] = i2cDevice;
                mSensors[1] = sensor;
                break;
            case BBS1:
                mBankB.writeByte(PS_SENSOR_MODE_1, (byte) PS_SENSOR_TYPE_I2C);
                mI2cSensors[2] = i2cDevice;
                mSensors[2] = sensor;
                break;
            case BBS2:
                mBankB.writeByte(PS_SENSOR_MODE_2, (byte) PS_SENSOR_TYPE_I2C);
                mI2cSensors[3] = i2cDevice;
                mSensors[3] = sensor;
                break;
        }

        i2cDevice.readRegBuffer(0x0, buffer, buffer.length);
        String vendorName = new String(buffer);

        i2cDevice.readRegBuffer(0x8, buffer, buffer.length);
        String deviceId = new String(buffer);

        i2cDevice.readRegBuffer(0x10, buffer, buffer.length);
        String firmwareVersion = new String(buffer);

        StringBuilder builder = new StringBuilder().
                append("    I2C Device Addr: ").append(i2cAddr).
                append("\n    Vendor: ").append(vendorName).
                append("\n    Device ID: ").append(deviceId).
                append("\n    Firmware Version: ").append(firmwareVersion).
                append('\n');

        Log.i(PS_TAG, "Added I2C device on port " + port);
        Log.i(PS_TAG, builder.toString());

    }

    synchronized public void setLed(PS_Led led, int red, int green, int blue)
            throws InterruptedException, IOException {

        if (mRunning) {
            if (led == PS_Led.LED_A) {
                mBankA.setLed(red, green, blue);
            } else {
                mBankB.setLed(red, green, blue);
            }
        } else {
            throw (new InterruptedException("PiStorms stopped"));
        }
    }

    synchronized public void writeBuffer(PS_Port port, int addr, byte[] buffer, int bufferLen)
            throws InterruptedException, IOException {

        if (mRunning) {
            switch (port) {
                case BAS1:
                    if (mI2cSensors[0] != null) {
                        mI2cSensors[0].writeRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BAS2:
                    if (mI2cSensors[1] != null) {
                        mI2cSensors[1].writeRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BAM1:
                case BAM2:
                    mBankA.writeBuffer(addr, buffer, bufferLen);
                    break;

                case BBS1:
                    if (mI2cSensors[2] != null) {
                        mI2cSensors[2].writeRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BBS2:
                    if (mI2cSensors[3] != null) {
                        mI2cSensors[3].writeRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BBM1:
                case BBM2:
                    mBankB.writeBuffer(addr, buffer, bufferLen);
                    break;
            }
        } else {
            throw (new InterruptedException("PiStorms stopped"));
        }
    }

    synchronized public void writeByte(PS_Port port, int addr, byte val)
            throws InterruptedException, IOException {

        if (mRunning) {
            switch (port) {
                case BAS1:
                    if (mI2cSensors[0] != null) {
                        mI2cSensors[0].writeRegByte(addr, val);
                        break;
                    }
                case BAS2:
                    if (mI2cSensors[1] != null) {
                        mI2cSensors[1].writeRegByte(addr, val);
                        break;
                    }
                case BAM1:
                case BAM2:
                    mBankA.writeByte(addr, val);
                    break;

                case BBS1:
                    if (mI2cSensors[2] != null) {
                        mI2cSensors[2].writeRegByte(addr, val);
                        break;
                    }
                case BBS2:
                    if (mI2cSensors[3] != null) {
                        mI2cSensors[3].writeRegByte(addr, val);
                        break;
                    }
                case BBM1:
                case BBM2:
                    mBankB.writeByte(addr, val);
                    break;
            }
        } else {
            throw (new InterruptedException("PiStorms stopped"));
        }
    }

    synchronized public void readBuffer(PS_Port port, int addr, byte[] buffer, int bufferLen)
            throws InterruptedException, IOException {

        if (mRunning) {
            switch (port) {
                case BAS1:
                    if (mI2cSensors[0] != null) {
                        mI2cSensors[0].readRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BAS2:
                    if (mI2cSensors[1] != null) {
                        mI2cSensors[1].readRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BAM1:
                case BAM2:
                    mBankA.readBuffer(addr, buffer, bufferLen);
                    break;

                case BBS1:
                    if (mI2cSensors[2] != null) {
                        mI2cSensors[2].readRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BBS2:
                    if (mI2cSensors[3] != null) {
                        mI2cSensors[3].readRegBuffer(addr, buffer, bufferLen);
                        break;
                    }
                case BBM1:
                case BBM2:
                    mBankB.readBuffer(addr, buffer, bufferLen);
                    break;
            }
        } else {
            throw (new InterruptedException("PiStorms stopped"));
        }
    }

    private void start() {
        mRunning = true;
        for (PiStormsListener l : listeners) {
            l.onProgramGo();
        }
    }

    private void stop() {
        mRunning = false;
        for (PiStormsListener l : listeners) {
            l.onProgramStop();
        }

        synchronized (this) {
            try {
                // Stop motors 1 & 2 and turn off LEDs
                mBankA.writeByte(PS_COMMAND, (byte) PS_COMMAND_C);
                mBankA.setLed(0, 0, 0);

                mBankB.writeByte(PS_COMMAND, (byte) PS_COMMAND_C);
                mBankB.setLed(0, 0, 0);

                for (int i = 0; i < 4; i++) {
                    // Close I2C sensors
                    if (mI2cSensors[i] != null) {
                        mI2cSensors[i].close();
                        mI2cSensors[i] = null;
                    }

                    // Clear motors
                    mMotors[i] = null;
                }

                // Clear sensors
                if (mSensors[0] != null) {
                    mBankA.writeByte(PS_SENSOR_MODE_1, (byte) PS_SENSOR_TYPE_NONE);
                    mSensors[0] = null;
                }
                if (mSensors[1] != null) {
                    mBankA.writeByte(PS_SENSOR_MODE_2, (byte) PS_SENSOR_TYPE_NONE);
                    mSensors[1] = null;
                }
                if (mSensors[2] != null) {
                    mBankB.writeByte(PS_SENSOR_MODE_1, (byte) PS_SENSOR_TYPE_NONE);
                    mSensors[2] = null;
                }
                if (mSensors[3] != null) {
                    mBankB.writeByte(PS_SENSOR_MODE_2, (byte) PS_SENSOR_TYPE_NONE);
                    mSensors[3] = null;
                }
            } catch (Exception e) {
                Log.e(PS_TAG, "Got exception during stop: ", e);
            }
        }
    }

    synchronized private void resetKeyPressCount()
            throws InterruptedException, IOException {
        // Clear the 'GO' button key press count
        mKeyPressCount = 0;
        mBankA.writeByte(PS_KEY1_COUNT, mKeyPressCount);
    }

    private class Bank {
        I2cDevice mI2cDevice;
        int mI2cAddr;

        Bank(PeripheralManagerService manager, int i2cAddr, I2cDevice i2cDev) throws IOException {
            mI2cAddr = i2cAddr;
            mI2cDevice = i2cDev;

            StringBuilder builder = new StringBuilder().
                    append("I2C Device Addr: ").append(i2cAddr).
                    append("\nVendor: ").append(getVendorName()).
                    append("\nDevice ID: ").append(getDeviceId()).
                    append("\nFirmware Version: ").append(getFirmwareVersion()).
                    append('\n');
            Log.i(PS_TAG, builder.toString());
        }

        String getFirmwareVersion() throws IOException {
            byte[] buffer = new byte[8];
            mI2cDevice.readRegBuffer(0x0, buffer, buffer.length);
            return (new String(buffer));
        }

        String getVendorName() throws IOException {
            byte[] buffer = new byte[8];
            mI2cDevice.readRegBuffer(0x8, buffer, buffer.length);
            return (new String(buffer));
        }

        String getDeviceId() throws IOException {
            byte[] buffer = new byte[8];
            mI2cDevice.readRegBuffer(0x10, buffer, buffer.length);
            return (new String(buffer));
        }

        void setLed(int red, int green, int blue)
                throws InterruptedException, IOException {
            mI2cDevice.writeRegByte(PS_RED, (byte) red);
            mI2cDevice.writeRegByte(PS_GREEN, (byte) green);
            mI2cDevice.writeRegByte(PS_BLUE, (byte) blue);
            Thread.sleep(10);
        }

        void writeByte(int addr, byte value) throws InterruptedException, IOException {
            mI2cDevice.writeRegByte(addr, value);
            Thread.sleep(10);
        }

        byte readByte(int addr) throws InterruptedException, IOException {
            byte val = mI2cDevice.readRegByte(addr);
            Thread.sleep(10);
            return val;
        }

        void writeBuffer(int addr, byte[] buffer, int bufferLen)
                throws InterruptedException, IOException {
            mI2cDevice.writeRegBuffer(addr, buffer, bufferLen);
            Thread.sleep(10);
        }

        void readBuffer(int addr, byte[] buffer, int bufferLen)
                throws InterruptedException, IOException {
            mI2cDevice.readRegBuffer(addr, buffer, bufferLen);
            Thread.sleep(10);
        }
    }
}
