/*
 * Copyright (C) 2011 0xlab - http://0xlab.org/
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Owl integration for Aster by Wei-Ning Huang <azhuang@0xlab.org>
 */
package org.zeroxlab.wookieerunner;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.ChimpImageBase;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.monkeyrunner.MonkeyImage;
import com.android.monkeyrunner.doc.MonkeyRunnerExported;

/**
 * This is the main interface class into the java bindings.
 */
@MonkeyRunnerExported(doc = "Main entry point for WookieeRunner")
public class MonkeyRunnerWrapper {
    private static final Logger LOG = Logger
            .getLogger(MonkeyRunnerWrapper.class.getCanonicalName());
    private static ChimpChat chimpchat;
    private static IChimpDevice device = null;
    private static MonkeyDeviceWrapper monkeyDeviceWrapper = null;

    // This is a ungly hack, in oreder to get the IChimpDevice outside
    // WookieeRunner after the connection is established.
    static public void setChimpChat(ChimpChat chimp){
        chimpchat = chimp;
    }


    public static MonkeyDeviceWrapper waitForConnection(int timeout, String deviceId) {
        device = chimpchat.waitForConnection(timeout, deviceId);
        monkeyDeviceWrapper = new MonkeyDeviceWrapper(device);
        return monkeyDeviceWrapper;
    }

    public static MonkeyDeviceWrapper waitForConnection(String deviceId) {
        if (deviceId == null || deviceId.length() == 0) {
            device = chimpchat.waitForConnection();
        } else {
            device = chimpchat.waitForConnection(Integer.MAX_VALUE, deviceId);
        }
        monkeyDeviceWrapper = new MonkeyDeviceWrapper(device);
        return monkeyDeviceWrapper;
    }

    public static MonkeyDeviceWrapper waitForConnection() {
        device = chimpchat.waitForConnection();
        monkeyDeviceWrapper = new MonkeyDeviceWrapper(device);
        return monkeyDeviceWrapper;
    }

    public static MonkeyDeviceWrapper connect() {
        return waitForConnection();
    }

    public static MonkeyDeviceWrapper connect(String deviceId) {
        return waitForConnection(deviceId);
    }

    public static void sleep(int seconds) {
        long ms = (long) (seconds * 1000.0);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "Error sleeping", e);
        }
    }

    public static MonkeyImage loadImageFromFile(String path) {
        IChimpImage image = ChimpImageBase.loadImageFromFile(path);
        return new MonkeyImage(image);
    }
}
