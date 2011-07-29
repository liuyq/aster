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
import org.zeroxlab.wookieerunner.WookieeAPI;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpImage;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class AsterMainPanel extends JPanel {

    ImageView mImageView;

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

	c.gridx = 4;
	c.gridy = 0;
	c.gridwidth = 1;
	c.gridheight = 3;
	c.weightx = 0;
	c.weighty = 0;

	AsterCommand[] cmds = {new Click(new Point(1, 2)),
			       new Click(new Point(2, 2)),
			       new Click(new Point(3, 2)),
			       new Click(new Point(4, 2)),
			       new Click(new Point(5, 2)),
			       new Click(new Point(6, 2)),
			       new Click(new Point(7, 2)),
			       new Click(new Point(1, 2)),
			       new Click(new Point(1, 2)),
			       new Click(new Point(1, 2)),
			       new Click(new Point(1, 2)),
			       new Click(new Point(1, 2)),
			       new Click(new Point(1, 2)),
			       new Click(new Point(1, 2)),
			       new Click(new Point(1, 2))};
	JList myList = new JList(cmds);
	myList.setCellRenderer(new AsterCommandCellRenderer());
	myList.setFixedCellHeight(50);
	myList.setFixedCellWidth(200);
	JScrollPane scrollPane = new JScrollPane();
	scrollPane.getViewport().setView(myList);
	scrollPane.setMinimumSize(new Dimension(250, 100));
	add(scrollPane, c);


	Map<String, String> options = new TreeMap<String, String>();
	options.put("backend", "adb");
	mChimpChat = ChimpChat.getInstance(options);
        mImpl = new WookieeAPI(mChimpChat.waitForConnection());
	updateScreen();
	timer.start();
    }

    ChimpChat mChimpChat;
    WookieeAPI mImpl;

    private final javax.swing.Timer timer =
	new javax.swing.Timer(100, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            updateScreen();
        }
    });

    private void updateScreen() {
        IChimpImage snapshot = mImpl.takeSnapshot();
        mImageView.setImage(snapshot.createBufferedImage());
	mImageView.repaint(mImageView.getBounds());
    }
}
