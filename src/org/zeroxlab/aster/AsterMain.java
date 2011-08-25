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

package org.zeroxlab.aster;

import java.io.IOException;
import java.lang.ArrayIndexOutOfBoundsException;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.zeroxlab.aster.CmdConnection.SnapshotDrawer;
import org.zeroxlab.aster.cmds.AsterCommandManager;
import org.zeroxlab.aster.cmds.AsterCommand.ExecutionResult;

public class AsterMain {

    private AsterCommandManager mCmdMgr;
    private CmdConnection mCmdConn;

    public AsterMain() {
        mCmdMgr = new AsterCommandManager();

    }

    public void startCLI(String script) {
        ExecutionResult result = null;
        try {
            result = mCmdMgr.run(script);
        } catch(IOException e) {
            System.err.printf(e.toString());
            System.exit(1);
        }
        if (result.mSuccess) {
            System.out.println("\nFinished\n");
            System.exit(0);
        } else {
            System.out.println("\nFailed\n");
            System.exit(1);
        }
    }

    public void startGUI() {
        mCmdConn = new CmdConnection(mCmdMgr);

        trySetupLookFeel();
        JFrame f = new JFrame("Aster");
        AsterMainPanel p = new AsterMainPanel(mCmdMgr, mCmdConn);
        f.setContentPane(p);
        f.setJMenuBar(p.createMenuBar());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);

        SnapshotDrawer drawer = p.getSnapshotDrawer();
        mCmdConn.setDrawer(drawer);
        Thread thread = new Thread(mCmdConn);
        thread.start();
    }

    private static void trySetupLookFeel() {
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }
    }

    private static void usage() {
        System.out.printf("Usage: aster [-run TEST.ast]\n\n");
    }

    public static void main(String[] args) {
        AsterMain aster = new AsterMain();
        if (args.length > 0) {
            try {
                if ("-run".equals(args[0])) {
                    aster.startCLI(args[1]);
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                aster.usage();
            }
        } else {
            aster.startGUI();
        }
    }

}
