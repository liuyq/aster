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
 *             Wei-Ning Huang <azhuang@0xlab.org>
 *             Julian Chu <walkingice@0xlab.org>
 */

package org.zeroxlab.aster;

import org.zeroxlab.aster.cmds.AsterCommand;
import org.zeroxlab.aster.cmds.AsterCommand.CommandExecutionListener;
import org.zeroxlab.aster.cmds.AsterCommandManager;
import org.zeroxlab.wookieerunner.ImageUtils;

import com.android.chimpchat.core.IChimpImage;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import javax.swing.JCheckBox;

class CmdConnection implements Runnable, ItemListener {

    private boolean mKeepWalking = true;
    private AsterCommand mCmd;
    private ExecutionState mState;
    private boolean mLandscape = false;
    private CommandExecutionListener mListener = null;

    /* FIXME: They should be interface but not implementation */
    private AsterCommandManager mCmdManager;
    private SnapshotDrawer      mDrawer;

    public enum ScreenOrientation {
        PORTRAIT,
        LANDSCAPE,
    }

    private enum ExecutionState {
        NORMAL,
        EXECUTION,
    }

    public CmdConnection(AsterCommandManager mgr) {
        mCmdManager = mgr;
    }

    public void setDrawer(SnapshotDrawer drawer) {
        mDrawer = drawer;
    }

    public void finish() {
        mKeepWalking = false;
    }

    synchronized public void setListener(CommandExecutionListener listener) {
        mListener = listener;
    }

    synchronized public void runCommand(AsterCommand cmd) {
        mCmd = cmd;
        switchState(ExecutionState.EXECUTION);
    }

    public void onScreenRotate(ScreenOrientation orientation) {
        if (orientation == ScreenOrientation.LANDSCAPE) {
            mLandscape = true;
        } else {
            mLandscape = false;
        }
    }

    synchronized private void switchState(ExecutionState state) {
        mState = state;
    }

    @Override
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
                mCmdManager.cdCwd();
                String msg = String.format("Executing %s command ...\n",
                        mCmd.getName());
                System.err.printf(msg);
                AsterMainPanel.message(msg);
                System.err.println(mCmd.toScript());
                AsterCommand.ExecutionResult result = mCmd.execute();
                switchState(ExecutionState.NORMAL);
                if (mListener != null) {
                    mListener.processResult(result);
                }
                updateScreen();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                String msg = "Update Screen thread is interrupted";
                System.err.println(msg);
                AsterMainPanel.message(msg);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source instanceof JCheckBox) {
            JCheckBox rotate = (JCheckBox) source;
            if (rotate.isSelected()) {
                onScreenRotate(ScreenOrientation.LANDSCAPE);
            } else {
                onScreenRotate(ScreenOrientation.PORTRAIT);
            }
        }
    }

    private void updateScreen() {
        if (mDrawer == null) {
            System.out.println("There is no drawer to update screenshot");
            return;
        }

        IChimpImage snapshot = mCmdManager.takeSnapshot();
        BufferedImage image = snapshot.createBufferedImage();
        if (mLandscape) {
            // By default, a NOT-ROTATED-Image should be Portrait
            image = ImageUtils.rotate(image);
        }
        mDrawer.setImage(image);
    }

    interface SnapshotDrawer {
        public void setImage(BufferedImage img);
    }
}
