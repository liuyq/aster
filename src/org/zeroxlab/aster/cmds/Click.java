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

import org.zeroxlab.aster.AsterCommand;

import java.awt.image.BufferedImage;
import java.awt.Point;

public class Click extends AsterCommand {

    private static int FIXED = 1;
    private static int AUTO  = 2;

    BufferedImage mImage;
    Point mPosition;
    int mFixedOrAuto;

    public Click(BufferedImage img) {
	mImage = img;
	mFixedOrAuto = AUTO;
    }

    public Click(Point pos) {
	mPosition = pos;
	mFixedOrAuto = FIXED;
    }

    public boolean isAuto() {
	return mFixedOrAuto == AUTO;
    }

    public boolean isFixed() {
	return mFixedOrAuto == FIXED;
    }

    public BufferedImage getImage() {
	return mImage;
    }

    public Point getPos() {
	return mPosition;
    }
}
