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

import java.awt.Point;
import java.awt.event.MouseListener;
import java.util.Vector;

import org.zeroxlab.aster.AsterWorkspace;
import org.zeroxlab.aster.Touch;

public class CmdSelector {

    String[] mNames;
    AsterWorkspace mWorkspace;

    CmdSelector(AsterWorkspace workspace) {
        mNames = new String[2];
        mNames[0] = "Touch";
        mNames[1] = "Drag";
        mWorkspace = workspace;
    }

    public static String getMsg() {
        return "選擇要加入的動作";
    }

    public static String getTitle() {
        return "新增動作";
    }

    public Object[] getCmdNames() {
        return mNames;
    }

    public Object getDefValue() {
        return mNames[0];
    }

    public AsterCommand selectCmd(int i) {
        if (i == 0) {
            return new Touch(mWorkspace.getOpTouch());
        } else if (i == 1) {
            return new Drag(mWorkspace.getOpDrag());
        }

        System.err.println("Unknow index:" + i);
        return null;
    }
}
