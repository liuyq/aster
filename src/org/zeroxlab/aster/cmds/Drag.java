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

import org.zeroxlab.aster.AsterOperation;
import org.zeroxlab.aster.OpDrag;
import org.zeroxlab.aster.AsterWorkspace;

public class Drag extends AsterCommand {

    private enum CoordType { FIXED, AUTO }

    CoordType mCoordType;
    Point mStartPosition;
    Point mEndPosition;

    double mDuration = 0.5;
    int mSteps = 10;
    double mTimeout = 3;

    public Drag() {
        mCoordType = CoordType.FIXED;
        mStartPosition = new Point();
        mEndPosition = new Point();
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getInstance().getOpDrag();
    }

    public Drag(SimpleBindings settings) {
        fillSettings(settings);
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getInstance().getOpDrag();
    }

    public Drag(String prefix, String argline) throws IllegalArgumentException {
        String[] args = splitArgs(argline);

        if (args.length == 7) {
            mCoordType = CoordType.AUTO;
            try {
                args[0] = stripQuote(args[0]);
                mImage = ImageIO.read(new File(prefix, args[0]));
                mSerial = Integer.parseInt(args[0].substring(0,
                                           args[0].length() -4));
                mSeqNext = mSerial + 1;
            } catch (IOException e) {
                throw new IllegalArgumentException(e.toString());
            } catch (NumberFormatException e) {
                mSerial = mSeqNext++;
            }
            try {
                mEndPosition = new Point(Integer.parseInt(args[1]),
                                           Integer.parseInt(args[2]));
                mDuration = Double.parseDouble(args[3]);
                mSteps = Integer.parseInt(args[4]);
                mTimeout = Double.parseDouble(args[5]);
                mLandscape = Boolean.parseBoolean(args[6]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else if (args.length == 8) {
            try {
                mCoordType = CoordType.FIXED;
                mStartPosition = new Point(Integer.parseInt(args[0]),
                                           Integer.parseInt(args[1]));
                mEndPosition = new Point(Integer.parseInt(args[2]),
                                         Integer.parseInt(args[3]));
                mDuration = Double.parseDouble(args[4]);
                mSteps = Integer.parseInt(args[5]);
                mTimeout = Double.parseDouble(args[6]);
                mLandscape = Boolean.parseBoolean(args[7]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getInstance().getOpDrag();
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
    public String getName() {
        return "Drag";
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
    public void fillSettings(SimpleBindings settings) {
        if (settings.containsKey("CoordType")) {
            mCoordType = (CoordType)settings.get("CoordType");
        }

        if (mCoordType == CoordType.AUTO) {
            if (settings.containsKey("Image")) {
                mImage = (BufferedImage)settings.get("Image");
                mSerial = mSeqNext++;
            }
            if (settings.containsKey("Offset")) {
                mEndPosition = (Point)settings.get("Offset");
            }
        } else {
            if (settings.containsKey("StartPos")) {
                mStartPosition = (Point)settings.get("StartPos");
            }
            if (settings.containsKey("EndPos")) {
                mEndPosition = (Point)settings.get("EndPos");
            }
        }

        if (settings.containsKey("Duration")) {
            mDuration = (Double)settings.get("Duration");
        }
        if (settings.containsKey("Steps")) {
            mSteps = (Integer)settings.get("Steps");
        }
        if (settings.containsKey("Timeout")) {
            mTimeout = (Double)settings.get("Timeout");
        }
        if (settings.containsKey("Landscape")) {
            mLandscape = (Boolean)settings.get("Landscape");
        }
    }

    @Override
    protected String toScript() {
        if (isAuto()) {
            return String.format("drag('%d.png', (%d, %d), %.1f, %d, %.1f, %s)\n",
                                 mSerial,
                                 (int)mEndPosition.getX(),
                                 (int)mEndPosition.getY(),
                                 mDuration, mSteps, mTimeout,
                                 mLandscape? "True": "False");
        } else {
            return String.format("drag((%d, %d), (%d, %d), %.1f, %d, %.1f, %s)\n",
                                 (int)mStartPosition.getX(),
                                 (int)mStartPosition.getY(),
                                 (int)mEndPosition.getX(),
                                 (int)mEndPosition.getY(),
                                 mDuration, mSteps, mTimeout,
                                 mLandscape? "True": "False");
        }
    }
}
