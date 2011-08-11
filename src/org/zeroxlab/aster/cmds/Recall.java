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

public abstract class Recall extends AsterCommand {
    private String mScript;

    public Recall(String script) {
        mScript = script;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Script", mScript);
        return settings;
    }

    @Override
    public void fill(SimpleBindings settings) {
        mScript = (String)settings.get("Script");
    }

    @Override
    public String getName() {
        return "Recall";
    }

    @Override
    public AsterOperation[] getOperations() {
        System.out.println("Get Operation");
        AsterOperation[] foo = new AsterOperation[1];
        return foo;
    }

    @Override
    protected String toScript() {
        return new String();
    }

    @Override
    protected String[] getRegex() {
        String[] regexs = { "" };
        return regexs;
    }

    @Override
    protected String getPrefix() {
        return "recall";
    }
}
