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

import java.awt.image.BufferedImage;
import java.awt.Graphics;

import javax.script.SimpleBindings;

public abstract class AsterCommand {
    private static ScriptRunner mRunner;
    protected BufferedImage mImage;

    static public void setScriptRunner(ScriptRunner runner) {
        mRunner = runner;
    }

    /* uncomment these abstract methods and implement them
    public abstract void drawHint(Graphics g);
    */

    /* Get image for command */
    public void setImg(BufferedImage img) {
        mImage = img;
    }

    /* Set image for command */
    public BufferedImage getImage() {
        return mImage;
    }

    /* Return operations that stored in this Command */
    public abstract AsterOperation[] getOperations();

    /* Get the name of command */
    public abstract String getName();

    /* Get settings of a command */
    public abstract SimpleBindings getSettings();

    /* Set settings of a command */
    public abstract void fill(SimpleBindings settings);

    /* Dump command to script text */
    protected abstract String toScript();

    /* Execute command */
    public void execute() {
        mRunner.runStringLocal(toScript());
    }

    /* Get regex for matching command from script */
    static protected String[] getRegex() {
        return null;
    }
}
