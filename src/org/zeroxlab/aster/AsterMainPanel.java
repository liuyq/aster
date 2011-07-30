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

import org.zeroxlab.aster.cmds.*;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.IChimpDevice;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class AsterMainPanel extends JPanel {

    private ImageView mImageView;

    private ChimpChat mChimpChat;
    private IChimpDevice mDevice;
    private AsterCommand[] mCmds;

    private UpdateScreen mUpdateScreen;

    public AsterMainPanel() {
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	setLayout(gridbag);
	c.fill = GridBagConstraints.BOTH;
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 3;
	c.gridheight = 3;
	c.weightx = 0.5;
	c.weighty = 0.5;
	mImageView = new ImageView();
	add(mImageView, c);

        mCmds = new AsterCommand[14];
        generateCmds(mCmds);
        JList myList = new JList(mCmds);
	myList.setCellRenderer(new AsterCommandCellRenderer());
	myList.setFixedCellHeight(50);
	myList.setFixedCellWidth(200);
	JScrollPane scrollPane = new JScrollPane();
	scrollPane.getViewport().setView(myList);
	scrollPane.setMinimumSize(new Dimension(250, 100));

        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.weightx = 0;
        c.weighty = 0;
        add(scrollPane, c);


	Map<String, String> options = new TreeMap<String, String>();
	options.put("backend", "adb");
	mChimpChat = ChimpChat.getInstance(options);
	mDevice = mChimpChat.waitForConnection();

        mUpdateScreen = new UpdateScreen();
        Thread thread = new Thread(mUpdateScreen);
        thread.start();
    }

    private void generateCmds(AsterCommand cmds[]) {
        for (int i = 0; i < cmds.length; i++) {
	    cmds[i] = new Click(new Point(i, 2));
        }
    }

    class UpdateScreen implements Runnable {

        private boolean mKeepWalking = true;

        public void finish() {
            mKeepWalking = false;
        }

        public void run() {
            while(mKeepWalking) {
                updateScreen();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.err.println("Update Screen thread is interrupted");
                    e.printStackTrace();
                }
            }
        }

        private void updateScreen() {
            IChimpImage snapshot = mDevice.takeSnapshot();
            mImageView.setImage(snapshot.createBufferedImage());
            mImageView.repaint(mImageView.getBounds());
        }
    }
}
