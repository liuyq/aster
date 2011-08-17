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

import java.awt.image.BufferedImage;

import javax.script.SimpleBindings;

public class Recall extends AsterCommand {
    private String mScript;

    public Recall() {
        mScript = new String();
        mOps = new AsterOperation[1];
        //mOps[0] = AsterWorkspace.getOpRecall();
    }

    public Recall(SimpleBindings settings) {
        fillSettings(settings);
        mOps = new AsterOperation[1];
        //mOps[0] = AsterWorkspace.getOpRecall();
    }

    public Recall(String argline) throws IllegalArgumentException {
        String[] args = splitArgs(argline);

        if (args.length == 1) {
            mScript = args[0];
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        //mOps[0] = AsterWorkspace.getOpRecall();
    }

    @Override
    public String getName() {
        return "Recall";
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Recall");
        settings.put("Script", mScript);
        return settings;
    }

    @Override
    public void fillSettings(SimpleBindings settings) {
        if (settings.containsKey("Script")) {
            mScript = (String)settings.get("Script");
        }
    }

    @Override
    protected String toScript() {
        return new String();
    }

    static protected String[] getKeys() {
        String[] keys = {
        };
        return keys;
    }
}
