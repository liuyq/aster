package org.linaro.utils;

public interface Constants {
    public static final String SCR_DIR_HOST = "/tmp/";
    public static final String SCREENCAP_RAW_DEVICE = "/data/local/tmp/aster_screencap.raw";
    public static final String XML_LAYOUT_FILE_DEVICE_PATH = "/data/local/tmp/uiautomator-dump-compressed.xml";

    public static final int DDMS_RAWIMAGE_VERSION = 1;

    public static final String SSH_ADB_HOST = "aster-adb-host";

    public static double DEFAULT_SIMILARITY = 0.9;
    public static double DEFAULT_TIMEOUT = 30;

    public static final String ADB_TYPE_LAVA = "LAVA";
    public static final String ADB_TYPE_LOCAL = "LOCAL";
    public static final String ADB_TYPE_MONKEYRUNNER = "MONKEYRUNNER";
    public static final String ADB_TYPE_SSH = "SSH";
    public static final String ADB_TYPE_DEFAULT = ADB_TYPE_LOCAL;
    public static final String[] ADB_TYPES = { ADB_TYPE_LOCAL, ADB_TYPE_SSH,
            ADB_TYPE_MONKEYRUNNER };

    public static boolean DEBUG = false;
    public static boolean DEBUG_CMDLINE = false;
    public static boolean DEBUG_IMAGE_MATCH = false;

    public static final int DEFAULT_ROTATE_ORDER = 270;

    public static enum ExecutionState {
        NORMAL, EXECUTION
    }

    public static enum ScreenOrientation {
        PORTRAIT, LANDSCAPE,
    }

    public static int STATUS_BAR_ROWS = 7;
}