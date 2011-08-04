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
 * Authored by Kan-Ru Chen <kanru@0xlab.org>
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

public class Touch extends AsterCommand {

    private enum CoordType { FIXED, AUTO }
    private enum TouchType {
        DOWN("down"), UP("up"), DOWN_AND_UP("downAndUp");

        private String typeStr;
        TouchType(String typestr) {
            typeStr = typestr;
        }

        public String getTypeStr() {
            return typeStr;
        }

        static public TouchType parse(String str) {
            if (str == "down") {
                return DOWN;
            } else if (str == "up") {
                return UP;
            } else {
                return DOWN_AND_UP;
            }
        }
    }

    CoordType mCoordType;
    BufferedImage mImage;
    Point mPosition;
    TouchType mTouchType;
    double mTimeout = 3;

    public Touch(BufferedImage img) {
	mCoordType = CoordType.AUTO;
	mImage = img;
    }

    public Touch(Point pos) {
	mCoordType = CoordType.FIXED;
	mPosition = pos;
    }

    /*
     * Format:
     * 1. img, type, timeout
     * 2. pos_x, pos_y, type, timeout
     */
    public Touch(String[] strs) throws IllegalArgumentException {
        if (strs.length == 3) {
            try {
                mImage = ImageIO.read(new File(strs[0]));
            } catch (IOException e) {
                throw new IllegalArgumentException(e.toString());
            }
            mTouchType = TouchType.parse(strs[1]);
        } else if (strs.length == 4) {
            try {
                mPosition.setLocation(Integer.parseInt(strs[0]),
                                      Integer.parseInt(strs[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
            mTouchType = TouchType.parse(strs[2]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public BufferedImage getImage() {
	return mImage;
    }

    public Point getPos() {
	return mPosition;
    }

    public boolean isAuto() {
	return mCoordType == CoordType.AUTO;
    }

    public boolean isFixed() {
	return mCoordType == CoordType.FIXED;
    }

    @Override
    protected String toScript() {
        if (isAuto()) {
            return String.format("touch(%d.jpg, \"%s\", %d)", 0,
                                 mTouchType.getTypeStr(), mTimeout);
        } else {
            return String.format("touch(%d, %d, \"%s\", %d)",
                                 mPosition.getX(), mPosition.getY(),
                                 mTouchType.getTypeStr(), mTimeout);
        }
    }

    @Override
    protected String[] getRegex() {
        String[] regexs = {
            "touch\\s*\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*\"(\\w+)\"\\s*,\\s*([0-9]+)\\s*\\)",
            "touch\\s*\\(\\s*\"(\\w+)\"\\s*,\\s*\"(\\w+)\"\\s*,\\s*([0-9]+)\\s*\\)"
        };
        return regexs;
    }
}
