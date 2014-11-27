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
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.zeroxlab.aster.CmdConnection.SnapshotDrawer;
import org.zeroxlab.aster.cmds.AsterCommand.ExecutionResult;
import org.zeroxlab.aster.cmds.AsterCommandManager;

public class AsterMain {

    private AsterCommandManager mCmdMgr;
    private CmdConnection mCmdConn;

    public AsterMain() {

    }

    public void startCLI(String scriptPath, String serial) {
        mCmdMgr = new AsterCommandManager(null, null);

        ExecutionResult result = null;
        try {
            result = mCmdMgr.run(scriptPath, serial);
        } catch (IOException e) {
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
        String[] adbTypeKeys = { "LOCAL", "MONKEYRUNNER", "SSH" };
        Object adbType = JOptionPane.showInputDialog(null,
                "Please choose the adb type",
                "Choose the type on how to connect device",
                JOptionPane.QUESTION_MESSAGE, null, adbTypeKeys, "LAVA");
        String serial = null;
        String adbTypeStr = null;
        if (adbType != null) {
            if (adbType.toString().equals("LAVA")) {
                adbTypeStr = "SSH";
                TreeMap<String, String> juno_devices = new TreeMap<String, String>();
                Object lava_device = JOptionPane.showInputDialog(null,
                        "Please select the juno device",
                        "Select the juno device", JOptionPane.QUESTION_MESSAGE,
                        null, juno_devices.keySet().toArray(), "juno-01");

                if (lava_device != null) {
                    serial = juno_devices.get(lava_device.toString());
                }
            } else {
                adbTypeStr = adbType.toString();
                serial = JOptionPane.showInputDialog(null,
                        "Please input the serial number", "");
            }
        }else{
            adbTypeStr = "LOCAL";
        }

        mCmdMgr = new AsterCommandManager(adbTypeStr, serial);
        mCmdConn = new CmdConnection(mCmdMgr);

        ActionListModel model = new DefaultActionListModel();
        /*
         * FIXME: The way to set needed objects before initialize is bad It
         * should be improved to become stable.
         */
        AsterController.setConnection(mCmdConn);
        AsterController.setModel(model);
        AsterController.setCmdMgr(mCmdMgr);
        AsterController.getInstance();

        trySetupLookFeel();
        JFrame f = new JFrame("Aster");// The entire window
        AsterMainPanel p = new AsterMainPanel(mCmdMgr, mCmdConn, model);
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
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            // Set System L&F
            UIManager
                    .setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
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
                String serial = null;
                if (args.length >= 4 && "-serial".equals(args[2])) {
                    serial = args[3];
                }
                if ("-run".equals(args[0])) {
                    aster.startCLI(args[1], serial);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                AsterMain.usage();
            }
        } else {
            aster.startGUI();
        }
    }

}
