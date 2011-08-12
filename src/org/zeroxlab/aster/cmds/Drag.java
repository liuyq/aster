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

import java.awt.image.BufferedImage;
import java.awt.Point;

import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.script.SimpleBindings;

public class Drag extends AsterCommand {

    private enum CoordType { FIXED, AUTO }

    CoordType mCoordType;
    Point mStartPosition;
    Point mEndPosition;

    double mDuration = 1;
    int mSteps = 10;
    double mTimeout = 3;

    public Drag(Point pos2) {
	mCoordType = CoordType.AUTO;
	mStartPosition = new Point();
        mEndPosition = pos2;
    }

    public Drag(Point pos1, Point pos2) {
	mCoordType = CoordType.FIXED;
	mStartPosition = pos1;
        mEndPosition = pos2;
    }

    /*
     * Format:
     * 1. start_img, end_x1, end_y1, duration, step, timeout
     * 2. start_x1, start_y1, end_x1, end_y1, duration, step, timeout
     */
    public Drag(String[] strs) throws IllegalArgumentException {
        if (strs.length == 6) {
            try {
                mImage = ImageIO.read(new File(strs[0]));
                mSerial = Integer.parseInt(strs[0]);
                mEndPosition.setLocation(Integer.parseInt(strs[1]),
                                         Integer.parseInt(strs[2]));
            } catch (IOException e) {
                throw new IllegalArgumentException(e.toString());
            }
            try {
                mDuration = Double.parseDouble(strs[3]);
                mSteps = Integer.parseInt(strs[4]);
                mTimeout = Double.parseDouble(strs[5]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else if (strs.length == 7) {
            try {
                mStartPosition.setLocation(Integer.parseInt(strs[0]),
                                           Integer.parseInt(strs[1]));
                mEndPosition.setLocation(Integer.parseInt(strs[2]),
                                         Integer.parseInt(strs[3]));
                mDuration = Double.parseDouble(strs[4]);
                mSteps = Integer.parseInt(strs[5]);
                mTimeout = Double.parseDouble(strs[6]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Point getStartPos() {
	return mStartPosition;
    }

    public Point getEndPos() {
	return mEndPosition;
    }

    public boolean isAuto() {
	return mCoordType == CoordType.AUTO;
    }

    public boolean isFixed() {
	return mCoordType == CoordType.FIXED;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Drag");
        settings.put("CoordType", mCoordType);
        if (isFixed()) {
            settings.put("StartPos", mStartPosition);
            settings.put("EndPos", mEndPosition);
        } else {
            settings.put("Image", mImage);
            settings.put("Offset", mEndPosition);
        }
        settings.put("Duration", mDuration);
        settings.put("Steps", mSteps);
        settings.put("Timeout", mTimeout);
        settings.put("Landscape", mLandscape);
        return settings;
    }

    @Override
    public void fill(SimpleBindings settings) {
        mCoordType = (CoordType)settings.get("CoordType");
        if (mCoordType == CoordType.AUTO) {
            mImage = (BufferedImage)settings.get("Image");
            mEndPosition = (Point)settings.get("Offset");
        } else {
            mStartPosition = (Point)settings.get("StartPos");
            mEndPosition = (Point)settings.get("EndPos");
        }
        mDuration = (Double)settings.get("Duration");
        mSteps = (Integer)settings.get("Steps");
        mTimeout = (Double)settings.get("Timeout");
        mLandscape = (Boolean)settings.get("Landscape");
    }

    @Override
    public AsterOperation[] getOperations() {
        System.out.println("Drag operation");
        AsterOperation[] ops = new OpDrag[1];
        return ops;
    }

    @Override
    protected String toScript() {
        if (isAuto()) {
            return String.format("drag(\"%d.jpg\", (%d, %d), %f, %d, %d)\n",
                                 mSerial,
                                 mEndPosition.getX(), mEndPosition.getY(),
                                 mSteps, mTimeout);
        } else {
            return String.format("drag((%d, %d), (%d, %d), %f, %d, %d)\n",
                                 mStartPosition.getX(), mStartPosition.getY(),
                                 mEndPosition.getX(), mEndPosition.getY(),
                                 mDuration, mSteps, mTimeout);
        }
    }

    static protected String[] getRegex() {
        String[] regexs = {
            "drag\\s*\\(\\s*\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)\\s*,\\s*\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9.]+)\\s*\\)",
            "drag\\s*\\(\\s\"*(\\w+)\"\\s*,\\s*([0-9]+)\\s*\\)\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9.]+)\\s*\\)",
        };
        return regexs;
    }
}
