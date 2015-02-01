package org.linaro.utils;

import java.util.TreeMap;

public class LinaroUtils {
    public static TreeMap<String, String> getJunoDevices() {
        TreeMap<String, String> juno_devices = new TreeMap<String, String>();
        for (int i = 0; i < Constants.JUNO_DEVICES.length; i++) {
            juno_devices.put(Constants.JUNO_DEVICES[i],
                    Constants.JUNO_DEVICES_IP[i]);
        }
        return juno_devices;
    }
}
