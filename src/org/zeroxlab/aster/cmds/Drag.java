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

public class Drag extends AsterCommand {

    private enum CoordType { FIXED, AUTO }

    CoordType mCoordType;
    BufferedImage mStartImage;
    BufferedImage mEndImage;
    Point mStartPosition;
    Point mEndPosition;

    double mDuration = 1;
    int mSteps = 10;
    double mTimeout = 3;

    public Drag(BufferedImage img1, BufferedImage img2) {
	mCoordType = CoordType.AUTO;
	mStartImage = img1;
	mEndImage = img2;
    }

    public Drag(Point pos1, Point pos2) {
	mCoordType = CoordType.FIXED;
	mStartPosition = pos1;
        mEndPosition = pos2;
    }

    /*
     * Format:
     * 1. start_img, end_img, duration, step, timeout
     * 2. start_x1, start_y1, end_x1, end_y1, duration, step, timeout
     */
    public Drag(String[] strs) throws IllegalArgumentException {
        if (strs.length == 5) {
            try {
                mStartImage = ImageIO.read(new File(strs[0]));
                mEndImage = ImageIO.read(new File(strs[1]));
            } catch (IOException e) {
                throw new IllegalArgumentException(e.toString());
            }
            try {
                mDuration = Double.parseDouble(strs[2]);
                mSteps = Integer.parseInt(strs[3]);
                mTimeout = Double.parseDouble(strs[4]);
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

    @Override
    protected String toScript() {
        if (isAuto()) {
            return String.format("drag(\"%d.jpg\", \"%d.jpg\", %f, %d, %d)",
                                 0, 1, mDuration, mSteps, mTimeout);
        } else {
            return String.format("drag((%d, %d), (%d, %d), %f, %d, %d)",
                                 mStartPosition.getX(), mStartPosition.getY(),
                                 mEndPosition.getX(), mEndPosition.getY(),
                                 mDuration, mSteps, mTimeout);
        }
    }

    @Override
    protected String[] getRegex() {
        String[] regexs = {
            "drag\\s*\\(\\s*\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)\\s*,\\s*\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9.]+)\\s*\\)",
            "drag\\s*\\(\\s\"*(\\w+)\"\\s*,\\s*\"(\\w+)\"\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9.]+)\\s*\\)",
        };
        return regexs;
    }

    public boolean isAuto() {
	return mCoordType == CoordType.AUTO;
    }

    public boolean isFixed() {
	return mCoordType == CoordType.FIXED;
    }

    public BufferedImage getStartImage() {
	return mStartImage;
    }

    public BufferedImage getEndImage() {
	return mEndImage;
    }

    public Point getStartPos() {
	return mStartPosition;
    }

    public Point getEndPos() {
	return mEndPosition;
    }
}
