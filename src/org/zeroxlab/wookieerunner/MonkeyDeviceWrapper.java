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
 * Owl intergration for Aster by Wei-Ning Huang <azhuang@0xlab.org>
 */
package org.zeroxlab.wookieerunner;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import org.zeroxlab.owl.TemplateNotFoundException;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.TouchPressType;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;
import com.android.monkeyrunner.doc.MonkeyRunnerExported;

/*
 * Abstract base class that represents a single connected Android
 * Device and provides MonkeyRunner API methods for interacting with
 * that device.  Each backend will need to create a concrete
 * implementation of this class.
 */
public class MonkeyDeviceWrapper {
    private static final Logger LOG = Logger
            .getLogger(MonkeyDeviceWrapper.class.getName());
    private WookieeAPI wookiee;
    private final double default_timeout = 3;
    private final double default_similarity = 0.9;

    private IChimpDevice impl;

    public MonkeyDeviceWrapper(IChimpDevice impl) {
        this.impl = impl;
        this.wookiee = new WookieeAPI(impl);
    }

    public IChimpDevice getImpl() {
        return impl;
    }

    public HierarchyViewer getHierarchyViewer() {
        return impl.getHierarchyViewer();
    }

    public IChimpImage takeSnapshot() {
        IChimpImage image = impl.takeSnapshot();
        return image;
    }

    public String getProperty(String key) {
        return impl.getProperty(key);
    }

    public String getSystemProperty(String key) {
        return impl.getSystemProperty(key);
    }

    public void touch(int x, int y, String typeStr) {
        TouchPressType type = TouchPressType.fromIdentifier(typeStr);
        if (type == null)
            type = TouchPressType.DOWN_AND_UP;
        impl.touch(x, y, type);
    }

    public void touch(String name, String typestr, double timeout,
            double similarity, boolean landscape) throws Exception {
        wookiee.touch(name, typestr,
                timeout != -1 ? timeout : default_timeout,
                similarity != -1 ? similarity : default_similarity, landscape);
    }

    @MonkeyRunnerExported(doc = "Simulates dragging (touch, hold, and move) on the device screen.",
            args = { "start", "end", "duration", "steps", "timeout", "similarity", "landscape"},
            argDocs = { "The starting point for the drag (a tuple (x,y) in pixels, or a filename that specifies the target template)",
            "The end point for the drag (a tuple (x,y) in pixels",
            "Duration of the drag in seconds (default is 1.0 seconds)",
            "The number of steps to take when interpolating points. (default is 10)",
            "Timeout when using a template",
            "Mininum similarity to determine found",
            "True if in landscape mode"})
    public void drag(int startx, int starty, int endx, int endy, int steps,
            double seconds) {
        long ms = (long) (seconds * 1000.0);
        impl.drag(startx, starty, endx, endy, steps, ms);
    }

    public void drag(String start_img, int dx, int dy, int steps, double sec,
            double timeout, double similarity, boolean landscape)
            throws Exception {
        timeout = timeout != -1 ? timeout : default_timeout;
        similarity = similarity != -1 ? similarity : default_similarity;
        wookiee.drag(start_img, dx, dy, steps, sec, timeout, similarity,
                landscape);
    }

    public void drag(String start_img, String end_img, int steps,
            double seconds, double timeout, double similarity, boolean landscape)
            throws FileNotFoundException, TemplateNotFoundException {
        timeout = timeout != -1 ? timeout : default_timeout;
        similarity = similarity != -1 ? similarity : default_similarity;
        wookiee.drag(start_img, end_img, steps, seconds, timeout, similarity,
                landscape);
    }

    @MonkeyRunnerExported(doc = "Send a key event to the specified key",
            args = { "name", "type" },
            argDocs = { "the keycode of the key to press (see android.view.KeyEvent)",
            "touch event type as returned by TouchPressType(). To simulate typing a key, " +
            "send DOWN_AND_UP"})
    public void press(String name, String touchType) {
        // The old docs had this string, and so in favor of maintaining
        // backwards compatibility, let's special case it to the new one.
        if (touchType.equals("DOWN_AND_UP")){
            touchType = "downAndUp";
        }
        TouchPressType type = TouchPressType.fromIdentifier(touchType);
        if (type == null) {
            LOG.warning(String.format("Invalid TouchPressType specified (%s) default used instead",
                            touchType));
            type = TouchPressType.DOWN_AND_UP;
        }

        impl.press(name, type);
    }

    @MonkeyRunnerExported(doc = "Types the specified string on the keyboard. This is " +
            "equivalent to calling press(keycode,DOWN_AND_UP) for each character in the string.",
            args = { "message" },
            argDocs = { "The string to send to the keyboard." })
    public void type(String message) {
        impl.type(message);
    }

    @MonkeyRunnerExported(doc = "Image assertion",
            args = { "name", "timeout" },
            argDocs = { "name name of the image",
                        "timeout timeout before assertion failed"})
    public void wait(String name, double timeout, double similarity,
            boolean landscape) throws Exception {
        wookiee.wait(name, timeout, similarity, landscape);
    }

    public void wait(String name, boolean landscape) throws Exception {
        wookiee.wait(name, default_timeout, default_similarity, landscape);
    }

    @MonkeyRunnerExported(doc = "Executes an adb shell command and returns the result, if any.",
            args = { "cmd"},
            argDocs = { "The adb shell command to execute." },
            returns = "The output from the command.")
    public String shell(String cmd) {
        return impl.shell(cmd);
    }

    @MonkeyRunnerExported(doc = "Reboots the specified device into a specified bootloader.",
            args = { "into" },
            argDocs = { "the bootloader to reboot into: bootloader, recovery, or None"})
    public void reboot(String into) {
        impl.reboot(into);
    }

    @MonkeyRunnerExported(doc = "Push a local file to remote device.",
            args = { "local", "remote" },
            argDocs = { "the local filepath",
                        "the remote filepath"})
    public void push(String local, String remote) {
        impl.pushFile(local, remote);
    }

    @MonkeyRunnerExported(doc = "Pull a file from remote device.",
            args = { "remote", "local" },
            argDocs = { "the remote filepath",
                        "the local filepath"})
    public void pull(String remote, String local) {
        impl.pullFile(remote, local);
    }

    @MonkeyRunnerExported(doc = "Installs the specified Android package (.apk file) " +
            "onto the device. If the package already exists on the device, it is replaced.",
            args = { "path" },
            argDocs = { "The package's path and filename on the host filesystem." },
            returns = "True if the install succeeded")
    public boolean installPackage(String path) {
        return impl.installPackage(path);
    }

    @MonkeyRunnerExported(doc = "Deletes the specified package from the device, including its " +
            "associated data and cache.",
            args = { "package"},
            argDocs = { "The name of the package to delete."},
            returns = "True if remove succeeded")
    public boolean removePackage(String packageName) {
        return impl.removePackage(packageName);
    }

    @MonkeyRunnerExported(doc = "Starts an Activity on the device by sending an Intent " +
            "constructed from the specified parameters.",
            args = { "uri", "action", "data", "mimetype", "categories", "extras",
                     "component", "flags" },
            argDocs = { "The URI for the Intent.",
                        "The action for the Intent.",
                        "The data URI for the Intent",
                        "The mime type for the Intent.",
                        "A Python iterable containing the category names for the Intent.",
                        "A dictionary of extras to add to the Intent. Types of these extras " +
                        "are inferred from the python types of the values.",
                        "The component of the Intent.",
                        "An iterable of flags for the Intent." +
                        "All arguments are optional. The default value for each argument is null." +
                        "(see android.content.Intent)"})

    public void startActivity(String uri, String action, String data,
            String mimetype, Collection<String> categories,
            Map<String, Object> extras, String component, int flags) {
        impl.startActivity(uri, action, data, mimetype, categories, extras, component, flags);
    }

    @MonkeyRunnerExported(doc = "Sends a broadcast intent to the device.",
            args = { "uri", "action", "data", "mimetype", "categories", "extras",
                     "component", "flags" },
                     argDocs = { "The URI for the Intent.",
                             "The action for the Intent.",
                             "The data URI for the Intent",
                             "The mime type for the Intent.",
                             "An iterable of category names for the Intent.",
                             "A dictionary of extras to add to the Intent. Types of these extras " +
                             "are inferred from the python types of the values.",
                             "The component of the Intent.",
                             "An iterable of flags for the Intent." +
                             "All arguments are optional. " + "" +
                             "The default value for each argument is null." +
                             "(see android.content.Context.sendBroadcast(Intent))"})
    public void broadcastIntent(String uri, String action, String data,
            String mimetype, Collection<String> categories,
            Map<String, Object> extras, String component, int flags) {
        impl.broadcastIntent(uri, action, data, mimetype, categories, extras, component, flags);
    }

    @MonkeyRunnerExported(doc = "Run the specified package with instrumentation and return " +
            "the output it generates. Use this to run a test package using " +
            "InstrumentationTestRunner.",
            args = { "className", "args" },
            argDocs = { "The class to run with instrumentation. The format is " +
                        "packagename/classname. Use packagename to specify the Android package " +
                        "to run, and classname to specify the class to run within that package. " +
                        "For test packages, this is usually " +
                        "testpackagename/InstrumentationTestRunner",
                        "A map of strings to objects containing the arguments to pass to this " +
                        "instrumentation (default value is None)." },
            returns = "A map of strings to objects for the output from the package. " +
                      "For a test package, contains a single key-value pair: the key is 'stream' " +
                      "and the value is a string containing the test output.")

    public Map<String, Object> instrument(String packageName,
            Map<String, Object> instrumentArgs) {
        if (instrumentArgs == null) {
            instrumentArgs = Collections.emptyMap();
        }

        Map<String, Object> result = impl.instrument(packageName, instrumentArgs);
        return result;
    }

    public void wake() {
        impl.wake();
    }
}
