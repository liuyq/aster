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

import java.io.File;
import java.io.IOException;

import javax.script.SimpleBindings;

import org.linaro.utils.LinaroUtils;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpFileChooser;

/**
 * Call for sub script
 * 
 */
public class Call extends AsterCommand {
    private static final String name = "Call";

    public Call() {
        mOps = new AsterOperation[1];
        mOps[0] = new OpFileChooser();
    }

    public Call(String rootPath, String argline)
            throws IllegalArgumentException {
        super.setRootPath(rootPath);
        super.setFilled(true);
        String[] args = splitArgs(argline);

        if (args.length == 1) {
            // Call(script)
            inputFileName = stripQuote(args[0]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = new OpFileChooser();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("FilePath", inputFileName);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings.containsKey("FilePath")) {
            String inputFilePath = (String) settings.get("FilePath");
            inputFileName = new File(inputFilePath).getName();
            LinaroUtils.copyFile(new File(inputFilePath),
                    new File(super.getRootPath(),
                    inputFileName));
        }
    }

    @Override
    public String toScript() {
        return String.format("%s('%s')\n", name, inputFileName);
    }

    @Override
    public void execute() {
        // ExecutionResult result = new ExecutionResult(true, "");
        String scriptPath = new File(super.getRootPath(), inputFileName)
                .getAbsolutePath();
        super.runAsterScript(scriptPath, super.device);
    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }
}
