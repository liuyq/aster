package org.linaro.utils;

import java.util.ArrayList;


public class LocalAdb extends DeviceForAster {

    public LocalAdb(String serial) {
        super(serial);
    }

    @Override
    protected ArrayList<String> getAdbSerialArrayList() {
        ArrayList<String> cmdArray = new ArrayList<String>();

        if (this.serial == null) {
            cmdArray.add("adb");
        } else {
            cmdArray.add("adb");
            cmdArray.add("-s");
            cmdArray.add(this.serial);
        }
        return cmdArray;
    }

    @Override
    public void executeAdbCommands(String... cmds) {
        ArrayList<String> cmdArray = getAdbSerialArrayList();
        RuntimeWrapper.executeCommand(RuntimeWrapper.merge2Strings(cmdArray,
                cmds));
    }

    @Override
    public void executeAdbShell(String... cmds) {
        String[] shellCmds = new String[1];
        shellCmds[0] = "shell";
        executeAdbCommands(RuntimeWrapper.merge2Strings(shellCmds, cmds));
    }

    public String getScreenShotPath() {
        executeAdbShell("screencap", Contants.SCR_PATH_DEVICE);
        executeAdbCommands("pull", Contants.SCR_PATH_DEVICE,
                Contants.SCR_PATH_HOST);
        return Contants.SCR_PATH_HOST;
    }
}
