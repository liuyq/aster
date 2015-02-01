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

import java.io.File;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.linaro.utils.Constants;
import org.linaro.utils.DeviceForAster;
import org.linaro.utils.LinaroUtils;
import org.zeroxlab.aster.cmds.AsterCommand.ExecutionResult;
import org.zeroxlab.aster.cmds.AsterCommandManager;
import org.zeroxlab.aster.cmds.InitAndHome;

public class AsterMain {



    private String getAdbType() {
        Object adbType = JOptionPane.showInputDialog(null,
                "Please choose the adb type",
                "Choose the type on how to connect device",
                JOptionPane.QUESTION_MESSAGE, null, Constants.ADB_TYPES,
                Constants.ADB_TYPE_DEFAULT);
        if (adbType == null || adbType.toString().isEmpty()) {
            return Constants.ADB_TYPE_LOCAL;
        } else {
            return adbType.toString();
        }
    }

    private String getSerial(String adbTypeStr) {
        String serial = null;
        if (adbTypeStr.equals(Constants.ADB_TYPE_LAVA)) {
            adbTypeStr = Constants.ADB_TYPE_SSH;
            TreeMap<String, String> devices = LinaroUtils.getJunoDevices();
            Object device = JOptionPane.showInputDialog(null,
                    "Please select the juno device", "Select the juno device",
                    JOptionPane.QUESTION_MESSAGE, null, devices.keySet()
                            .toArray(),
                    Constants.JUNO_DEVICES[0]);

            if (device != null) {
                serial = devices.get(device.toString());
            }
        } else {
            serial = JOptionPane.showInputDialog(null,
                    "Please input the serial number", "");
        }
        if (serial != null && serial.isEmpty()) {
            serial = null;
        }
        return serial;
    }

    public void startGUI() {
        File screenShot = new File(Constants.SCR_PATH_HOST);
        screenShot.delete();

        trySetupLookFeel();
        String frame_title = "Aster";
        JFrame f = new JFrame(frame_title);// The entire window
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

        String adbTypeStr = getAdbType();
        String serial = getSerial(adbTypeStr);

        if (adbTypeStr != null) {
            frame_title = frame_title + ": type=" + adbTypeStr;
        }
        if (serial != null) {
            frame_title = frame_title + " serial=" + serial;
        }
        f.setTitle(frame_title);

        try {
            DeviceForAster.initialize(adbTypeStr, serial);
            DeviceForAster.getInstance().connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ScreenUpdateSession mCmdConn = new ScreenUpdateSession();
        IActionListContoller model = new ActionListController();
        /*
         * FIXME: The way to set needed objects before initialize is bad It
         * should be improved to become stable.
         */
        PlayStepStopController.setConnection(mCmdConn);
        PlayStepStopController.setModel(model);
//        AsterController.setCmdMgr(mCmdMgr);
        PlayStepStopController.getInstance();

        AsterMainPanel p = new AsterMainPanel(mCmdConn, model);
        f.setContentPane(p);
        f.setJMenuBar(p.createMenuBar());
        f.pack();

        mCmdConn.setDrawer(p.getSnapshotDrawer());
        new Thread(mCmdConn).start();
        p.getActionList().getActionListController()
                .setInitAndHomeCmd(new InitAndHome());

        new Thread(new StatusBarLogcatUpdateSession()).start();
        new Thread(new StatusBarKmsgUpdateSession()).start();
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

    public void startCLI(String scriptPath, String adbType, String serial)
            throws Exception {

        DeviceForAster.initialize(adbType, serial);
        DeviceForAster.getInstance().connect();
        ExecutionResult result = (new AsterCommandManager())
                .runLocal(scriptPath);
        if (result.mSuccess) {
            System.out.println("\nFinished\n");
            System.exit(0);
        } else {
            System.out.println("\nFailed\n");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
        AsterMain aster = new AsterMain();
        if (args.length > 0) {
            try {
                // TODO
                String serial = null;
                String adbType = null;
                if (args.length >= 4 && "-serial".equals(args[2])) {
                    serial = args[3];
                }
                if ("-run".equals(args[0])) {
                    aster.startCLI(args[1], adbType, serial);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                AsterMain.usage();
            }
        } else {
            aster.startGUI();
        }
    }

}
