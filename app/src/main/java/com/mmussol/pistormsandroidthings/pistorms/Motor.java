package com.mmussol.pistormsandroidthings.pistorms;

import java.io.IOException;

public class Motor implements Constants {

    private PiStorms mPiStorms;
    private PS_Port mPort;

    private int mPsControlSpeed;

    private static final int PS_CONTROL_SPEED = 0x01;
    private static final int PS_CONTROL_TIME = 0x40;
    private static final int PS_CONTROL_GO = 0x80;

    public Motor(PS_Port port) throws IOException {
        mPiStorms = PiStorms.getPiStorms();
        mPort = port;
        if (mPort == PS_Port.BAM1 || mPort == PS_Port.BBM1) {
            mPsControlSpeed = PS_M1_SPEED;
        } else {
            mPsControlSpeed = PS_M2_SPEED;
        }
        mPiStorms.addMotor(this, port);
    }

    public void setSpeed(int speed) throws InterruptedException, IOException {
        byte ctrl = (byte) (PS_CONTROL_SPEED | PS_CONTROL_GO);
        byte[] buffer = {(byte) speed, 0, 0, ctrl};
        mPiStorms.writeBuffer(mPort, mPsControlSpeed, buffer, buffer.length);
    }
}
