package org.linaro.utils;

import java.util.ArrayList;

import org.linaro.utils.RuntimeWrapper.RuntimeResult;

public class LocalMonkeyRunner extends DeviceForAster {
    public LocalMonkeyRunner(String serial) {
        super(serial);
    }

    public String getScreenShotPath(){
        return null;
    }

    @Override
    protected ArrayList<String> getAdbSerialArrayList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RuntimeResult executeAdbShell(String... cmds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RuntimeResult executeAdbCommands(String... cmds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void installApk(String apkFilePath) {
        // TODO Auto-generated method stub

    }

    @Override
    public void push(String filePathHost) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pull(String filePathHost, String fileDevicePath) {
        // TODO Auto-generated method stub

    }

}
