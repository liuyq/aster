package org.linaro.utils;

import java.io.File;
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

    @Override
    public void installApk(String apkFilePath) {
        scpFileToSshRemote(apkFilePath);
        String baseName = new File(apkFilePath).getName();
        executeAdbCommands("install", String.format("~/%s", baseName));
    }

    public void push(String filePathHost) {
        scpFileToSshRemote(filePathHost);
        String baseName = new File(filePathHost).getName();
        executeAdbCommands("push", String.format("~/%s", baseName),
                String.format("/data/local/tmp/%s", baseName));
    }

    public void pull(String filePathHost, String fileDevicePath) {
        String baseName = new File(fileDevicePath).getName();
        executeAdbCommands("pull", fileDevicePath,
                String.format("~/%s", baseName));
        scpFileFromSshRemote(String.format("~/%s", baseName), filePathHost);
    }

    private RuntimeResult scpFileToSshRemote(String filePathHost) {
        String baseName = new File(filePathHost).getName();
        RuntimeResult res = RuntimeWrapper.executeCommand("scp", filePathHost,
                String.format("%s:~/%s", adbHost, baseName));
        return res;
    }

    private RuntimeResult scpFileFromSshRemote(String filePathHost,
            String filePathSshHost) {
        RuntimeResult res = RuntimeWrapper.executeCommand("scp",
                String.format("%s:", adbHost, filePathSshHost), filePathHost);
        return res;
    }
}
