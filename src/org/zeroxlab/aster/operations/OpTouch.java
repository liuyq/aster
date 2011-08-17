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
 * Authored by Julian Chu <walkingice@0xlab.org>
 *             Kan-Ru Chen <kanru@0xlab.org>
 *             Wei-Ning Huang <azhuang@0xlab.org>
 */

package org.zeroxlab.aster;

import java.awt.Point;

public abstract class OpTouch implements AsterOperation {

    protected Point mPoint;
    public final static int INVALID = -1;

    public OpTouch() {
        mPoint = new Point(INVALID, INVALID);
    }

    public Point getPoint() {
        return mPoint;
    }

    public int getX() {
        return (int)mPoint.getX();
    }

    public int getY() {
        return (int)mPoint.getY();
    }

    public void set(int x, int y) {
        mPoint.setLocation(x, y);
    }

    public boolean isValid() {
        return (getX() != INVALID && getY() != INVALID);
    }
}
