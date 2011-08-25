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

import org.zeroxlab.aster.AsterWorkspace.FillListener;
import org.zeroxlab.aster.CmdConnection.SnapshotDrawer;
import org.zeroxlab.aster.cmds.AsterCommand;
import org.zeroxlab.aster.cmds.AsterCommand.CommandExecutionListener;
import org.zeroxlab.aster.cmds.AsterCommand.ExecutionResult;
import org.zeroxlab.aster.cmds.AsterCommandManager;
import org.zeroxlab.aster.cmds.Press;
import org.zeroxlab.aster.cmds.Recall;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpSelectKey;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.script.SimpleBindings;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class AsterMainPanel extends JPanel {

    static JStatusBar mStatus = new JStatusBar();

    private final static String RECALL = "recall.ast";
    private static String sRecall = RECALL;
    private static ImageIcon logoIcon = null;

    static {
        try {
            InputStream stream = Class.class.getResourceAsStream("/logo.png");
            BufferedImage img = ImageIO.read(stream);
            stream.close();
            logoIcon = new ImageIcon(img);
        } catch (IOException e) {
        }
    }

    public static void status(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mStatus.setStatus(msg);
                }
            });
    }

    public static void message(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mStatus.message(msg);
                }
            });
    }

    public final static int MENU_ROTATE = 1;
    public final static int MENU_NOT_ROTATE = 2;

    private enum ExecutionState { NORMAL, EXECUTION }

    private AsterWorkspace mWorkspace;
    private JActionList mActionList;
    private AsterCommandManager mCmdManager;

    private CmdConnection mCmdConn;

    private MyListener mCmdFillListener;

    public AsterMainPanel(AsterCommandManager cmdMgr,
                          CmdConnection conn) {
        mCmdManager = cmdMgr;
        mCmdConn = conn;

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
                @Override
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
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        add(ActionDashboard.getInstance(), c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        add(mStatus, c);

        setPreferredSize(new Dimension(800, 600));

        mWorkspace.addRotationListener(mCmdConn);
        ActionExecutor executor = new ActionExecutor();
        ActionDashboard.getInstance().setListener(executor);
    }

    public SnapshotDrawer getSnapshotDrawer() {
        return mWorkspace.getInstance();
    }

    public JMenuBar createMenuBar() {
        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menu.add(fileMenu);

        JMenuItem newItem = new JMenuItem();
        newItem.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    mActionList.getModel().clear();
                }
            });
        newItem.setText("New File");
        newItem.setMnemonic(KeyEvent.VK_N);
        fileMenu.add(newItem);

        // Open
        JMenuItem openItem = new JMenuItem();
        openItem.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    try {
                        final JFileChooser fc = new JFileChooser();
                        int returnVal = fc.showOpenDialog(AsterMainPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            AsterCommand[] cmds = mCmdManager.load(file.getAbsolutePath());
                            mActionList.getModel().disableChangeListener();
                            mActionList.getModel().clear();
                            mActionList.getModel().setRecall(cmds[0]);
                            for (int i = 1; i < cmds.length; i++) {
                                mActionList.getModel().pushCmd(cmds[i]);
                            }
                            mActionList.getModel().enableChangeListener();
                            mActionList.getModel().trigger();
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
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (mCmdManager.getSaved()) {
                        try {
                            mCmdManager.dump(mActionList.getModel().toArray(),
                                             mCmdManager.getFile().getAbsolutePath(),
                                             true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        final JFileChooser fc = new JFileChooser();
                        int returnVal = fc.showSaveDialog(AsterMainPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                mCmdManager.dump(mActionList.getModel().toArray(),
                                    file.getAbsolutePath(), false);
                            } catch (IOException e) {
                                JOptionPane pane = new JOptionPane(
                                    "File exists! Do you want to overwrite?");
                                Object[] options = new String[] { "Yes", "No" };
                                pane.setOptions(options);
                                JDialog dialog = pane.createDialog(new JFrame(), "Confirm");
                                dialog.show();
                                Object obj = pane.getValue();
                                if (options[0].equals(obj)) {
                                    try {
                                        mCmdManager.dump(mActionList.getModel().toArray(),
                                                file.getAbsolutePath(), true);
                                    } catch (IOException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            });
        saveItem.setText("Save");
        saveItem.setMnemonic(KeyEvent.VK_S);
        fileMenu.add(saveItem);

        // Save As
        JMenuItem saveAsItem = new JMenuItem();
        saveAsItem.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    final JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showSaveDialog(AsterMainPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            mCmdManager.dump(mActionList.getModel().toArray(),
                                file.getAbsolutePath(), false);
                        } catch (IOException e) {
                            JOptionPane pane = new JOptionPane(
                                "File exists! Do you want to overwrite?");
                            Object[] options = new String[] { "Yes", "No" };
                            pane.setOptions(options);
                            JDialog dialog = pane.createDialog(new JFrame(), "Confirm");
                            dialog.show();
                            Object obj = pane.getValue();
                            if (options[0].equals(obj)) {
                                try {
                                    mCmdManager.dump(mActionList.getModel().toArray(),
                                            file.getAbsolutePath(), true);
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        saveAsItem.setText("Save As...");
        saveAsItem.setMnemonic(KeyEvent.VK_A);
        fileMenu.add(saveAsItem);

        // Recall
        JMenuItem recallItem = new JMenuItem();
        recallItem.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                String input = JOptionPane.showInputDialog(
                    null
                    ,"Input filename for recall, such as: " + RECALL
                    , sRecall);
                if (input != null) {
                    sRecall = input;
                }
                System.out.println("use recall:" + sRecall);
                resetRecall(sRecall);
            }
        });
        recallItem.setText("Set Recall...");
        recallItem.setMnemonic(KeyEvent.VK_R);
        fileMenu.add(recallItem);

        // Quit
        JMenuItem quitItem = new JMenuItem();
        quitItem.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    System.exit(0);
                }
            });
        quitItem.setText("Quit");
        quitItem.setMnemonic(KeyEvent.VK_Q);
        fileMenu.add(quitItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        // About
        JMenuItem aboutItem = new JMenuItem();
        aboutItem.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                JOptionPane.showMessageDialog(null,
                                              new AboutMsg(),
                                              "About",
                                              JOptionPane.INFORMATION_MESSAGE,
                                              logoIcon);
            }
        });
        aboutItem.setText("About");
        aboutItem.setMnemonic(KeyEvent.VK_A);
        helpMenu.add(aboutItem);
        menu.add(helpMenu);
        return menu;
    }

    private void resetRecall(String fileName) {
        AsterCommand oldRecall = mActionList.getModel().getRecall();
        AsterCommand newRecall = new Recall();
        SimpleBindings settings = oldRecall.getSettings();
        try {
            settings.put("Script", fileName);
            newRecall.fillSettings(settings);
            mActionList.getModel().setRecall(newRecall);
        } catch (IOException e) {
            e.printStackTrace();
            mActionList.getModel().setRecall(oldRecall);
        }
    }

    class AboutMsg {
        public String toString() {
            String msg = "";
            msg += "Android System Testing Environment and Runtime\n";
            msg += "Designed by 0xLab\n\n";
            msg += "Aster is a testing environment and\n";
            msg += "test-driven development runtime for Android. ";
            return msg;
        }
    }

    class MyListener implements FillListener {
        @Override
        public void commandFilled(AsterCommand whichOne) {
            mActionList.getModel().trigger();
            System.out.println("Complete cmd: " + whichOne.getName());
            mCmdConn.runCommand(whichOne);
        }
    }

    class MainKeyMonitor implements AsterWorkspace.MainKeyListener {
        @Override
        public void onClickHome() {
            AsterOperation op = new OpSelectKey("KEYCODE_HOME");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
        @Override
        public void onClickMenu() {
            AsterOperation op = new OpSelectKey("KEYCODE_MENU");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
        @Override
        public void onClickBack() {
            AsterOperation op = new OpSelectKey("KEYCODE_BACK");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
        @Override
        public void onClickSearch() {
            AsterOperation op = new OpSelectKey("KEYCODE_SEARCH");
            AsterCommand cmd = new Press(op);
            mCmdConn.runCommand(cmd);
        }
    }

    class ActionExecutor implements ActionDashboard.ClickListener,
                                    CommandExecutionListener {
        AsterCommand[] mList;
        ActionDashboard mDashboard;
        int mIndex; // refer to a command which is going to be executed
                    // but not happen yet
        boolean mInPlaying = false;

        ActionExecutor() {
            mDashboard = ActionDashboard.getInstance();
            reset();
        }

        private void set() {
            ActionListModel model = mActionList.getModel();
            mList = model.toArray();
            mIndex = 0;
            mCmdConn.setListener(this);
        }

        private void reset() {
            if (mList != null) {
                for (AsterCommand cmd : mList) {
                    cmd.setExecuting(false);
                }
                mActionList.getModel().trigger();
                mList = null;
            }
            mIndex = -1;
            mInPlaying = false;
            mCmdConn.setListener(null);
        }

        public void onPlayClicked() {
            mInPlaying = true;
            onStepClicked();
        }

        public void onStepClicked() {
            if (mIndex == -1 || mList == null) {
                set();
            }

            if (mIndex < mList.length) {
                mDashboard.setRunning();
                mCmdConn.runCommand(mList[mIndex]);
                mList[mIndex].setExecuting(true);
                mActionList.getModel().trigger();
                mIndex++;
            } else {
                onStopClicked();
            }
        }

        public void onStopClicked() {
            reset();
            mDashboard.resetButtons();
        }

        public void processResult(ExecutionResult result) {
            if(result.mSuccess && mIndex < mList.length) {
                // process success and is not in the end. (1 for Recall)
                if (mInPlaying) {
                    this.onStepClicked(); // go to next step automatically
                } else {
                    mDashboard.setStep();
                }
            } else {
                if (!result.mSuccess) {
                    if (mIndex == 1 && !mCmdManager.getSaved()) {
                        JOptionPane.showMessageDialog(null,
                              "Since you have set recall script, you need to \n"
                            + "save this script first for Aster to locate the\n"
                            + " recall script.",
                              "Execution failed",
                              JOptionPane.ERROR_MESSAGE);
                    } else {
                        System.out.println("\nFailed\n");
                        JOptionPane.showMessageDialog(null,
                                result.mMessage,
                                "Execution failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                reset();
                mDashboard.resetButtons();
            }
        }
    }
}
