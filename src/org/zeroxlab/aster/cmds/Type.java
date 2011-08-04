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

import org.zeroxlab.aster.AsterCommand;

import java.awt.image.BufferedImage;
import java.awt.Point;

import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Type extends AsterCommand {

    String mText;

    public Type(String text) {
	mText = text;
    }

    /*
     * Format:
     * 1. text
     */
    public Type(String[] strs) throws IllegalArgumentException {
        if (strs.length == 1) {
            mText = strs[0];
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getText() {
	return mText;
    }

    @Override
    protected String toScript() {
        return String.format("type(\"%s\")", mText);
    }

    @Override
    protected String[] getRegex() {
        String[] regexs = {
            "type\\s*\\(\\s*\"(\\w+)\"\\s*\\)",
        };
        return regexs;
    }
}
