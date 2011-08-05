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

import org.zeroxlab.aster.AsterCommand;

import java.awt.image.BufferedImage;

public abstract class Recall extends AsterCommand {
    private String mScript;

    public Recall(String script) {
        mScript = script;
    }

    public AsterOperation[] getOperations() {
        System.out.println("Get Operation");
        AsterOperation[] foo = new AsterOperation[1];
        return foo;
    }

    public void setImg(BufferedImage img) {
    }

    protected String toScript() {
        return new String();
    }

    protected String[] getRegex() {
        String[] regexs = { "" };
        return regexs;
    }
}
