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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.script.SimpleBindings;

import org.linaro.utils.Constants;
import org.linaro.utils.DeviceForAster;
import org.linaro.utils.LinaroUtils;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.wookieerunner.ScriptRunner;

import com.google.common.io.Files;

public abstract class AsterCommand {
    // directory where images/files are saved for this command
    private String rootPath = null;

    public String getRootPath() {
        if (rootPath == null) {
            return System.getProperty("user.dir");
        } else {
            return rootPath;
        }
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @SuppressWarnings({ "rawtypes", "serial" })
    private static final Map<String, Class> supportedCommands = new LinkedHashMap<String, Class>() {
        {
            put("Touch", Touch.class);
            put("TouchWithText", TouchWithText.class);
            put("TouchWithContentDesc", TouchWithContentDesc.class);
            put("TouchWithResId", TouchWithResId.class);
            put("WaitIdWithTextMatch", WaitIdWithTextMatch.class);
            put("Drag", Drag.class);
            put("Press", Press.class);
            put("Type", Type.class);
            put("Call", Call.class);
            put("InstallApk", InstallApk.class);
            put("UninstallPackage", UninstallPackage.class);
            put("AdbShell", AdbShell.class);
            put("WaitTimeout", WaitTimeout.class);
            put("WaitImage", WaitImage.class);
            put("Screencap", Screencap.class);
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
    protected String inputFileName = new String();

    protected AsterOperation[] mOps;
    protected boolean mExecuting = false;
    protected boolean mFilled = false;

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

    public void saveInputFile(String prefix) {
        if (inputFileName != null && !inputFileName.isEmpty()) {
            File orgInputFile = new File(new File(this.getRootPath(),
                    inputFileName).getAbsolutePath());
            String baseName = orgInputFile.getName();
            File targetFile = new File(prefix, baseName);
            if (!orgInputFile.getAbsolutePath().equals(
                    targetFile.getAbsolutePath())) {
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                LinaroUtils.copyFile(orgInputFile, targetFile);
            }
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
    public static AsterCommand getAsterCommandSubInstance(String rootpath,
            String line) {
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

    public static ExecutionResult runAsterScript(String scriptPath,
            DeviceForAster device) {

        ArrayList<AsterCommand> cmds = null;
        try {
            cmds = loadActionsFromAsterScript(scriptPath);
        } catch (IOException e) {
            return new AsterCommand.ExecutionResult(false, e.toString());
        }

        for (AsterCommand c : cmds) {
            if (Constants.DEBUG) {
                System.out.printf("%s", c.toScript());
            }
            // AsterCommand.ExecutionResult result = c
            c.execute(device);
            // if (result.mSuccess != true) {
            // return result;
            // }
            // try {
            // Thread.sleep(1000);
            // } catch (InterruptedException e) {
            // }
        }
        return new AsterCommand.ExecutionResult(true, "");
    }

    public static ArrayList<AsterCommand> loadActionsFromAsterScript(
            String scriptPath) throws IOException {
        ArrayList<AsterCommand> cmds = new ArrayList<AsterCommand>();
        String filename = "script.py";
        File rootDir = unzipDir(scriptPath);

        try {
            FileInputStream ist = new FileInputStream(new File(
                    rootDir.getAbsolutePath(), filename));
            byte[] buf = new byte[4096];
            String data = new String();

            try {
                while (ist.available() > 0) {
                    ist.read(buf);
                    data += new String(buf);
                }
            } catch (IOException e) {
                System.out.println(e);
            }

            for (String line : data.split("\n")) {
                AsterCommand asterCmdInstance = AsterCommand
                        .getAsterCommandSubInstance(rootDir.getAbsolutePath(),
                                line);
                if (asterCmdInstance != null) {
                    cmds.add(asterCmdInstance);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return cmds;
    }

    private static File unzipDir(String zipfile) throws IOException {
        Enumeration<?> entries;
        ZipFile zipFile = new ZipFile(zipfile);
        byte[] buffer = new byte[4096];
        int len = 0;

        File rootDir = Files.createTempDir();
        rootDir.mkdirs();

        entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                (new File(rootDir.getAbsolutePath(), entry.getName())).mkdir();
                continue;
            }

            InputStream is = zipFile.getInputStream(entry);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(
                    (new File(rootDir.getAbsolutePath(), entry.getName()))
                            .getAbsolutePath()));
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            is.close();
            os.close();
        }
        zipFile.close();
        return rootDir;
    }
}
