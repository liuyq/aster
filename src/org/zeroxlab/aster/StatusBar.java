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
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.linaro.utils.Constants;

@SuppressWarnings("serial")
public class StatusBar extends JPanel {
    private static JTextArea miniBuf;
    private static JTextArea logcatBuf;
    private static JTextArea kmsgBuf;
    private static JTextArea xmlLayoutBuf;
    private static StatusBar instance = null;

    private StatusBar() {
        super(new GridLayout(1, 1));
        JTabbedPane tp = new JTabbedPane();
        miniBuf = createTap(tp, "DebugMessage",
                "Show message for debug and check", "");
       
        logcatBuf = createTap(tp, "Logcat",
                "Show logcat message of the device", "");
        kmsgBuf = createTap(tp, "Kmsg", "Show kernel log message", "");
        xmlLayoutBuf = createTap(tp, "XML Layout",
                "Show XML information of UI layout", "");

        // 设置合适的显示尺寸，这个是必须的，因为如果所有的标签都
        // 不指定适合的显示尺寸，系统无法判断初始显示尺寸大小
        // 默认是使用最小化，并且对一个标签设计即可
        // tp.setPreferredSize(new Dimension(500, 500));
        add(tp);
        // 设置窗口过小时，标签的显示策略
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // 设置标签停放的位置，这里设置为左侧停放
        // tp.setTabPlacement(JTabbedPane.LEFT);
    }

    public static StatusBar getInstance() {
        if (instance == null) {
            instance = new StatusBar();
        }
        return instance;
    }

    public void message(String msg) {
        miniBuf.append("\n" + msg);
    }

    public void logcat(String msg) {
        logcatBuf.append("\n" + msg);
    }

    public void kmsg(String msg) {
        kmsgBuf.append("\n" + msg);
    }

    public void dumpXMLLayout(String msg) {
        xmlLayoutBuf.append("\n" + msg);
    }

    private JTextArea createTap(JTabbedPane tabPanel, String title,
            String tipMsg, String initValue) {
        JPanel panel = new JPanel(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        panel.setLayout(new BorderLayout(0, 0));

        JTextArea textArea = new JTextArea();
        textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setRows(Constants.STATUS_BAR_ROWS);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setView(textArea);
        scrollPane.getViewport().setAutoscrolls(true);
        panel.add(scrollPane, BorderLayout.PAGE_END);

        textArea.setText(initValue);

        // ImageIcon ii = createImageIcon("images/middle.gif");
        ImageIcon imageicon = null;
        tabPanel.addTab(title, imageicon, panel, tipMsg);

        // 设置标签的快捷键
        // tabPanel.setMnemonicAt(1, KeyEvent.VK_1);
        return textArea;
    }

    // private ImageIcon createImageIcon(String string) {
    // URL url = JStatusBar.class.getResource(string);
    // if (url == null) {
    // System.out.println("the image " + string + " is not exist!");
    // return null;
    // }
    // return new ImageIcon(url);
    // }
}