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

package org.zeroxlab.aster;

import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;

import javax.script.SimpleBindings;

public class Type extends AsterCommand {

    String mText;

    public Type() {
        mText = new String();
        mOps = new AsterOperation[1];
        //mOps[0] = AsterWorkspace.getOpType();
    }

    public Type(SimpleBindings settings) {
        fillSettings(settings);
        mText = new String();
        mOps = new AsterOperation[1];
        //mOps[0] = AsterWorkspace.getOpType();
    }

    public Type(String argline) throws IllegalArgumentException {
        String[] args = splitArgs(argline);

        if (args.length == 1) {
            mText = stripQuote(args[0]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        //mOps[0] = AsterWorkspace.getOpType();
    }

    public String getText() {
	return mText;
    }

    @Override
    public String getName() {
        return "Type";
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Type");
        settings.put("Text", mText);
        return settings;
    }

    @Override
    public void fillSettings(SimpleBindings settings) {
        if (settings.containsKey("Text")) {
            mText = (String)settings.get("Text");
        }
    }

    @Override
    protected String toScript() {
        return String.format("type('%s')\n", mText);
    }
}
