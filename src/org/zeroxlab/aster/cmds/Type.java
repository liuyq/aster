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

public class Type extends AsterCommand {
    String tipMsg = "Please input the string to send to device";
    String mText;

    public Type() {
        mText = new String();
        super.mOps = new AsterOperation[1];
        super.mOps[0] = new OpGetInput(tipMsg, "");
    }

    public Type(String argline)
            throws IllegalArgumentException {
        super.setFilled(true);
        String[] args = splitArgs(argline);

        if (args.length == 1) {
            // type(text)
            mText = stripQuote(args[0]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = new OpGetInput(tipMsg, "");
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
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings == null) {
            return;
        }

        if (settings.containsKey("Text")) {
            mText = (String)settings.get("Text");
        }
    }

    @Override
    public String toScript() {
        return String.format("type('%s')\n", mText);
    }

    @Override
    public void execute() {
        super.device.inputText(mText);
    }
}
