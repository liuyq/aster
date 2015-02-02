/*
 * Copyright (C) 2011 0xlab - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authored by Kan-Ru Chen <kanru@0xlab.org>
 */

package org.zeroxlab.aster.cmds;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.script.SimpleBindings;

import org.linaro.utils.DeviceForAster;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.wookieerunner.ScriptRunner;

public abstract class AsterCommand {

    @SuppressWarnings({ "rawtypes", "serial" })
    private static final Map<String, Class> supportedCommands = new LinkedHashMap<String, Class>() {
        {
            put("Touch", Touch.class);
            put("Drag", Drag.class);
            put("Press", Press.class);
            put("Type", Type.class);
            // put("Recall", Recall.class);
            put("AdbShell", AdbShell.class);
            put("Wait", Wait.class);
        }
    };

    @SuppressWarnings("rawtypes")
    public static Map<String, Class> getSupportedcommands() {
        return supportedCommands;
    }

    protected DeviceForAster device;

    public void setDevice(DeviceForAster device) {
        this.device = device;
    }

    protected static ScriptRunner mRunner;
    protected static int mSeqNext = 0;
    protected int mSerial = 0;
    protected boolean mLandscape = false;
    protected BufferedImage mImage = null;
    protected AsterOperation[] mOps;
    protected boolean mExecuting = false;
    protected boolean mFilled = false;

    // protected MonkeyDeviceWrapper monkeyDeviceWrapper = null;

    public static class ExecutionResult {
        public boolean mSuccess;
        public String mMessage;

        public ExecutionResult() {
        }

        public ExecutionResult(boolean success, String message) {
            mSuccess = success;
            mMessage = message;
        }
    }

    public boolean isFilled() {
        return mFilled;
    }

    protected void setFilled(boolean status) {
        mFilled = status;
    }

    static public void setScriptRunner(ScriptRunner runner) {
        mRunner = runner;
    }

    synchronized public void setExecuting(boolean status) {
        mExecuting = status;
    }

    synchronized public boolean isExecuting() {
        return mExecuting;
    }

    synchronized public boolean isLandscape() {
        return mLandscape;
    }

    protected String[] splitArgs(String argline) {
        String[] args = argline.split(",");
        for (int i = 0; i < args.length; ++i) {
            args[i] = args[i].replaceAll("^[() ]+", "").replaceAll("[() ]+$",
                    "");
        }
        return args;
    }

    protected String stripQuote(String src) {
        return src.replaceAll("'", "").replaceAll("\"", "");
    }

    public BufferedImage getImage() {
        return mImage;
    }

    public void saveImage(String prefix) throws IOException {
        if (mImage != null) {
            File pngfile = new File(prefix, String.format("%d.png", mSerial));
            pngfile.delete();
            ImageIO.write(mImage, "png", pngfile);
        }
    }

    /* Return operations that stored in this Command */
    public AsterOperation[] getOperations() {
        return mOps;
    }

    public final void fillSettings(SimpleBindings settings) throws IOException {
        setFilled(true);
        onFillSettings(settings);
    }

    /* Get name of command */
    public abstract String getName();

    /* Get settings of a command */
    public abstract SimpleBindings getSettings();

    /* Set settings of a command */
    protected abstract void onFillSettings(SimpleBindings settings)
            throws IOException;

    /* Dump command to script text */
    public abstract String toScript();

    public abstract void execute();

    public ExecutionResult execute(DeviceForAster device) {
        try {
            this.device = device;
            execute();
        } catch (Exception e) {
            e.printStackTrace();
            return new ExecutionResult(false, e.toString());
        }
        return new ExecutionResult(true, "");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AsterCommand getAsterCommandSubInstance(String rootpath, String line) {
        for (Entry<String, Class> entry : supportedCommands.entrySet()) {
            try {
                AsterCommand asterCommand = (AsterCommand) (entry.getValue()
                        .getConstructor().newInstance());
                if (line.startsWith(asterCommand.getCommandPrefix())) {
                    return (AsterCommand) (entry.getValue().getConstructor(
                            String.class, String.class).newInstance(rootpath,
                            line.substring(asterCommand.getCommandPrefix()
                                    .length(), line.length())));
                }
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    protected abstract String getCommandPrefix();
}
