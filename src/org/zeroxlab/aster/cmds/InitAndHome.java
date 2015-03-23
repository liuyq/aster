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

package org.zeroxlab.aster.cmds;

import java.io.IOException;

import javax.script.SimpleBindings;

import org.zeroxlab.aster.operations.AsterOperation;

/**
 * Do initialization and go to home when the script start
 * 
 * @author liuyq
 * 
 */
public class InitAndHome extends AsterCommand {
    private static final String name = "InitAndHome";
    public InitAndHome() {
        mOps = new AsterOperation[1];
    }

    public InitAndHome(String rootPath, String argline)
            throws IllegalArgumentException {
        super.setRootPath(rootPath);
        super.setFilled(true);

        String[] args = splitArgs(argline);

        if (args.length != 0 && !args[0].isEmpty()) {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", name);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
    }

    @Override
    public void execute() {
        super.device.connect();
        // super.device.executeAdbShell("disablesuspend.sh");
        super.device.executeAdbShell("sleep", "2");
        super.device.press("KEYCODE_MENU");
        super.device.press("KEYCODE_BACK");
        super.device.press("KEYCODE_HOME");
    }

    @Override
    public String toScript() {
        return String.format("%s()\n", name);
    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }
}
