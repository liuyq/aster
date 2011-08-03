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

import org.zeroxlab.aster.AsterCommand;

import java.awt.image.BufferedImage;
import java.awt.Point;

import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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

    public Press(String keycode) {
	mKeyCode = keycode;
    }

    /*
     * Format:
     * 1. keycode, type
     */
    public Press(String[] strs) throws IllegalArgumentException {
        if (strs.length == 2) {
            mKeyCode = strs[0];
            mPressType = PressType.parse(strs[1]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getKeyCode() {
	return mKeyCode;
    }

    @Override
    protected String toScript() {
        return String.format("press(\"%s\", \"%s\")",
                             mKeyCode, mPressType.getTypeStr());
    }

    @Override
    protected String[] getRegex() {
        String[] regexs = {
            "press\\s*\\(\\s*\"(\\w+)\"\\s*,\\s*\"(\\w+)\"\\s*\\)",
        };
        return regexs;
    }
}
