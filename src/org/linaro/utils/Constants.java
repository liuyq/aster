package org.linaro.utils;


public interface Constants {
    public static final String SCR_PATH_HOST = "/tmp/aster_screencap.png";
    public static final String SCR_PATH_HOST_ROTATED = "/tmp/aster_screencap_rotated.png";
    public static final String SCR_PATH_DEVICE = "/data/local/tmp/aster_screencap.png";

    public static final String SSH_ADB_HOST = "aster-adb-host";

    public static double DEFAULT_SIMILARITY = 0.9;
    public static double DEFAULT_TIMEOUT = 30;

    public static final String ADB_TYPE_LAVA = "LAVA";
    public static final String ADB_TYPE_LOCAL = "LOCAL";
    public static final String ADB_TYPE_MONKEYRUNNER = "MONKEYRUNNER";
    public static final String ADB_TYPE_SSH = "SSH";
    public static final String ADB_TYPE_DEFAULT = ADB_TYPE_LAVA;
    public static final String[] ADB_TYPES = { ADB_TYPE_LAVA, ADB_TYPE_LOCAL,
            ADB_TYPE_MONKEYRUNNER, ADB_TYPE_SSH };

    public static final String[] JUNO_DEVICES = { "juno-01", "juno-02",
            "juno-03", "juno-04", "juno-05", "juno-06", "juno-07", "juno-08",
            "juno-09" };
    public static final String[] JUNO_DEVICES_IP = { "10.10.3.7:5555",
            "10.10.3.8:5555", "10.10.3.9:5555", "10.10.3.10:5555",
            "10.10.3.11:5555", "10.10.3.12:5555", "10.10.3.13:5555",
            "10.7.0.5:5555", "10.10.3.14:5555" };

    public static boolean DEBUG_CMDLINE = false;
    public static boolean DEBUG_IMAGE_MATCH = false;

    public static final int DEFAULT_ROTATE_ORDER = 270;

    public static enum ExecutionState {
        NORMAL, EXECUTION
    }

    public static enum ScreenOrientation {
        PORTRAIT, LANDSCAPE,
    }

    public static int STATUS_BAR_ROWS = 10;
}