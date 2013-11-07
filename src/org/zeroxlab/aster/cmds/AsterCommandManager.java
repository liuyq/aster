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
 * Authored by Wei-Ning Huang <azhuang@0xlab.org>
 */

package org.zeroxlab.aster.cmds;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.zeroxlab.wookieerunner.MonkeyDeviceWrapper;
import org.zeroxlab.wookieerunner.MonkeyRunnerWrapper;
import org.zeroxlab.wookieerunner.ScriptRunner;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.monkeyrunner.MonkeyFormatter;
import com.google.common.io.Files;


public class AsterCommandManager {

    private File mCwd = null;
    private File mFile = null;
    private static Stack<String> mPathStack = new Stack<String>();
    private static ChimpChat mChimpChat;
    private static IChimpDevice mImpl;
    private static ScriptRunner mScriptRunner;
    public static boolean mConnected = false;
    private static MonkeyDeviceWrapper deviceWrapper = null;

    public AsterCommandManager() {
        mCwd = Files.createTempDir();
        System.setProperty("user.dir", mCwd.getAbsolutePath());
    }

    public void cdCwd() {
        System.setProperty("user.dir", mCwd.getAbsolutePath());
    }

    public File getFile() {
        return mFile;
    }

    public boolean getSaved() {
        return mFile != null;
    }

    private void zipDir(File prefix, String dir, ZipOutputStream zos)
        throws FileNotFoundException, IOException {
        File dirfile = new File(dir);
        String[] dirlist = dirfile.list();
        byte[] buffer = new byte[4096];
        int len = 0;

        for (int i = 0; i < dirlist.length; ++i) {
            File f = new File(dirfile, dirlist[i]);

            if (f.isDirectory()) {
                zipDir(prefix, f.getPath(), zos);
                continue;
            }

            FileInputStream fis = new FileInputStream(f);
            String rpath = prefix.toURI().relativize(f.toURI()).getPath();
            ZipEntry ent = new ZipEntry(rpath);
            zos.putNextEntry(ent);
            while ((len = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            fis.close();
        }
    }

    private void unzipDir(String zipfile, String prefix)
        throws IOException {
        Enumeration entries;
        ZipFile zipFile = new ZipFile(zipfile);
        byte[] buffer = new byte[4096];
        int len = 0;

        (new File(prefix)).mkdirs();
        entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            if(entry.isDirectory()) {
                (new File(prefix, entry.getName())).mkdir();
                continue;
            }

            InputStream is = zipFile.getInputStream(entry);
            OutputStream os = new BufferedOutputStream(new FileOutputStream
                               ((new File(prefix, entry.getName())).getPath()));
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            is.close();
            os.close();
        }
        zipFile.close();
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private final void replaceAllLogFormatters(Formatter form, Level level) {
        LogManager mgr = LogManager.getLogManager();
        Enumeration<String> loggerNames = mgr.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();
            Logger logger = mgr.getLogger(loggerName);
            for (Handler handler : logger.getHandlers()) {
                handler.setFormatter(form);
                handler.setLevel(level);
            }
        }
    }

    public MonkeyDeviceWrapper connect() {
        replaceAllLogFormatters(MonkeyFormatter.DEFAULT_INSTANCE, Level.SEVERE);

        Map<String, String> options = new TreeMap<String, String>();
        options.put("backend", "adb");
        String adbLocation = System.getProperty("adb.location");
        if (adbLocation != null){
            options.put("adbLocation", "adb");
        }
        mChimpChat = ChimpChat.getInstance(options);
        MonkeyRunnerWrapper.setChimpChat(mChimpChat);
        deviceWrapper = MonkeyRunnerWrapper.connect();
        mImpl = MonkeyRunnerWrapper.getLastChimpDevice();

        mConnected = true;
        return deviceWrapper;
    }

    public IChimpImage takeSnapshot() {
        return mImpl.takeSnapshot();
    }

    public AsterCommand.ExecutionResult run(String astfile)
        throws IOException {
        connect();
        return runLocal(astfile);
    }

    public File findFile(String cwd, String astfile)
        throws IOException {
        File ast = new File(cwd, astfile);
        while (!ast.exists()) {
            if (ast.getParentFile().getParent() == null) {
                return null;
            }
            ast = new File(ast.getParentFile().getParent(), astfile);
        }
        return ast;
    }

    public AsterCommand.ExecutionResult runLocal(String astfile)
        throws IOException {
        if (!mPathStack.empty()) {
            try {
                astfile = findFile(mPathStack.peek(), astfile).getAbsolutePath();
            } catch(NullPointerException e) {
                throw new IOException(String.format("Can not open `%s'.", astfile));
            }
        }
        AsterCommand[] cmds = load(astfile);

        System.out.printf("Staring command execution...\n");
        for (AsterCommand c: cmds) {
            System.err.printf("%s", c.toScript());
            cdCwd();
            AsterCommand.ExecutionResult result = c.execute();
            if (result.mSuccess != true) {
                System.err.println(result.mMessage);
                mPathStack.pop();
                return result;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        mPathStack.pop();
        return new AsterCommand.ExecutionResult(true, "");
    }
    
    public void dump(AsterCommand[] cmds, String filename, boolean overwrite)
        throws IOException {
        if (!filename.endsWith(".ast")) {
            filename += ".ast";
        }

        File root = Files.createTempDir();

        FileOutputStream out = new FileOutputStream(new File(root, "script.py"));
        for (AsterCommand c: cmds) {
            out.write(c.toScript().getBytes());
            c.saveImage(root.getAbsolutePath());
        }
        out.close();

        try {
            File outfile = new File(filename);
            if (outfile.exists()) {
                if (overwrite) {
                    outfile.delete();
                } else {
                    throw new IOException(String.format("File `%s' exists", filename));
                }
            }
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
            zipDir(root, root.getAbsolutePath(), zos);
            zos.close();
            deleteDir(root);
        } catch(FileNotFoundException e) {
            throw new IOException(e);
        }

        mFile = new File(filename);
    }

    public AsterCommand[] load(String zipfile)
        throws IOException {
        String filename = "script.py";
        String rootpath = mCwd.getAbsolutePath();
        unzipDir(zipfile, rootpath);

        mPathStack.push((new File(zipfile)).getParent());

        cdCwd();
        ArrayList<AsterCommand> cmds = new ArrayList<AsterCommand>();

        try {
            FileInputStream ist = new FileInputStream(new File(mCwd, filename));

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

            for (String s: data.split("\n")) {
                if (s.startsWith("drag")) {
                    cmds.add(new Drag(rootpath, s.substring(4, s.length())));
                } else if (s.startsWith("touch")) {
                    cmds.add(new Touch(rootpath, s.substring(5, s.length())));
                } else if (s.startsWith("press")) {
                    cmds.add(new Press(s.substring(5, s.length())));
                } else if (s.startsWith("type")) {
                    cmds.add(new Type(s.substring(4, s.length())));
                } else if (s.startsWith("wait")) {
                    cmds.add(new Wait(rootpath, s.substring(4, s.length())));
                } else if (s.startsWith("recall")) {
                    cmds.add(new Recall(s.substring(6, s.length())));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        mFile = new File(zipfile);
        AsterCommand[] cmd_array = new AsterCommand[cmds.size()];

        return cmds.toArray(cmd_array);
    }
}
