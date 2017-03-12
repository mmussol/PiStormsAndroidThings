package com.mmussol.pistormsandroidthings.pistorms;

public interface Constants {

    // Tag for Android logging, use "adb logcat -s PiStorms:v"
    static final String PS_TAG = "PiStorms";

    enum PS_Port {
        // Motor ports
        BAM1("BAM1"),
        BAM2("BAM2"),
        BBM1("BBM1"),
        BBM2("BBM2"),

        // Sensor ports
        BAS1("BAS1"),
        BAS2("BAS2"),
        BBS1("BBS1"),
        BBS2("BBS2");

        private final String name;

        private PS_Port(String nameVal) {
            this.name = nameVal;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum PS_Led {
        LED_A,
        LED_B
    }

    static final byte PS_A_ADDRESS = 0x34;
    static final byte PS_B_ADDRESS = 0x36;

    // Command register
    static final int PS_COMMAND = 0x41;

    // Motor 1
    static final int PS_M1_ENCODER_TARGET = 0x42;
    static final int PS_M1_SPEED = 0x46;
    static final int PS_M1_TIME = 0x47;
    static final int PS_M1_COMMAND_B = 0x48;
    static final int PS_M1_COMMAND_A = 0x49;

    // Motor 2
    static final int PS_M2_ENCODER_TARGET = 0x4A;
    static final int PS_M2_SPEED = 0x4E;
    static final int PS_M2_TIME = 0x4F;
    static final int PS_M2_COMMAND_B = 0x50;
    static final int PS_M2_COMMAND_A = 0x51;

    // LED
    static final int PS_RED = 0xD7;
    static final int PS_GREEN = 0xD8;
    static final int PS_BLUE = 0xD9;

    // Buttons
    static final int PS_KEY_PRESS = 0xDA;
    static final int PS_KEY1_COUNT = 0xDB;
    static final int PS_KEY2_COUNT = 0xDC;

    // I2C Commands
    static final int PS_COMMAND_R = 0x52; // Reset all Encoder values and motor params
    static final int PS_COMMAND_S = 0x53; // Issue commands to both motors at the same time
    static final int PS_COMMAND_a = 0x61; // Motor 1 float while stopping
    static final int PS_COMMAND_b = 0x62; // Motor 2 float while stopping
    static final int PS_COMMAND_c = 0x63; // Motor 1 & 2 float while stopping
    static final int PS_COMMAND_A = 0x41; // Motor 1 brake while stopping
    static final int PS_COMMAND_B = 0x42; // Motor 2 brake while stopping
    static final int PS_COMMAND_C = 0x43; // Motor 1 & 2 brake while stopping

    // Sensor Modes
    static final int PS_SENSOR_MODE_1 = 0x6F;
    static final int PS_SENSOR_MODE_2 = 0xA3;

    // Sensor Types
    static final int PS_SENSOR_TYPE_NONE = 0;
    static final int PS_SENSOR_TYPE_SWITCH = 1;
    static final int PS_SENSOR_TYPE_ANALOG = 2;
    static final int PS_SENSOR_TYPE_LIGHT_REFLECTED = 3;
    static final int PS_SENSOR_TYPE_LIGHT_AMBIENT = 4;
    static final int PS_SENSOR_TYPE_I2C = 9;
    static final int PS_SENSOR_TYPE_COLOR_FULL = 13;
    static final int PS_SENSOR_TYPE_COLOR_RED = 14;
    static final int PS_SENSOR_TYPE_COLOR_GREEN = 15;
    static final int PS_SENSOR_TYPE_COLOR_BLUE = 16;
    static final int PS_SENSOR_TYPE_COLOR_NONE = 17;
    static final int PS_SENSOR_TYPE_EV3_SWITCH = 18;
    static final int PS_SENSOR_TYPE_EV3 = 19;
}
