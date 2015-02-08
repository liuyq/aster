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

import org.zeroxlab.aster.ScreenUpdatePanel;
import org.zeroxlab.aster.operations.AsterOperation;

public class WaitImage extends AsterCommand {
    private static final String name = "WaitImage";

    double mTimeout = 30.0;
    double mSimilarity = 0.9;
    double mWaitDuration = 10.0;

    public WaitImage() {
        mOps = new AsterOperation[1];
        mOps[0] = ScreenUpdatePanel.getInstance().getOpTouch();
    }

    public WaitImage(String rootPath, String argline)
            throws IllegalArgumentException {
        super.setRootPath(rootPath);
        super.setFilled(true);
        String[] args = splitArgs(argline);
        if (args.length == 2) {
            // wait(image, landscape)
            try {
                args[0] = stripQuote(args[0]);
                mImage = ImageIO.read(new File(rootPath, args[0]));
                mSerial = Integer.parseInt(args[0].substring(0,
                        args[0].length() - 4));
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
        } else {
            throw new IllegalArgumentException();
        }
        mOps = new AsterOperation[1];
        mOps[0] = ScreenUpdatePanel.getInstance().getOpTouch();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Wait");
        if (mImage != null) {
            settings.put("Image", mImage);
        }
        settings.put("Landscape", mLandscape);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings.containsKey("Image")) {
            mImage = (BufferedImage) settings.get("Image");
            mSerial = mSeqNext++;
            saveImage(getRootPath());
        }
        if (settings.containsKey("Landscape")) {
            mLandscape = (Boolean) settings.get("Landscape");
        }
    }

    @Override
    public String toScript() {
        return String.format("%s('%d.png', %s)\n", name, mSerial,
                mLandscape ? "True" : "False");
    }

    @Override
    public void execute() {
        device.waitImageUntil(String.format("%d.png", mSerial),
                device.getScreenShotPath(), mLandscape);

    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }
}
