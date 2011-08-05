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

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import org.zeroxlab.aster.AsterOperation;

public abstract class AsterCommand {

    public abstract String getName();

    /* Return operations that stored in this Command */
    public AsterOperation[] getOperations() {
        System.out.println("Get Operation");
        AsterOperation[] foo = new AsterOperation[1];
        return foo;
    }

    public void setImg(BufferedImage img) {
    }

    /* uncomment these abstract methods and implement them
    public abstract void drawHint(Graphics g);
    public abstract void execute();
    public abstract Map getSettings();
    */

    /* Dump command to script text */
    protected abstract String toScript();

    /* Get regex for matching command from script */
    protected abstract String[] getRegex();
}
