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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class JStatusBar extends JPanel {
    JLabel statusBar;
    JTextArea miniBuf;

    public JStatusBar() {
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setLayout(new BorderLayout(0, 0));

        statusBar = new JLabel();
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        statusBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        miniBuf = new JTextArea();
        miniBuf.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        miniBuf.setEditable(false);
        miniBuf.setLineWrap(true);
        miniBuf.setRows(5);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setView(miniBuf);
        add(statusBar, BorderLayout.PAGE_START);
        add(scrollPane, BorderLayout.PAGE_END);

        statusBar.setText(" ");
        miniBuf.setText("");
    }

    public void message(String msg) {
        miniBuf.append("\n" + msg);
    }

    public void setStatus(String msg) {
        statusBar.setText(msg + " ");
    }
}
