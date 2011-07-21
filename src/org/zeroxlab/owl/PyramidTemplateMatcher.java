/*
 * PyramidTemplateMatcher.java
 */
package org.zeroxlab.owl;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.lang.Math;
import java.util.ArrayList;

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
    private ArrayList<PyramidTemplateMatcherLayer> layers;
    private PlainTemplateMatcher plainmatcher;
    private double factor;
    private int levels;
    private final int target_min_side = 5;

    public PyramidTemplateMatcher() {
        this.factor = 2.0;
        this.plainmatcher = new PlainTemplateMatcher();
        this.layers = new ArrayList<PyramidTemplateMatcherLayer>();
    }

    @Override
    public MatchResult find(IplImage haystack, IplImage needle)
        throws TemplateNotFoundException {
        MatchResult result;
        int side = Math.min(needle.width(), needle.height());

        /* Calculate number of base levels */
        levels = 0;
        while (side > target_min_side) {
            side /= factor;
            ++levels;
        }
        initPyramid(haystack, needle);

        /* Since too many levels may leads to false match, we set a min value
         * of similarity. If the result similarity is smaller than the value
         * we defined, we restart the matching with one level less. If the
         * levels equals zero and we still can't find the image, an exception
         * is thrown.  */
        while (true) {
            System.out.println("levels: " + levels);
            try {
                result = findImpl();
            } catch (TemplateNotFoundException e) {
                if (--levels == 0)
                    throw new TemplateNotFoundException();
                continue;
            }
            break;
        }
        layers.clear();
        return result;
    }

    private void initPyramid(IplImage source, IplImage target) {
        layers.add(new PyramidTemplateMatcherLayer(source, target));
        PyramidTemplateMatcherLayer last = layers.get(0);

        for (int i = 0; i < levels -1; ++i) {
            last = layers.get(i);
            IplImage src = IplImage.create(last.source.width() / 2,
                                           last.source.height() / 2,
                                           IPL_DEPTH_8U, 3);
            IplImage tgt = IplImage.create(last.target.width() / 2,
                                           last.target.height() / 2,
                                           IPL_DEPTH_8U, 3);

            cvPyrDown(last.source, src, CV_GAUSSIAN_5x5);
            cvPyrDown(last.target, tgt, CV_GAUSSIAN_5x5);

            layers.add(new PyramidTemplateMatcherLayer(src, tgt));
        }
    }

    private MatchResult findImpl() throws TemplateNotFoundException {
        PyramidTemplateMatcherLayer layer = layers.get(levels - 1);
        MatchResult match = plainmatcher.find(layer.source, layer.target);

        for (int i = levels - 2; i >= 0; --i) {
            int div = 10;                  /* Number of divisions */
            int x = (int)(match.x * factor);
            int y = (int)(match.y * factor);

            layer = layers.get(i);
            int xchunk = (int)((double)layer.source.width() / div);
            int ychunk = (int)((double)layer.source.height() / div);
            int x0 = x / xchunk * xchunk;
            int y0 = y / ychunk * ychunk;
            int x1 = ((x + layer.target.width()) / xchunk + 1) * xchunk;
            int y1 = ((y + layer.target.height()) / ychunk + 1) * ychunk;

            CvRect roi = cvRect(x0, y0, x1 - x0, y1 - y0);
            cvSetImageROI(layer.source, roi);
            IplImage n_src = cvCreateImage(cvGetSize(layer.source),
                                           layer.source.depth(),
                                           layer.source.nChannels());
            cvCopy(layer.source, n_src, null);
            cvResetImageROI(layer.source);

            match = plainmatcher.find(n_src, layer.target);
            match.x += x0;
            match.y += y0;
        }

        return match;
    }
}
