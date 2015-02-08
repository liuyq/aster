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
import java.util.Stack;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.linaro.utils.DeviceForAster;

import com.android.monkeyrunner.MonkeyFormatter;
import com.google.common.io.Files;

public class AsterCommandManager {

    // TODO: this variant should be deleted or use a single instance
    // Or use the rootPath variant in the AsterCommand
    private static File mCwd = null;
    private static Stack<String> mPathStack = new Stack<String>();

    // private static ChimpChat mChimpChat;

    public AsterCommandManager() {
        mCwd = Files.createTempDir();
        System.setProperty("user.dir", mCwd.getAbsolutePath());
        replaceAllLogFormatters(MonkeyFormatter.DEFAULT_INSTANCE, Level.SEVERE);
    }

    public static void cdCwd() {
        System.setProperty("user.dir", mCwd.getAbsolutePath());
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

    private void unzipDir(String zipfile, String prefix) throws IOException {
        Enumeration<?> entries;
        ZipFile zipFile = new ZipFile(zipfile);
        byte[] buffer = new byte[4096];
        int len = 0;

        (new File(prefix)).mkdirs();
        entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                (new File(prefix, entry.getName())).mkdir();
                continue;
            }

            InputStream is = zipFile.getInputStream(entry);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(
                    (new File(prefix, entry.getName())).getPath()));
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

    public File findFile(String cwd, String astfile) throws IOException {
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
            throws Exception {
        if (!mPathStack.empty()) {
            try {
                astfile = findFile(mPathStack.peek(), astfile)
                        .getAbsolutePath();
            } catch (NullPointerException e) {
                throw new Exception(
                        String.format("Can not open `%s'.", astfile));
            }
        }

        DeviceForAster device = DeviceForAster.getInstance();

        AsterCommand[] cmds = load(astfile);

        System.out.printf("Staring command execution...\n");
        for (AsterCommand c : cmds) {
            System.err.printf("%s", c.toScript());
            cdCwd();
            // AsterCommand.ExecutionResult result = c
            c.execute(device);
            // if (result.mSuccess != true) {
            // System.err.println(result.mMessage);
            // mPathStack.pop();
            // return result;
            // }
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
        for (AsterCommand c : cmds) {
            out.write(c.toScript().getBytes());
            c.saveImage(root.getAbsolutePath());
            c.saveInputFile(root.getAbsolutePath());
        }
        out.close();

        try {
            File outfile = new File(filename);
            if (outfile.exists()) {
                if (overwrite) {
                    outfile.delete();
                } else {
                    throw new IOException(String.format("File `%s' exists",
                            filename));
                }
            }
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
                    filename));
            zipDir(root, root.getAbsolutePath(), zos);
            zos.close();
            deleteDir(root);
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    public AsterCommand[] load(String zipfile) throws IOException {
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

            for (String line : data.split("\n")) {
                AsterCommand asterCmdInstance = AsterCommand
                        .getAsterCommandSubInstance(rootpath, line);
                if (asterCmdInstance != null) {
                    cmds.add(asterCmdInstance);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        AsterCommand[] cmd_array = new AsterCommand[cmds.size()];
        return cmds.toArray(cmd_array);
    }
}
