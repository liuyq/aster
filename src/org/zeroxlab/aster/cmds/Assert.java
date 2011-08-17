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

import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;

import java.io.IOException;

import java.io.File;
import javax.imageio.ImageIO;

import javax.script.SimpleBindings;

public class Assert extends AsterCommand {
    double mTimeout = 3;

    public Assert() {
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getOpTouch();
    }

    public Assert(SimpleBindings settings) {
        fillSettings(settings);
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getOpTouch();
    }

    public Assert(String prefix, String argline)
        throws IllegalArgumentException {
        String[] args = splitArgs(argline);
        if (args.length == 2) {
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
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else {
            throw new IllegalArgumentException();
        }
        mOps = new AsterOperation[1];
        mOps[0] = AsterWorkspace.getOpTouch();
    }

    @Override
    public String getName() {
        return "Assert";
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Name", "Assert");
        if (mImage != null) {
            settings.put("Image", mImage);
        }
        settings.put("Timeout", mTimeout);
        return settings;
    }

    @Override
    public void fillSettings(SimpleBindings settings) {
        if (settings.containsKey("Image")) {
            mImage = (BufferedImage)settings.get("Image");
        }
        if (settings.containsKey("Timeout")) {
            mTimeout = (Double)settings.get("Timeout");
        }
    }

    @Override
    protected String toScript() {
        return String.format("iassert('%d.png', %f)\n", mSerial, mTimeout);
    }
}
