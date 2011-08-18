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

import javax.script.SimpleBindings;
import javax.swing.JOptionPane;

public class OpSelectKey implements AsterOperation {

    public final static String[] sKeys = {
        "KEYCODE_BACK",
        "KEYCODE_MENU",
        "KEYCODE_HOME",
        "KEYCODE_SEARCH",
        "KEYCODE_CLEAR",
        "KEYCODE_DEL",
        "KEYCODE_ENTER",
        "KEYCODE_SPACE",
        "KEYCODE_DPAD_UP",
        "KEYCODE_DPAD_DOWN",
        "KEYCODE_DPAD_LEFT",
        "KEYCODE_DPAD_RIGHT",
        "KEYCODE_DPAD_CENTER",
        "KEYCODE_VOLUME_UP",
        "KEYCODE_VOLUME_DOWN",
        "KEYCODE_VOLUME_MUTE",
        "KEYCODE_ZOOM_IN",
        "KEYCODE_ZOOM_OUT",
        "KEYCODE_CAMERA",
    };

    protected String mKey;

    public OpSelectKey() {
        mKey = new String();
        this.set(sKeys[0]);
    }

    public String getKey() {
        return mKey;
    }

    public void set(String key) {
        for (int i = 0; i < sKeys.length; i++) {
            if (key.equals(sKeys[i])) {
                mKey = key;
            }
        }
    }

    @Override
    public String getName() {
        return "Select Key";
    }

    @Override
    public void record(OperationListener listener) {
        Object selection = JOptionPane.showInputDialog(
                null
                , "Please choose a name"
                , "Example 1"
                , JOptionPane.QUESTION_MESSAGE
                , null
                , sKeys
                , mKey
                );

        if (selection != null) {
            set(selection.toString());
        } else  {
            System.out.println("Pressed Cancl, still using " +mKey);
        }

        listener.operationFinished(this);
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("KeyCode", mKey);
        return settings;
    }
}
