package org.linaro.utils;

import java.util.ArrayList;

public class SshAdb extends DeviceForAster {
    public String adbHost = Contants.SSH_ADB_HOST;

    public String getAdbHost() {
        return adbHost;
    }

    public SshAdb(String serial, String adb_host) {
        super(serial);
        this.adbHost = adb_host;
    }

    protected ArrayList<String> getAdbSerialArrayList() {
        ArrayList<String> cmdArray = new ArrayList<String>();

        if (this.serial == null) {
            cmdArray.add("ssh");
            cmdArray.add(adbHost);
            cmdArray.add("adb");
        } else {
            cmdArray.add("ssh");
            cmdArray.add(adbHost);
            cmdArray.add("adb");
            cmdArray.add("-s");
            cmdArray.add(this.serial);
        }
        return cmdArray;
    }

    public void executeAdbCommands(String... cmds) {
        ArrayList<String> cmdArray = getAdbSerialArrayList();
        RuntimeWrapper.executeCommand(RuntimeWrapper.merge2Strings(cmdArray,
                cmds));
    }

    public void executeAdbShell(String... cmds) {
        String[] shell = new String[] { "shell" };
        executeAdbCommands(RuntimeWrapper.merge2Strings(shell, cmds));
    }

    public String getScreenShotPath(){
        executeAdbShell("screencap", Contants.SCR_PATH_DEVICE);
        executeAdbCommands("pull", Contants.SCR_PATH_DEVICE,
                Contants.SCR_PATH_HOST);
        RuntimeWrapper.executeCommand("scp",
                String.format("%s:%s", adbHost, Contants.SCR_PATH_HOST),
                Contants.SCR_PATH_HOST);
        return Contants.SCR_PATH_HOST;
    }
}
