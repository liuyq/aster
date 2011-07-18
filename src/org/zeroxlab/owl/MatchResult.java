/*
 * MatchResult
 */
package org.zeroxlab.owl;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

public class MatchResult {
    public int x;
    public int y;
    public int w;
    public int h;
    public int cx;
    public int cy;
    public double maxval;

    public MatchResult() {}

    public MatchResult(int x, int y, int w, int h, double val) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.cx = x + w / 2;
        this.cy = y + h / 2;
        this.maxval = val;
    }

    @Override
    public String toString() {
        return cvRect(x, y, w, h).toString() + ": " + maxval;
    }
}
