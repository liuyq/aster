package org.linaro.utils;

import java.util.ArrayList;

import org.linaro.utils.RuntimeWrapper.RuntimeResult;

public class SshAdb extends DeviceForAster {
    public String adbHost = Constants.SSH_ADB_HOST;

    public String getAdbHost() {
        return adbHost;
    }

    public SshAdb(String serial, String adb_host) {
        super(serial);
        this.adbHost = adb_host;
    }

    protected ArrayList<String> getAdbSerialArrayList() {
        ArrayList<String> cmdArray = new ArrayList<String>();

        if (this.getSerial() == null) {
            cmdArray.add("ssh");
            cmdArray.add(adbHost);
            cmdArray.add("adb");
        } else {
            cmdArray.add("ssh");
            cmdArray.add(adbHost);
            cmdArray.add("adb");
            cmdArray.add("-s");
            cmdArray.add(this.getSerial());
        }
        return cmdArray;
    }

    public String getScreenShotPath(){
        executeAdbShell("screencap", Constants.SCR_PATH_DEVICE);
        executeAdbCommands("pull", Constants.SCR_PATH_DEVICE,
                Constants.SCR_PATH_HOST);
        RuntimeResult res = RuntimeWrapper.executeCommand("scp",
                String.format("%s:%s", adbHost, Constants.SCR_PATH_HOST),
                Constants.SCR_PATH_HOST);
        if (res.getStatus() != 0) {
            return null;
        }
        return Constants.SCR_PATH_HOST;
    }
}
