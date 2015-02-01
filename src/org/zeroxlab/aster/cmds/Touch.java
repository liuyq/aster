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

public class Touch extends AsterCommand {

    private enum CoordType {
        FIXED, AUTO
    }

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
    double mTimeout = Constants.DEFAULT_TIMEOUT;
    double mSimilarity = Constants.DEFAULT_SIMILARITY;

    public Touch() {
        mCoordType = CoordType.AUTO;
        mTouchType = TouchType.DOWN_AND_UP;
        mPosition = new Point();
        mOps = new AsterOperation[1];
        mOps[0] = ScreenUpdatePanel.getInstance().getOpTouch();
    }

    public Touch(String prefix, String argline) throws IllegalArgumentException {
        super.setFilled(true);
        String[] args = splitArgs(argline);

        if (args.length == 5) {
            // touch(image, type, timeout, similarity, landscape)
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
                mTouchType = TouchType.parse(args[1]);
                mTimeout = Double.parseDouble(args[2]);
                mSimilarity = Double.parseDouble(args[3]);
                mLandscape = Boolean.parseBoolean(args[4]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else if (args.length == 3) {
            // touch(x, y, type)
            try {
                mCoordType = CoordType.FIXED;
                mPosition = new Point(Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]));
                mTouchType = TouchType.parse(args[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = ScreenUpdatePanel.getInstance().getOpTouch();
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
        settings.put("Similarity", mSimilarity);
        settings.put("Type", mTouchType.getTypeStr());
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
        if (settings.containsKey("Pos")) {
            mPosition = (Point) settings.get("Pos");
        }
        if (settings.containsKey("Type")) {
            mTouchType = TouchType.parse((String) settings.get("Type"));
        }
        if (settings.containsKey("Timeout")) {
            mTimeout = (Double) settings.get("Timeout");
        }
        if (settings.containsKey("Similarity")) {
            mSimilarity = (Double) settings.get("Similarity");
        }
        if (settings.containsKey("Landscape")) {
            mLandscape = (Boolean) settings.get("Landscape");
        }
    }

    @Override
    public String toScript() {
        if (isAuto()) {
            return String.format("touch('%d.png', '%s', %.1f, %.2f, %s)\n",
                    mSerial, mTouchType.getTypeStr(), mTimeout, mSimilarity,
                    mLandscape ? "True" : "False");
        } else {
            return String.format("touch(%d, %d, '%s')\n",
                    (int) mPosition.getX(), (int) mPosition.getY(),
                    mTouchType.getTypeStr());
        }
    }

    @Override
    public void execute() {
        int x = 0, y = 0;
        if (isAuto()) {
            MatchResult r = device.waitImageUntil(
                    String.format("%d.png", mSerial),
                    device.getScreenShotPath(), mTimeout, mLandscape);
            x = r.cx();
            y = r.cy();
        } else {
            x = (int) mPosition.getX();
            y = (int) mPosition.getY();
        }
        device.touch(x, y);
    }

}
