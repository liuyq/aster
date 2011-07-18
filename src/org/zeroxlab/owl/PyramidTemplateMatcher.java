/*
 * PyramidTemplateMatcher.java
 */
package org.zeroxlab.owl;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.util.ArrayList;
import java.lang.Math;

class PyramidTemplateMatcherLayer {
    public IplImage m_Source;
    public IplImage m_Target;

    public PyramidTemplateMatcherLayer(IplImage source, IplImage target) {
        this.m_Source = source;
        this.m_Target = target;
    }
}

/**
 * A coarse-to-find pyramid template matcher
 */
public class PyramidTemplateMatcher implements IMatcher {
    private ArrayList<MatchResult> m_Results;
    private ArrayList<PyramidTemplateMatcherLayer> m_Layers;
    private PlainTemplateMatcher m_PlainMatcher;
    private double m_Scale;
    private int m_Levels;

    public PyramidTemplateMatcher(double scale, int levels) {
        this.m_Scale = 2.0;
        this.m_Levels = levels;
        this.m_PlainMatcher = new PlainTemplateMatcher();
        this.m_Results = new ArrayList<MatchResult>();
        this.m_Layers = new ArrayList<PyramidTemplateMatcherLayer>();
    }

    @Override
    public MatchResult find(IplImage haystack, IplImage needle) {
        initPyramid(haystack, needle);
        return findImpl();
    }

    private void initPyramid(IplImage source, IplImage target) {
        m_Layers.add(new PyramidTemplateMatcherLayer(source, target));
        PyramidTemplateMatcherLayer last = m_Layers.get(0);

        for (int i = 0; i < m_Levels; ++i) {
            last = m_Layers.get(i);
            IplImage src = IplImage.create(last.m_Source.width() / 2,
                                           last.m_Source.height() / 2,
                                           IPL_DEPTH_8U, 3);
            IplImage tgt = IplImage.create(last.m_Target.width() / 2,
                                           last.m_Target.height() / 2,
                                           IPL_DEPTH_8U, 3);

            cvPyrDown(last.m_Source, src, CV_GAUSSIAN_5x5);
            cvPyrDown(last.m_Target, tgt, CV_GAUSSIAN_5x5);

            System.out.println(cvGetSize(last.m_Target));
            m_Layers.add(new PyramidTemplateMatcherLayer(src, tgt));
        }
    }

    private MatchResult findImpl() {
        PyramidTemplateMatcherLayer layer = m_Layers.get(m_Levels - 1);
        MatchResult match = m_PlainMatcher.find(layer.m_Source, layer.m_Target);

        for (int i = m_Levels - 2; i >= 0; --i) {
            int scale = (int)m_Scale;
            int x = match.m_X * scale;
            int y = match.m_Y * scale;

            layer = m_Layers.get(i);
            int x0 = Math.max(x - scale * 3, 0);
            int y0 = Math.max(y - scale * 3, 0);
            int x1 = Math.min(x + layer.m_Target.width() + scale * 3,
                              layer.m_Source.width());
            int y1 = Math.min(y + layer.m_Target.height() + scale * 3,
                              layer.m_Source.height());

            CvRect roi = cvRect(x0, y0, x1 - x0, y1 - y0);

            cvSetImageROI(layer.m_Source, roi);
            IplImage n_src = cvCreateImage(cvGetSize(layer.m_Source),
                                           layer.m_Source.depth(),
                                           layer.m_Source.nChannels());
            cvCopy(layer.m_Source, n_src, null);
            layer.m_Source = n_src;

            match = m_PlainMatcher.find(layer.m_Source, layer.m_Target);
            match.m_X += x0;
            match.m_Y += y0;

            System.out.println(match);
        }
        return match;
    }
}
