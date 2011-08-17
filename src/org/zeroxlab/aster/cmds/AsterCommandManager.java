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

package org.zeroxlab.aster;

import org.zeroxlab.wookieerunner.WookieeAPI;
import org.zeroxlab.wookieerunner.WookieeRunner;
import org.zeroxlab.wookieerunner.ScriptRunner;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;

import java.util.ArrayList;

import com.google.common.io.Files;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.python.core.PyException;

public class AsterCommandManager {

    private static File mCwd;
    private static ChimpChat mChimpChat;
    private static IChimpDevice mImpl;
    private static ScriptRunner mScriptRunner;

    static private void zipDir(File prefix, String dir, ZipOutputStream zos)
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

    static private void unzipDir(String zipfile, String prefix)
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

    static private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    static public void connect() {
        mChimpChat = ChimpChat.getInstance();
        WookieeRunner.setChimpChat(mChimpChat);

        String wookieeRunnerPath =
            System.getProperty("org.zeroxlab.wookieerunner.bindir") +
            File.separator + "wookieerunner";
        mScriptRunner = ScriptRunner.newInstance(null, null, wookieeRunnerPath);
        AsterCommand.setScriptRunner(mScriptRunner);

        // Import WookieeRunnerWrapper
        mScriptRunner.runStringLocal("import os, sys");
        mScriptRunner.runStringLocal("sys.path.insert(0, os.getcwd())");
        mScriptRunner.runStringLocal("from WookieeRunnerWrapper import *");

        // Connect to the device and get IChimpDevice
        mScriptRunner.runStringLocal("connect()");
        mImpl = WookieeRunner.getLastChimpDevice();
    }

    static public IChimpImage takeSnapshot() {
        return mImpl.takeSnapshot();
    }

    static public void run(String astfile)
        throws IOException {
        AsterCommand[] cmds = AsterCommandManager.load(astfile);

        AsterCommandManager.connect();
        System.setProperty("user.dir", mCwd.getAbsolutePath());
        System.out.printf("Staring command execution...\n");
        try {
            for (AsterCommand c: cmds) {
                System.err.println(c.toScript());
                c.execute();
            }
        } catch (PyException e) {
            System.out.printf("%s\n", e);
        }
    }
    
    static public void dump(AsterCommand[] cmds, String filename)
        throws IOException {
        if (!filename.endsWith(".ast"))
            filename += ".ast";

        File root = Files.createTempDir();

        FileOutputStream out = new FileOutputStream(new File(root, "script.py"));
        for (AsterCommand c: cmds) {
            out.write(c.toScript().getBytes());
            c.saveImage(root.getAbsolutePath());
        }
        out.close();

        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
            zipDir(root, root.getAbsolutePath(), zos);
            zos.close();
            deleteDir(root);
        } catch(FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    static public AsterCommand[] load(String zipfile)
        throws IOException {
        String filename = "script.py";
        mCwd = Files.createTempDir();
        String rootpath = mCwd.getAbsolutePath();
        unzipDir(zipfile, rootpath);

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
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        AsterCommand[] cmd_array = new AsterCommand[cmds.size()];
        return cmds.toArray(cmd_array);
    }
}
