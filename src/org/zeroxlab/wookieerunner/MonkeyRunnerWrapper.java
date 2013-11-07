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

    static public IChimpDevice getLastChimpDevice() {
        return device;
    }

    public static IChimpDevice getLastMonkeyDeviceWrapper() {
        return device;
    }

    public static MonkeyDeviceWrapper waitForConnection(int timeout, String deviceId) {
        device = chimpchat.waitForConnection(timeout, deviceId);
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

    /*
     * public static void takeSnapshot(String path){ if(!path.endsWith(".png")){
     * path = path + ".png"; }
     * monkeyDeviceWrapper.takeSnapshot().writeToFile(path, "png"); }
     * 
     * public static String getProperty(String key){ return
     * monkeyDeviceWrapper.getProperty(key); }
     * 
     * public static String getSystemProperty(String key){ return
     * monkeyDeviceWrapper.getSystemProperty(key); }
     * 
     * public static boolean installPackage(String path){ return
     * monkeyDeviceWrapper.installPackage(path); }
     * 
     * public static boolean removePackage(String packageName){ return
     * monkeyDeviceWrapper.removePackage(packageName); }
     * 
     * public static void wake(){ monkeyDeviceWrapper.wake(); }
     * 
     * public static void wait(String name, double timeout, double similarity,
     * boolean landscape) throws Exception { monkeyDeviceWrapper.wait(name,
     * timeout, similarity, landscape); }
     * 
     * public static void wait(String name, boolean landscape) throws Exception
     * { monkeyDeviceWrapper.wait(name, landscape); }
     * 
     * public static void press(String name, String touchType) {
     * monkeyDeviceWrapper.press(name, touchType); }
     * 
     * public static void type(String message) {
     * monkeyDeviceWrapper.type(message); }
     * 
     * public static String shell(String cmd) { return
     * monkeyDeviceWrapper.shell(cmd); }
     * 
     * public static void reboot(String into) {
     * monkeyDeviceWrapper.reboot(into); }
     * 
     * public static void push(String local, String remote) {
     * monkeyDeviceWrapper.push(local, remote); }
     * 
     * public static void pull(String remote, String local) {
     * monkeyDeviceWrapper.pull(remote, local); }
     * 
     * public static void startActivity(String uri, String action, String data,
     * String mimetype, Collection<String> categories, Map<String, Object>
     * extras, String component, int flags) {
     * monkeyDeviceWrapper.startActivity(uri, action, data, mimetype,
     * categories, extras, component, flags); }
     * 
     * public static void broadcastIntent(String uri, String action, String
     * data, String mimetype, Collection<String> categories, Map<String, Object>
     * extras, String component, int flags) {
     * monkeyDeviceWrapper.broadcastIntent(uri, action, data, mimetype,
     * categories, extras, component, flags); }
     * 
     * public static Map<String, Object> instrument(String packageName,
     * Map<String, Object> instrumentArgs) { Map<String, Object> result =
     * monkeyDeviceWrapper.instrument(packageName, instrumentArgs); return
     * result; }
     * 
     * public static void drag(int startx, int starty, int endx, int endy, int
     * steps, long seconds) { monkeyDeviceWrapper.drag(startx, starty, endx,
     * endy, steps, seconds); }
     * 
     * public static void drag(String start_img, int dx, int dy, int steps,
     * double sec, boolean landscape) throws Exception {
     * monkeyDeviceWrapper.drag(start_img, dx, dy, steps, sec, -1, -1,
     * landscape); }
     * 
     * public static void drag(String start_img, String end_img, int steps,
     * double seconds, boolean landscape) throws FileNotFoundException,
     * TemplateNotFoundException { monkeyDeviceWrapper.drag(start_img, end_img,
     * steps, seconds, -1, -1, landscape); }
     * 
     * public static void touch(int x, int y, TouchPressType type) {
     * monkeyDeviceWrapper.touch(x, y, type); }
     * 
     * public static void touch(String name, String typestr, double timeout,
     * double similarity, boolean landscape) throws Exception {
     * monkeyDeviceWrapper .touch(name, typestr, timeout, similarity,
     * landscape); }
     */
}
