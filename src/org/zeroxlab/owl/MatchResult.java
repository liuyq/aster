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
