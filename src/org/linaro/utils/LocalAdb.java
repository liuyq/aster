package org.linaro.utils;

import java.util.ArrayList;


public class LocalAdb extends DeviceForAster {

    public LocalAdb(String serial) {
        super(serial);
    }

    @Override
    protected ArrayList<String> getAdbSerialArrayList() {
        ArrayList<String> cmdArray = new ArrayList<String>();

        if (this.getSerial() == null) {
            cmdArray.add("adb");
        } else {
            cmdArray.add("adb");
            cmdArray.add("-s");
            cmdArray.add(this.getSerial());
        }
        return cmdArray;
    }

    public String getScreenShotPath() {
        executeAdbShell("screencap", Constants.SCR_PATH_DEVICE);
        executeAdbCommands("pull", Constants.SCR_PATH_DEVICE,
                Constants.SCR_PATH_HOST);
        return Constants.SCR_PATH_HOST;
    }
}
