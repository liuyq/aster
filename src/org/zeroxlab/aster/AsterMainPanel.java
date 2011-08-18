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

import org.zeroxlab.aster.AsterCommand;
import org.zeroxlab.aster.AsterWorkspace.FillListener;
import org.zeroxlab.wookieerunner.ImageUtils;

import com.android.chimpchat.core.IChimpImage;

import com.google.common.io.Files;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.script.SimpleBindings;

public class AsterMainPanel extends JPanel {

    static JStatusBar mStatus = new JStatusBar();

    public static void status(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    mStatus.setStatus(msg);
                }
            });
    }

    public static void message(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    mStatus.message(msg);
                }
            });
    }

    public final static int MENU_ROTATE = 1;
    public final static int MENU_NOT_ROTATE = 2;
    private static boolean sRotate;

    private enum ExecutionState { NORMAL, EXECUTION }

    private AsterWorkspace mWorkspace;
    private JActionList mActionList;
    private AsterCommandManager mCmdManager;

    private CmdConn mCmdConn;

    private MyListener mCmdFillListener;
    private RotationStateListener mRSListener;

    private File mCwd;

    public AsterMainPanel() {
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
        mCmdFillListener = new MyListener();

        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.weightx = 0;
        c.weighty = 0;
        mActionList = new JActionList();
        JScrollPane scrollPane = new JScrollPane();
        /*
         * TODO: FIXME:
         * Always show the scroll bar, otherwise when the scroll bar
         * was displayed the scrollPane will resize incorretly.
         */
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setView(mActionList);
        add(scrollPane, c);
        MainKeyMonitor mkmonitor = new MainKeyMonitor();
        mWorkspace = AsterWorkspace.getInstance();
        mWorkspace.setFillListener(mCmdFillListener);
        mWorkspace.setMainKeyListener(mkmonitor);
        mActionList.getModel().setRecall(new Recall());
        mActionList.getModel().addChangeListener(mWorkspace);
        mActionList.addNewActionListener(new MouseAdapter () {
                public void mouseClicked(MouseEvent e) {
                    AsterCommand cmd = CmdSelector.selectCommand((Component)e.getSource());
                    if (cmd != null) {
                        mActionList.getModel().pushCmd(cmd);
                    }
                }
            });

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 3;
        c.weightx = 0.5;
        c.weighty = 0.5;
        add(mWorkspace, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        add(mStatus, c);

        setPreferredSize(new Dimension(800, 600));

        // Initialize command Manager
        mCmdManager = new AsterCommandManager();

        // Set working dir and cd to it
        mCwd = Files.createTempDir();
        System.setProperty("user.dir", mCwd.getAbsolutePath());

        mCmdConn = new CmdConn();
        Thread thread = new Thread(mCmdConn);
        thread.start();
    }

    public JMenuBar createMenuBar() {
        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menu.add(fileMenu);

        // Open
        JMenuItem openItem = new JMenuItem();
        openItem.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent ev) {
                    try {
                        final JFileChooser fc = new JFileChooser();
                        int returnVal = fc.showOpenDialog(AsterMainPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            AsterCommand[] cmds = mCmdManager.load(file.getAbsolutePath());
                            mActionList.getModel().clear();
                            mActionList.getModel().setRecall(cmds[0]);
                            for (int i = 1; i < cmds.length; i++) {
                                mActionList.getModel().pushCmd(cmds[i]);
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            });
        openItem.setText("Open...");
        openItem.setMnemonic(KeyEvent.VK_O);
        fileMenu.add(openItem);

        // Save
        JMenuItem saveItem = new JMenuItem();
        saveItem.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent ev) {
                    try {
                        final JFileChooser fc = new JFileChooser();
                        int returnVal = fc.showSaveDialog(AsterMainPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            mCmdManager.dump(mActionList.getModel().toArray(),
                                                     file.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        System.err.println(e.toString());
                    }
                }
            });
        saveItem.setText("Save...");
        saveItem.setMnemonic(KeyEvent.VK_S);
        fileMenu.add(saveItem);

        // Quit
        JMenuItem quitItem = new JMenuItem();
        quitItem.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent ev) {
                    System.exit(0);
                }
            });
        quitItem.setText("Quit");
        quitItem.setMnemonic(KeyEvent.VK_Q);
        fileMenu.add(quitItem);

        JMenu viewMenu = new JMenu("View");
        ButtonGroup group = new ButtonGroup();
        RotationMenuListener rListener = new RotationMenuListener();
        JRadioButtonMenuItem rb;
        rb = new JRadioButtonMenuItem("Rotate Image");
        rb.addActionListener(rListener);
        rb.setMnemonic(MENU_ROTATE);
        group.add(rb);
        viewMenu.add(rb);

        rb = new JRadioButtonMenuItem("Not Rotate Image");
        rb.addActionListener(rListener);
        rb.setMnemonic(MENU_NOT_ROTATE);
        rb.setSelected(true);
        group.add(rb);
        viewMenu.add(rb);
        menu.add(viewMenu);
        return menu;
    }

    public boolean needRotate() {
        return sRotate;
    }

    public void setRotationStateListener(RotationStateListener l) {
        mRSListener = l;
    }

    interface RotationStateListener {
        public void rotationUpdated(boolean needRotate);
    }

    class RotationMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)e.getSource();
                boolean state = (button.getMnemonic() == MENU_ROTATE);
                if (state != sRotate) {
                    if (mRSListener!= null) {
                        mRSListener.rotationUpdated(state);
                    }
                }

                sRotate = state;
            }
        }
    }

    class MyListener implements FillListener {
        public void commandFilled(AsterCommand whichOne) {
            mActionList.getModel().trigger();
            System.out.println("Complete cmd: " + whichOne.getName());
            mCmdConn.runCommand(whichOne);
        }
    }

    class MainKeyMonitor implements AsterWorkspace.MainKeyListener {
        public void onClickHome() {
            AsterOperation op = new OpSelectKey("KEYCODE_HOME");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
        public void onClickMenu() {
            AsterOperation op = new OpSelectKey("KEYCODE_MENU");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
        public void onClickBack() {
            AsterOperation op = new OpSelectKey("KEYCODE_BACK");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
        public void onClickSearch() {
            AsterOperation op = new OpSelectKey("KEYCODE_SEARCH");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
    }

    class CmdConn implements Runnable {

        private boolean mKeepWalking = true;
        private AsterCommand mCmd;
        private ExecutionState mState;
        private AsterCommand.CommandExecutionLister mListener = null;

        public void finish() {
            mKeepWalking = false;
        }

        synchronized public void setListener(AsterCommand.CommandExecutionLister listener) {
            mListener = listener;
        }

        synchronized public void runCommand(AsterCommand cmd) {
            mCmd = cmd;
            switchState(ExecutionState.EXECUTION);
        }

        synchronized private void switchState(ExecutionState state) {
            mState = state;
        }

        public void run() {
            mState = ExecutionState.NORMAL;
            AsterMainPanel.message("Connecting to device...");
            mCmdManager.connect();
            AsterMainPanel.message("Connected");

            while (mKeepWalking) {
                if (mState == ExecutionState.NORMAL) {
                    updateScreen();
                } else {
                    // Reset user.dir everytime
                    System.setProperty("user.dir", mCwd.getAbsolutePath());
                    String msg = String.format("Executing %s command ...\n",
                                               mCmd.getName());
                    System.err.printf(msg);
                    AsterMainPanel.message(msg);
                    System.err.println(mCmd.toScript());
                    AsterCommand.ExecutionResult result = mCmd.execute();
                    if (mListener != null) {
                        mListener.processResult(result);
                    }
                    switchState(ExecutionState.NORMAL);
                    updateScreen();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.err.println("Update Screen thread is interrupted");
                    AsterMainPanel.message("Update Screen thread is interrupted");
                    e.printStackTrace();
                }
            }
        }

        private void updateScreen() {
            IChimpImage snapshot = mCmdManager.takeSnapshot();
            BufferedImage image = snapshot.createBufferedImage();
            if (true)
                image = ImageUtils.rotate(image);
            mWorkspace.setImage(image);
            mWorkspace.repaint(mWorkspace.getBounds());
        }
    }
}
