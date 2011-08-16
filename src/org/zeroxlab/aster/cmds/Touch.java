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

import java.awt.image.BufferedImage;
import java.awt.Point;

import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.script.SimpleBindings;

import org.zeroxlab.aster.AsterOperation;
import org.zeroxlab.aster.AsterWorkspace;
import org.zeroxlab.aster.OpTouch;

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
    Point mPosition;
    TouchType mTouchType;
    double mTimeout = 3;
    AsterOperation[] mOps;

    public Touch() {
        mCoordType = CoordType.FIXED;
        mTouchType = TouchType.DOWN_AND_UP;
        mPosition = new Point();
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getOpTouch();
    }

    public Touch(String argline) throws IllegalArgumentException {
        String[] args = splitArgs(argline);

        if (args.length == 4) {
            mCoordType = CoordType.AUTO;
            try {
                args[0] = args[0].replaceAll("'", "").replaceAll("\"", "");
                mImage = ImageIO.read(new File(args[0]));
                mSerial = Integer.parseInt(args[0].substring(0,
                                           args[0].length() -4));
                mSeqNext = mSerial + 1;
            } catch (IOException e) {
                throw new IllegalArgumentException(e.toString());
            } catch (NumberFormatException e) {
                mSerial = mSeqNext++;
            }
            mTouchType = TouchType.parse(args[1]);
            mTimeout = Double.parseDouble(args[2]);
            mLandscape = Boolean.parseBoolean(args[3]);
        } else if (args.length == 5) {
            mCoordType = CoordType.FIXED;
            mPosition = new Point(Integer.parseInt(args[0]),
                                  Integer.parseInt(args[1]));
            mTouchType = TouchType.parse(args[2]);
            mTimeout = Double.parseDouble(args[3]);
            mLandscape = Boolean.parseBoolean(args[4]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getOpTouch();
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
    public String getName() {
        return "Touch";
    }

    @Override
    public AsterOperation[] getOperations() {
        return mOps;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Touch");
        settings.put("CoordType", mCoordType);
        if (isFixed()) {
            settings.put("Pos", mPosition);
        } else {
            settings.put("Image", mImage);
        }
        settings.put("Timeout", mTimeout);
        settings.put("Type", mTouchType.getTypeStr());
        settings.put("Landscape", mLandscape);
        return settings;
    }

    @Override
    public void fillSettings(SimpleBindings settings) {
        mCoordType = (CoordType)settings.get("CoordType");
        if (mCoordType == CoordType.AUTO) {
            mImage = (BufferedImage)settings.get("Image");
        } else {
            mPosition = (Point)settings.get("Pos");
        }
        mTouchType = (TouchType)settings.get("Type");
        mTimeout = (Double)settings.get("Timeout");
        mLandscape = (Boolean)settings.get("Landscape");
        mSerial = (Integer)settings.get("Serial");
    }

    @Override
    protected String toScript() {
        if (isAuto()) {
            return String.format("touch('%d.png', '%s', %.1f, %s)\n", mSerial,
                                 mTouchType.getTypeStr(), mTimeout,
                                 mLandscape? "True": "False");
        } else {
            return String.format("touch(%d, %d, '%s', %.1f, %s)\n",
                                 (int)mPosition.getX(), (int)mPosition.getY(),
                                 mTouchType.getTypeStr(), mTimeout,
                                 mLandscape? "True": "False");
        }
    }

    static protected String[] getKeys() {
        String[] keys = {
        };
        return keys;
    }
}
