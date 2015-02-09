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

import java.io.File;
import java.io.IOException;

import javax.script.SimpleBindings;

import org.linaro.utils.LinaroUtils;
import org.zeroxlab.aster.operations.AsterOperation;

public class Screencap extends AsterCommand {
    private static final String name = "Screencap";

    public Screencap() {
        mOps = new AsterOperation[0];
    }

    public Screencap(String rootPath, String argline)
            throws IllegalArgumentException {
        super.setRootPath(rootPath);
        super.setFilled(true);
        String[] args = splitArgs(argline);
        if (args.length != 0 && !args[0].isEmpty()) {
            // Screencap()
            throw new IllegalArgumentException(
                    "Screencap does not support any parameter");
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
        // Nothing to do
    }

    @Override
    public String toScript() {
        return String.format("%s()\n", name);
    }

    @Override
    public void execute() {
        File target = new File(getRootPath(), String.format(
                "screenshot_%d.png", mSerial));
        LinaroUtils.copyFile(new File(device.getScreenShotPath()), target);
        System.out.println("Catched screenshot:" + target.getAbsolutePath());

    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }
}
