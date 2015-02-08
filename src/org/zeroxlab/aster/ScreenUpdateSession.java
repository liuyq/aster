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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;

import org.linaro.utils.Constants.ExecutionState;
import org.linaro.utils.DeviceForAster;
import org.zeroxlab.aster.cmds.AsterCommand;
import org.zeroxlab.aster.cmds.AsterCommandManager;

class ScreenUpdateSession implements Runnable, ItemListener {

    private boolean mKeepWalking = true;
    private AsterCommand mCmd;
    private ExecutionState mState;
    private static boolean mLandscape = false;
    private ICommandExecutionListener mListener = null;

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source instanceof JCheckBox) {
            JCheckBox rotate = (JCheckBox) source;
            mLandscape = rotate.isSelected();
        }
    }

    synchronized private void switchState(ExecutionState state) {
        mState = state;
    }

    /* FIXME: They should be interface but not implementation */
    private SnapshotDrawer mDrawer;

    public void setDrawer(SnapshotDrawer drawer) {
        mDrawer = drawer;
    }

    interface SnapshotDrawer {
        public void setImage(BufferedImage img);
    }

    public void finish() {
        mKeepWalking = false;
    }

    synchronized public void setListener(ICommandExecutionListener listener) {
        mListener = listener;
    }

    synchronized public void runCommand(AsterCommand cmd) {
        mCmd = cmd;
        switchState(ExecutionState.EXECUTION);
    }

    @Override
    public void run() {
        mState = ExecutionState.NORMAL;
        DeviceForAster device = null;
        try {
            device = DeviceForAster.getInstance();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // should not be null here
        AsterMainPanel.message("Connected");

        while (mKeepWalking) {
            if (mState == ExecutionState.NORMAL) {
                try {
                    updateScreen(device);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                // Reset user.dir everytime
                AsterCommandManager.cdCwd();
                String msg = String.format("Executing %s command ...\n",
                        mCmd.getName());
                System.err.printf(msg);
                AsterMainPanel.message(msg);
                System.err.println(mCmd.toScript());
                AsterMainPanel.message(mCmd.toScript());
                mCmd.execute(device);

                switchState(ExecutionState.NORMAL);
                if (mListener != null) {
                    mListener.processResult(null /* result */);
                }
                try {
                    updateScreen(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    private void updateScreen(DeviceForAster device) throws Exception {
        if (mDrawer == null) {
            System.out.println("There is no drawer to update screenshot");
            return;
        }
        if (device == null) {
            System.out.println("There is no device available");
            return;
        }

        BufferedImage image = device.getScreenshotBufferedImage(mLandscape);
        if (image != null) {
            mDrawer.setImage(image);
        }
    }

}
