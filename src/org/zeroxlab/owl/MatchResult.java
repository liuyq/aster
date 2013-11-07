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

package org.zeroxlab.owl;

import static com.googlecode.javacv.cpp.opencv_core.cvRect;

public class MatchResult {
    public int x;
    public int y;
    public int w;
    public int h;
    public double maxval;

    public MatchResult() {}

    public MatchResult(int x, int y, int w, int h, double val) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.maxval = val;
    }

    public int cx() {
        return x + w / 2;
    }

    public int cy() {
        return y + h / 2;
    }

    @Override
    public String toString() {
        return String.format("%s: %f", cvRect(x, y, w, h).toString(), maxval);
    }
}
