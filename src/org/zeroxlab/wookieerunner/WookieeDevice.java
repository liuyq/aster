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

import org.zeroxlab.owl.Finder;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.PlainTemplateMatcher;
import org.zeroxlab.owl.PyramidTemplateMatcher;
import org.zeroxlab.owl.TemplateNotFoundException;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.TouchPressType;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;

import com.android.monkeyrunner.doc.MonkeyRunnerExported;
import com.android.monkeyrunner.JythonUtils;
import com.android.monkeyrunner.MonkeyImage;

import org.python.core.ArgParser;
import org.python.core.ClassDictInit;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.core.PyBoolean;

import java.io.FileNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/*
 * Abstract base class that represents a single connected Android
 * Device and provides MonkeyRunner API methods for interacting with
 * that device.  Each backend will need to create a concrete
 * implementation of this class.
 */
@MonkeyRunnerExported(doc = "Represents a device attached to the system.")
public class WookieeDevice extends PyObject implements ClassDictInit {
    private static final Logger LOG = Logger.getLogger(WookieeDevice.class.getName());
    private IMatcher matcher;
    private WookieeAPI wookiee;
    private final double default_timeout = 3;

    public static void classDictInit(PyObject dict) {
        JythonUtils.convertDocAnnotationsForClass(WookieeDevice.class, dict);
    }

    @MonkeyRunnerExported(doc = "Sends a DOWN event when used with touch() or press().")
    public static final String DOWN = TouchPressType.DOWN.getIdentifier();

    @MonkeyRunnerExported(doc = "Sends an UP event when used with touch() or press().")
    public static final String UP = TouchPressType.UP.getIdentifier();

    @MonkeyRunnerExported(doc = "Sends a DOWN event, immediately followed by an UP event when used with touch() or press()")
    public static final String DOWN_AND_UP = TouchPressType.DOWN_AND_UP.getIdentifier();

    private IChimpDevice impl;

    public WookieeDevice(IChimpDevice impl) {
        this.impl = impl;
        this.matcher = new PyramidTemplateMatcher();
        this.wookiee = new WookieeAPI(impl);
    }

    public IChimpDevice getImpl() {
        return impl;
    }

    private String getCurrentSnapshot() {
        IChimpImage image = impl.takeSnapshot();
        image.writeToFile("/tmp/owl.png", "png");
        return "/tmp/owl.png";
    }

    @MonkeyRunnerExported(doc = "Get the HierarchyViewer object for the device.",
            returns = "A HierarchyViewer object")
    public HierarchyViewer getHierarchyViewer(PyObject[] args, String[] kws) {
        return impl.getHierarchyViewer();
    }

    @MonkeyRunnerExported(doc =
    "Gets the device's screen buffer, yielding a screen capture of the entire display.",
            returns = "A MonkeyImage object (a bitmap wrapper)")
    public MonkeyImage takeSnapshot() {
        IChimpImage image = impl.takeSnapshot();
        return new MonkeyImage(image);
    }

    @MonkeyRunnerExported(doc = "Given the name of a variable on the device, " +
            "returns the variable's value",
            args = {"key"},
            argDocs = {"The name of the variable. The available names are listed in " +
            "http://developer.android.com/guide/topics/testing/monkeyrunner.html."},
            returns = "The variable's value")
    public String getProperty(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        return impl.getProperty(ap.getString(0));
    }

    @MonkeyRunnerExported(doc = "Synonym for getProperty()",
            args = {"key"},
            argDocs = {"The name of the system variable."},
            returns = "The variable's value.")
    public String getSystemProperty(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        return impl.getSystemProperty(ap.getString(0));
    }

    @MonkeyRunnerExported(doc = "Sends a touch event at the specified location",
            args = { "name", "type" },
            argDocs = { "name name of the image",
                        "touch event type as returned by TouchPressType()"})
    public void touch(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        PyObject arg1 = ap.getPyObject(0);
        PyObject arg2 = ap.getPyObject(1);

        if (arg1 instanceof PyInteger) {
            String typestr = ap.getString(2);
            TouchPressType type = TouchPressType.fromIdentifier(typestr);
            if (type == null) {
                LOG.warning(String.format("Invalid TouchPressType specified (%s) default used instead",
                            typestr));
                type = TouchPressType.DOWN_AND_UP;
            }
            impl.touch(((PyInteger) arg1).asInt(), ((PyInteger) arg2).asInt(),
                       type);
        } else {
            try {
                wookiee.touch(((PyString) arg1).asString(),
                              ((PyString) arg2).asString(),
                              JythonUtils.getFloat(ap, 2, default_timeout),
                              (Boolean)(ap.getPyObject(3, new PyBoolean(false))).__tojava__(Boolean.class));
            } catch (FileNotFoundException e) {
                throw new PyException(Py.IOError, e.toString());
            } catch (TemplateNotFoundException e) {
                throw new PyException(Py.KeyError, "Template not found");
            }
        }
    }

    @MonkeyRunnerExported(doc = "Simulates dragging (touch, hold, and move) on the device screen.",
            args = { "start", "end", "duration", "steps", "timeout", "landscape"},
            argDocs = { "The starting point for the drag (a tuple (x,y) in pixels, or a filename that specifies the target template)",
            "The end point for the drag (a tuple (x,y) in pixels",
            "Duration of the drag in seconds (default is 1.0 seconds)",
            "The number of steps to take when interpolating points. (default is 10)",
            "Timeout when using a template",
            "True if in landscape mode"})
    public void drag(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        PyObject startObject = ap.getPyObject(0);
        PyObject endObject = ap.getPyObject(1);
        int steps = ap.getInt(3, 10);

        int startx, starty, endx, endy;
        double seconds = JythonUtils.getFloat(ap, 2, 1.0);

        if (startObject instanceof PyTuple && endObject instanceof PyTuple) {
            PyTuple start = (PyTuple) startObject;
            PyTuple end = (PyTuple) endObject;
            long ms = (long) (seconds * 1000.0);

            startx = (Integer) start.__getitem__(0).__tojava__(Integer.class);
            starty = (Integer) start.__getitem__(1).__tojava__(Integer.class);
            endx = (Integer) end.__getitem__(0).__tojava__(Integer.class);
            endy = (Integer) end.__getitem__(1).__tojava__(Integer.class);
            impl.drag(startx, starty, endx, endy, steps, ms);
        } else if (startObject instanceof PyString &&
                   endObject instanceof PyTuple) {
            PyTuple end = (PyTuple) endObject;

            endx = (Integer) end.__getitem__(0).__tojava__(Integer.class);
            endy = (Integer) end.__getitem__(1).__tojava__(Integer.class);

            try {
                wookiee.drag(((PyString) startObject).asString(), endx, endy,
                             steps, seconds,
                             JythonUtils.getFloat(ap, 4, default_timeout),
                             (Boolean)(ap.getPyObject(5, new PyBoolean(false))).__tojava__(Boolean.class));
            } catch (FileNotFoundException e) {
                throw new PyException(Py.IOError, e.toString());
            } catch (TemplateNotFoundException e) {
                throw new PyException(Py.KeyError, "Template not found");
            }
        } else {
            try {
                wookiee.drag(((PyString) startObject).asString(),
                             ((PyString) endObject).asString(),
                             steps, seconds,
                             JythonUtils.getFloat(ap, 4, default_timeout));
            } catch (FileNotFoundException e) {
                throw new PyException(Py.IOError, e.toString());
            } catch (TemplateNotFoundException e) {
                throw new PyException(Py.KeyError, "Template not found");
            }
        }
    }

    @MonkeyRunnerExported(doc = "Send a key event to the specified key",
            args = { "name", "type" },
            argDocs = { "the keycode of the key to press (see android.view.KeyEvent)",
            "touch event type as returned by TouchPressType(). To simulate typing a key, " +
            "send DOWN_AND_UP"})
    public void press(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String name = ap.getString(0);
        String touchType = ap.getString(1);

        // The old docs had this string, and so in favor of maintaining
        // backwards compatibility, let's special case it to the new one.
        if (touchType.equals("DOWN_AND_UP")){
            touchType = "downAndUp";
        }
        TouchPressType type = TouchPressType.fromIdentifier(ap.getString(1));
        if (type == null) {
            LOG.warning(String.format("Invalid TouchPressType specified (%s) default used instead",
                    ap.getString(1)));
            type = TouchPressType.DOWN_AND_UP;
        }

        impl.press(name, type);
    }

    @MonkeyRunnerExported(doc = "Types the specified string on the keyboard. This is " +
            "equivalent to calling press(keycode,DOWN_AND_UP) for each character in the string.",
            args = { "message" },
            argDocs = { "The string to send to the keyboard." })
    public void type(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String message = ap.getString(0);
        impl.type(message);
    }

    @MonkeyRunnerExported(doc = "Executes an adb shell command and returns the result, if any.",
            args = { "cmd"},
            argDocs = { "The adb shell command to execute." },
            returns = "The output from the command.")
    public String shell(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String cmd = ap.getString(0);
        return impl.shell(cmd);
    }

    @MonkeyRunnerExported(doc = "Reboots the specified device into a specified bootloader.",
            args = { "into" },
            argDocs = { "the bootloader to reboot into: bootloader, recovery, or None"})
    public void reboot(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String into = ap.getString(0, null);

        impl.reboot(into);
    }

    @MonkeyRunnerExported(doc = "Push a local file to remote device.",
            args = { "local", "remote" },
            argDocs = { "the local filepath",
                        "the remote filepath"})
    public void push(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String local = ap.getString(0);
        String remote = ap.getString(1);

        impl.pushFile(local, remote);
    }

    @MonkeyRunnerExported(doc = "Pull a file from remote device.",
            args = { "remote", "local" },
            argDocs = { "the remote filepath",
                        "the local filepath"})
    public void pull(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String remote = ap.getString(0);
        String local = ap.getString(1);

        impl.pullFile(remote, local);
    }

    @MonkeyRunnerExported(doc = "Installs the specified Android package (.apk file) " +
            "onto the device. If the package already exists on the device, it is replaced.",
            args = { "path" },
            argDocs = { "The package's path and filename on the host filesystem." },
            returns = "True if the install succeeded")
    public boolean installPackage(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String path = ap.getString(0);
        return impl.installPackage(path);
    }

    @MonkeyRunnerExported(doc = "Deletes the specified package from the device, including its " +
            "associated data and cache.",
            args = { "package"},
            argDocs = { "The name of the package to delete."},
            returns = "True if remove succeeded")
    public boolean removePackage(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String packageName = ap.getString(0);
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

    public void startActivity(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String uri = ap.getString(0, null);
        String action = ap.getString(1, null);
        String data = ap.getString(2, null);
        String mimetype = ap.getString(3, null);
        Collection<String> categories = Collections2.transform(JythonUtils.getList(ap, 4),
                Functions.toStringFunction());
        Map<String, Object> extras = JythonUtils.getMap(ap, 5);
        String component = ap.getString(6, null);
        int flags = ap.getInt(7, 0);

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
    public void broadcastIntent(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String uri = ap.getString(0, null);
        String action = ap.getString(1, null);
        String data = ap.getString(2, null);
        String mimetype = ap.getString(3, null);
        Collection<String> categories = Collections2.transform(JythonUtils.getList(ap, 4),
                Functions.toStringFunction());
        Map<String, Object> extras = JythonUtils.getMap(ap, 5);
        String component = ap.getString(6, null);
        int flags = ap.getInt(7, 0);

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

    public PyDictionary instrument(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String packageName = ap.getString(0);
        Map<String, Object> instrumentArgs = JythonUtils.getMap(ap, 1);
        if (instrumentArgs == null) {
            instrumentArgs = Collections.emptyMap();
        }

        Map<String, Object> result = impl.instrument(packageName, instrumentArgs);
        return JythonUtils.convertMapToDict(result);
    }

    @MonkeyRunnerExported(doc = "Wake up the screen on the device")
    public void wake(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        impl.wake();
    }
}
