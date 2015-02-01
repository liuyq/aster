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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.script.SimpleBindings;

import org.linaro.utils.Constants;
import org.zeroxlab.aster.ScreenUpdatePanel;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.owl.MatchResult;

public class Drag extends AsterCommand {

    private enum CoordType {
        FIXED, AUTO
    }

    CoordType mCoordType;
    Point mStartPosition;
    Point mEndPosition;
    Point mShiftDistance;

    double mTimeout = Constants.DEFAULT_TIMEOUT;
    double mSimilarity = Constants.DEFAULT_SIMILARITY;

    public Drag() {
        mCoordType = CoordType.FIXED;
        mStartPosition = new Point();
        mEndPosition = new Point();
        mShiftDistance = new Point();
        mOps = new AsterOperation[1];
        mOps[0] = ScreenUpdatePanel.getInstance().getOpDrag();
    }

    public Drag(String prefix, String argline) throws IllegalArgumentException {
        String[] args = splitArgs(argline);
        super.setFilled(true);

        if (args.length == 6) {
            // drag(start_image, (dx, dy), timeout, similarity, landscape)
            mCoordType = CoordType.AUTO;
            try {
                args[0] = stripQuote(args[0]);
                mImage = ImageIO.read(new File(prefix, args[0]));
                mSerial = Integer.parseInt(args[0].substring(0,
                        args[0].length() - 4));
                mSeqNext = mSerial + 1;
            } catch (IOException e) {
                throw new IllegalArgumentException(e.toString());
            } catch (NumberFormatException e) {
                mSerial = mSeqNext++;
            }
            try {
                mShiftDistance = new Point(Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]));
                mTimeout = Double.parseDouble(args[3]);
                mSimilarity = Double.parseDouble(args[4]);
                mLandscape = Boolean.parseBoolean(args[5]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else if (args.length == 4) {
            // drag((%d, %d), (%d, %d))
            try {
                mCoordType = CoordType.FIXED;
                mStartPosition = new Point(Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]));
                mEndPosition = new Point(Integer.parseInt(args[2]),
                        Integer.parseInt(args[3]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = ScreenUpdatePanel.getInstance().getOpDrag();
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
        settings.put("Timeout", mTimeout);
        settings.put("Similarity", mSimilarity);
        settings.put("Landscape", mLandscape);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings.containsKey("CoordType")) {
            mCoordType = (CoordType) settings.get("CoordType");
        }

        if (settings.containsKey("Image")) {
            mImage = (BufferedImage) settings.get("Image");
            mSerial = mSeqNext++;
            saveImage(System.getProperty("user.dir"));
        }
        if (settings.containsKey("StartPos")) {
            mStartPosition = (Point) settings.get("StartPos");
        }
        if (settings.containsKey("EndPos")) {
            mEndPosition = (Point) settings.get("EndPos");
        }
        mShiftDistance.setLocation(mEndPosition.getX() - mStartPosition.getX(),
                mEndPosition.getY() - mStartPosition.getY());

        if (settings.containsKey("Timeout")) {
            mTimeout = (Double) settings.get("Timeout");
        }
        if (settings.containsKey("Similarity")) {
            mTimeout = (Double) settings.get("Similarity");
        }
        if (settings.containsKey("Landscape")) {
            mLandscape = (Boolean) settings.get("Landscape");
        }
    }

    @Override
    public String toScript() {
        if (isAuto()) {
            return String.format("drag('%d.png', (%d, %d), %.1f, %.2f, %s)\n",
                    mSerial, (int) mShiftDistance.getX(),
                    (int) mShiftDistance.getY(), mTimeout, mSimilarity,
                    mLandscape ? "True" : "False");
        } else {
            return String.format("drag((%d, %d), (%d, %d))\n",
                    (int) mStartPosition.getX(), (int) mStartPosition.getY(),
                    (int) mEndPosition.getX(), (int) mEndPosition.getY());
        }
    }

    @Override
    public void execute() {
        int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
        if (isAuto()) {
            MatchResult r = device.waitImageUntil(
                    String.format("%d.png", mSerial),
                    device.getScreenShotPath(), mTimeout, mLandscape);
            x1 = r.cx();
            y1 = r.cy();
            x2 = x1 + (int) mShiftDistance.getX();
            y2 = y1 + (int) mShiftDistance.getY();
        } else {
            x1 = (int) mStartPosition.getX();
            y1 = (int) mStartPosition.getY();
            x2 = (int) mEndPosition.getX();
            y2 = (int) mEndPosition.getY();
        }
        super.device.drag(x1, y1, x2, y2);
    }

    @Override
    protected String getCommandPrefix() {
        return "drag";
    }
}
