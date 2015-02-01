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
import org.zeroxlab.aster.operations.OpSelectKey;

public class Press extends AsterCommand {

    private enum PressType {
        DOWN("down"), UP("up"), DOWN_AND_UP("downAndUp");

        private String typeStr;

        PressType(String typestr) {
            typeStr = typestr;
        }

        public String getTypeStr() {
            return typeStr;
        }

        static public PressType parse(String str) {
            if (str == "down") {
                return DOWN;
            } else if (str == "up") {
                return UP;
            } else {
                return DOWN_AND_UP;
            }
        }
    }

    PressType mPressType;
    String mKeyCode;

    public Press() {
        this(new OpSelectKey());
    }

    public Press(AsterOperation op) {
        mPressType = PressType.DOWN_AND_UP;
        mKeyCode = new String();
        super.mOps = new AsterOperation[1];
        super.mOps[0] = op;
        try {
            this.onFillSettings(op.getSettings());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Press(String argline)
            throws IllegalArgumentException {
        super.setFilled(true);
        String[] args = splitArgs(argline);
        if (args.length == 2) {
            // press(keycode, type)
            mKeyCode = stripQuote(args[0]);
            mPressType = PressType.parse(args[1]);
        } else {
            throw new IllegalArgumentException();
        }
        super.mOps = new AsterOperation[1];
        super.mOps[0] = new OpSelectKey();
    }

    public String getKeyCode() {
        return mKeyCode;
    }

    @Override
    public String getName() {
        return "Press";
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Press");
        settings.put("KeyCode", mKeyCode);
        settings.put("Type", mPressType.getTypeStr());
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings.containsKey("KeyCode")) {
            mKeyCode = (String) settings.get("KeyCode");
        }
        if (settings.containsKey("Type")) {
            mPressType = PressType.parse((String) settings.get("Type"));
        }
    }

    @Override
    public String toScript() {
        return String.format("press('%s', '%s')\n", mKeyCode,
                mPressType.getTypeStr());
    }

    @Override
    public void execute() {
        super.device.press(mKeyCode);
    }

    @Override
    protected String getCommandPrefix() {
        return "press";
    }
}
