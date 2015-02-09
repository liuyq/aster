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
 * Authored by Wei-Ning Huang <azhuang@0xlab.org>
 */

package org.zeroxlab.aster.cmds;

import java.io.IOException;

import javax.script.SimpleBindings;

import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpGetInput;

public class WaitTimeout extends AsterCommand {
    private static final String name = "WaitTimeout";
    String tipMsg = "Please input the time value(in seconds) you want to wait:";
    String mTimeout = "30";

    public WaitTimeout() {
        super.mOps = new AsterOperation[1];
        super.mOps[0] = new OpGetInput(tipMsg, mTimeout);
    }

    public WaitTimeout(String rootPath, String argline)
            throws IllegalArgumentException {
        super.setRootPath(rootPath);
        super.setFilled(true);
        String[] args = splitArgs(argline);
        if (args.length == 1) {
            // WaitTimeout(time)
            mTimeout = args[0];
        } else {
            throw new IllegalArgumentException();
        }
        super.mOps = new AsterOperation[1];
        super.mOps[0] = new OpGetInput(tipMsg, mTimeout);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", name);
        settings.put("Text", mTimeout);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings == null) {
            return;
        }

        if (settings.containsKey("Text")) {
            mTimeout = (String) settings.get("Text");
        }
    }

    @Override
    public String toScript() {
        return String.format("%s(%s)\n", name, mTimeout);
    }

    @Override
    public void execute() {
        device.executeAdbShell("sleep", mTimeout);
    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }
}
