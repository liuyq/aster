package org.linaro.utils;

import java.io.File;
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

    @Override
    public void installApk(String apkFilePath) {
        super.executeAdbCommands("install", apkFilePath);
    }

    public void push(String filePathHost) {
        String baseName = new File(filePathHost).getName();
        executeAdbCommands("push", filePathHost,
                String.format("/data/local/tmp/%s", baseName));
    }

    public void pull(String filePathHost, String fileDevicePath) {
        executeAdbCommands("pull", fileDevicePath, filePathHost);
    }
}
