package com.mmussol.pistormsandroidthings.programs;

import android.util.Log;

import com.mmussol.pistormsandroidthings.pistorms.Constants;
import com.mmussol.pistormsandroidthings.pistorms.Led;
import com.mmussol.pistormsandroidthings.pistorms.Motor;
import com.mmussol.pistormsandroidthings.pistorms.PspController;
import com.mmussol.pistormsandroidthings.pistorms.PspControllerButtons;

public class Demo extends Thread implements Constants {

    @Override
    public void run() {
        try {
            Led led1 = new Led(PS_Led.LED_A);
            Led led2 = new Led(PS_Led.LED_B);
            Motor motor1 = new Motor(PS_Port.BAM1);
            Motor motor2 = new Motor(PS_Port.BAM2);
            PspController psp = new PspController(PS_Port.BAS1);
            PspControllerButtons buttons = new PspControllerButtons();

            motor1.setSpeed(75);
            motor2.setSpeed(10);
            for (int i = 0; i < 10; i++) {
                Thread.sleep(500);
                led1.set(0, 0xff, 0);
                led2.set(0xff, 0, 0xff);

                Thread.sleep(500);
                led2.set(0, 0xff, 0);
                led1.set(0xff, 0, 0xff);
            }
            motor1.setSpeed(0);
            motor2.setSpeed(0);

            while (true) {
                psp.getButtons(buttons);
                if (buttons.up) {
                    motor1.setSpeed(0x50);
                    motor2.setSpeed(0x50);
                } else if (buttons.down) {
                    motor1.setSpeed(0xd0);
                    motor2.setSpeed(0xd0);
                } else {
                    motor1.setSpeed(0);
                    motor2.setSpeed(0);
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            Log.e(PS_TAG, "Got Exception: ", e);
        }
    }
}
