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

import org.zeroxlab.aster.AsterOperation;
import org.zeroxlab.wookieerunner.ScriptRunner;

import com.android.monkeyrunner.MonkeyRunnerOptions;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.script.SimpleBindings;

import org.python.core.PyException;

public abstract class AsterCommand {
    private static ScriptRunner mRunner;
    protected static int mSeqNext = 0;
    protected int mSerial = 0;
    protected boolean mLandscape = false;
    protected BufferedImage mImage = null;
    protected AsterOperation[] mOps;

    public class ExecutionResult {
        public boolean mSuccess;
        public String mMessage;

        public ExecutionResult() {
        }

        public ExecutionResult(boolean success, String message) {
            mSuccess = success;
            mMessage = message;
        }
    }

    static public void setScriptRunner(ScriptRunner runner) {
        mRunner = runner;
    }

    protected String[] splitArgs(String argline) {
        String[] args = argline.split(",");
        for (int i = 0; i < args.length; ++i)
            args[i] = args[i].replaceAll("^[() ]+","").replaceAll("[() ]$","");
        return args;
    }

    protected String stripQuote(String src) {
        return src.replaceAll("'", "").replaceAll("\"", "");
    }

    public BufferedImage getImage() {
        return mImage;
    }

    public void saveImage(String prefix) throws IOException {
        if (mImage != null) {
            File pngfile = new File(prefix, String.format("%d.png", mSerial));
            pngfile.delete();
            ImageIO.write(mImage, "png", pngfile);
        }
    }

    /* Return operations that stored in this Command */
    public AsterOperation[] getOperations() {
        return mOps;
    }

    /* Get name of command */
    public abstract String getName();

    /* Get settings of a command */
    public abstract SimpleBindings getSettings();

    /* Set settings of a command */
    public abstract void fillSettings(SimpleBindings settings);

    /* Dump command to script text */
    protected abstract String toScript();

    /* Execute command */
    public ExecutionResult execute() {
        try {
            mRunner.runStringLocal(toScript());
        } catch (PyException e) {
            return new ExecutionResult(false, e.toString());
        }
        return new ExecutionResult(true, "");
    }

    public interface CommandListener {
        public void commandFinished(AsterCommand whichOne);
    }
}
