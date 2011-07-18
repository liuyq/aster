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
    public IplImage source;
    public IplImage target;

    public PyramidTemplateMatcherLayer(IplImage source, IplImage target) {
        this.source = source;
        this.target = target;
    }
}

/**
 * A coarse-to-find pyramid template matcher
 */
public class PyramidTemplateMatcher implements IMatcher {
    private ArrayList<MatchResult> results;
    private ArrayList<PyramidTemplateMatcherLayer> layers;
    private PlainTemplateMatcher plainmatcher;
    private double factor;
    private int levels;

    public PyramidTemplateMatcher(double scale, int levels) {
        this.factor = 2.0;
        this.levels = levels;
        this.plainmatcher = new PlainTemplateMatcher();
        this.results = new ArrayList<MatchResult>();
        this.layers = new ArrayList<PyramidTemplateMatcherLayer>();
    }

    @Override
    public MatchResult find(IplImage haystack, IplImage needle) {
        initPyramid(haystack, needle);
        return findImpl();
    }

    private void initPyramid(IplImage source, IplImage target) {
        layers.add(new PyramidTemplateMatcherLayer(source, target));
        PyramidTemplateMatcherLayer last = layers.get(0);

        for (int i = 0; i < levels; ++i) {
            last = layers.get(i);
            IplImage src = IplImage.create(last.source.width() / 2,
                                           last.source.height() / 2,
                                           IPL_DEPTH_8U, 3);
            IplImage tgt = IplImage.create(last.target.width() / 2,
                                           last.target.height() / 2,
                                           IPL_DEPTH_8U, 3);

            cvPyrDown(last.source, src, CV_GAUSSIAN_5x5);
            cvPyrDown(last.target, tgt, CV_GAUSSIAN_5x5);

            System.out.println(cvGetSize(last.target));
            layers.add(new PyramidTemplateMatcherLayer(src, tgt));
        }
    }

    private MatchResult findImpl() {
        PyramidTemplateMatcherLayer layer = layers.get(levels - 1);
        MatchResult match = plainmatcher.find(layer.source, layer.target);

        for (int i = levels - 2; i >= 0; --i) {
            int scale = (int)factor;
            int x = match.x * scale;
            int y = match.y * scale;

            layer = layers.get(i);
            int x0 = Math.max(x - scale * 3, 0);
            int y0 = Math.max(y - scale * 3, 0);
            int x1 = Math.min(x + layer.target.width() + scale * 3,
                              layer.source.width());
            int y1 = Math.min(y + layer.target.height() + scale * 3,
                              layer.source.height());

            CvRect roi = cvRect(x0, y0, x1 - x0, y1 - y0);

            cvSetImageROI(layer.source, roi);
            IplImage n_src = cvCreateImage(cvGetSize(layer.source),
                                           layer.source.depth(),
                                           layer.source.nChannels());
            cvCopy(layer.source, n_src, null);
            layer.source = n_src;

            match = plainmatcher.find(layer.source, layer.target);
            match.x += x0;
            match.y += y0;
        }
        return match;
    }
}
