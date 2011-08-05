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

public abstract class OpDrag implements AsterOperation {

    protected Point mStart;
    protected Point mEnd;

    public OpDrag() {
        mStart = new Point();
        mEnd   = new Point();
    }

    public Point getStart() {
        return mStart;
    }

    public Point getEnd() {
        return mEnd;
    }

    public int getStartX() {
        return (int)mStart.getX();
    }

    public int getStartY() {
        return (int)mStart.getY();
    }

    public int getEndX() {
        return (int)mEnd.getX();
    }

    public int getEndY() {
        return (int)mEnd.getY();
    }

    public void set(int sX, int sY, int eX, int eY) {
        mStart.setLocation(sX, sY);
        mEnd.setLocation(eX, eY);
    }
}
