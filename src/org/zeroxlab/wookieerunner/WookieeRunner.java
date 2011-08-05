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

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpBackend;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.ChimpImageBase;

import com.android.monkeyrunner.doc.MonkeyRunnerExported;
import com.android.monkeyrunner.JythonUtils;
import com.android.monkeyrunner.MonkeyImage;

import org.python.core.ArgParser;
import org.python.core.ClassDictInit;
import org.python.core.PyException;
import org.python.core.PyObject;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

/**
 * This is the main interface class into the jython bindings.
 */
@MonkeyRunnerExported(doc = "Main entry point for WookieeRunner")
public class WookieeRunner extends PyObject implements ClassDictInit {
    private static final Logger LOG = Logger.getLogger(WookieeRunner.class.getCanonicalName());
    private static ChimpChat chimpchat;

    public static void classDictInit(PyObject dict) {
        JythonUtils.convertDocAnnotationsForClass(WookieeRunner.class, dict);
    }

    static void setChimpChat(ChimpChat chimp){
        chimpchat = chimp;
    }


    @MonkeyRunnerExported(doc = "Waits for the workstation to connect to the device.",
            args = {"timeout", "deviceId"},
            argDocs = {"The timeout in seconds to wait. The default is to wait indefinitely.",
            "A regular expression that specifies the device name. See the documentation " +
            "for 'adb' in the Developer Guide to learn more about device names."},
            returns = "A ChimpDevice object representing the connected device.")
    public static WookieeDevice waitForConnection(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        long timeoutMs;
        try {
            double timeoutInSecs = JythonUtils.getFloat(ap, 0);
            timeoutMs = (long) (timeoutInSecs * 1000.0);
        } catch (PyException e) {
            timeoutMs = Long.MAX_VALUE;
        }

        IChimpDevice device = chimpchat.waitForConnection(timeoutMs,
                ap.getString(1, ".*"));
        WookieeDevice chimpDevice = new WookieeDevice(device);
        return chimpDevice;
    }

    @MonkeyRunnerExported(doc = "Pause the currently running program for the specified " +
            "number of seconds.",
            args = {"seconds"},
            argDocs = {"The number of seconds to pause."})
    public static void sleep(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        double seconds = JythonUtils.getFloat(ap, 0);

        long ms = (long) (seconds * 1000.0);

        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "Error sleeping", e);
        }
    }

    @MonkeyRunnerExported(doc = "Loads a MonkeyImage from a file.",
            args = { "path" },
            argDocs = {
            "The path to the file to load.  This file path is in terms of the computer running " +
            "WookieeRunner and not a path on the Android Device. " },
            returns = "A new MonkeyImage representing the specified file")
    public static MonkeyImage loadImageFromFile(PyObject[] args, String kws[]) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);

        String path = ap.getString(0);
        IChimpImage image = ChimpImageBase.loadImageFromFile(path);
        return new MonkeyImage(image);
    }

}
