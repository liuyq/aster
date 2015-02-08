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
 * Authored by Julian Chu <walkingice@0xlab.org>
 *             Kan-Ru Chen <kanru@0xlab.org>
 *             Wei-Ning Huang <azhuang@0xlab.org>
 */

package org.zeroxlab.aster;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.zeroxlab.aster.cmds.AsterCommand;

/*
 * The class used to show the command selection dialog 
 * when press the plus button
 */
public class CmdSelector {

    /*
     * The real method used to show the command selection dialog when press the
     * plus button. Called by mouseClicked in AsterMainPanel.java
     */
    @SuppressWarnings("unchecked")
    public static AsterCommand selectCommand(Component parent) {
        String s = (String) JOptionPane.showInputDialog(parent,
        // Tip message
                "Select next command",
                // dialog title
                "New Command", JOptionPane.PLAIN_MESSAGE, null,
                // array will be shown in the selectbox
                AsterCommand.getSupportedcommands().keySet().toArray(),

                null);
        try {
            if (s != null) {
                // TODO: need to pass the rootPath for the AsterCommand instance
                return (AsterCommand) AsterCommand.getSupportedcommands()
                        .get(s).getConstructor().newInstance();
            }
        } catch (Exception e) {
            System.out.println("Warning: Class cannot be instantiated");
            e.printStackTrace();
        }
        return null;
    }
}
