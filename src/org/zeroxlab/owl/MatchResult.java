/*
 * MatchResult
 */
package org.zeroxlab.owl;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

public class MatchResult {
    public int m_X;
    public int m_Y;
    public int m_W;
    public int m_H;
    public double m_MaxVal;

    public MatchResult() {}

    public MatchResult(int x, int y, int w, int h, double val) {
        this.m_X = x;
        this.m_Y = y;
        this.m_W = w;
        this.m_H = h;
        this.m_MaxVal = val;
    }

    @Override
    public String toString() {
        return cvRect(m_X, m_Y, m_W, m_H).toString() + ": " + m_MaxVal;
    }
}
