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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.script.SimpleBindings;

import org.zeroxlab.aster.AsterWorkspace;
import org.zeroxlab.aster.operations.AsterOperation;

public class Wait extends AsterCommand {

    private enum WaitType { IMAGE, TIME }

    WaitType mWaitType;
    double mTimeout = 30.0;
    double mSimilarity = 0.9;
    double mWaitDuration = 10.0;

    public Wait() {
        mWaitType = WaitType.IMAGE;
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getInstance().getOpTouch();
    }

    public Wait(String prefix, String argline)
        throws IllegalArgumentException {
        super.setFilled(true);
        String[] args = splitArgs(argline);
        if (args.length == 4) {
            // wait(image, timeout, similarity, landscape)
            mWaitType = WaitType.IMAGE;
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
                mTimeout = Double.parseDouble(args[1]);
                mSimilarity = Double.parseDouble(args[2]);
                mLandscape = Boolean.parseBoolean(args[3]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else if (args.length == 1) {
            // wait(time)
            mWaitType = WaitType.TIME;
            mWaitDuration = Double.parseDouble(args[0]);
        } else {
            throw new IllegalArgumentException();
        }
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getInstance().getOpTouch();
    }

    @Override
    public String getName() {
        return "Wait";
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Wait");
        settings.put("WaitType", mWaitType);
        if (mImage != null) {
            settings.put("Image", mImage);
        }
        settings.put("Timeout", mTimeout);
        settings.put("Similarity", mSimilarity);
        settings.put("Landscape", mLandscape);
        settings.put("WaitDuration", mWaitDuration);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings.containsKey("WaitType")) {
            mWaitType = (WaitType)settings.get("WaitType");
        }
        if (settings.containsKey("Image")) {
            mImage = (BufferedImage)settings.get("Image");
            mSerial = mSeqNext++;
            saveImage(System.getProperty("user.dir"));
        }
        if (settings.containsKey("Timeout")) {
            mTimeout = (Double)settings.get("Timeout");
        }
        if (settings.containsKey("Similarity")) {
            mSimilarity = (Double)settings.get("Similarity");
        }
        if (settings.containsKey("Landscape")) {
            mLandscape = (Boolean)settings.get("Landscape");
        }
        if (settings.containsKey("WaitDuration")) {
            mLandscape = (Boolean)settings.get("WaitDuration");
        }
    }

    @Override
    public String toScript() {
        if (mWaitType == WaitType.IMAGE) {
            return String.format("wait('%d.png', %.1f, %.2f, %s)\n", mSerial,
                    mTimeout, mSimilarity, mLandscape? "True": "False");
        } else {
            return String.format("wait(%.2f)\n", mWaitDuration);
        }
    }

    @Override
    public void executeFromJava() throws Exception {
        // TODO Auto-generated method stub
        throw new Exception("not implemented");
    }
}
