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

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvMinMaxLoc;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_TM_SQDIFF_NORMED;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMatchTemplate;
import static java.lang.Math.abs;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class PlainTemplateMatcher implements IMatcher {
    @Override
    public MatchResult find(IplImage haystack, IplImage needle, double similarity)
        throws TemplateNotFoundException {
        CvSize rsize = cvSize(abs(haystack.width() - needle.width()) + 1,
                              abs(haystack.height() - needle.height()) + 1);

        IplImage result = cvCreateImage(rsize, IPL_DEPTH_32F, 1);
        cvMatchTemplate(haystack, needle, result, CV_TM_SQDIFF_NORMED);

        double[] min = new double[1];
        double[] max = new double[1];
        CvPoint min_pos = new CvPoint();
        CvPoint max_pos = new CvPoint();
        cvMinMaxLoc(result, min, max, min_pos, max_pos, null);

        if (1.0 - min[0] < similarity)
            throw new TemplateNotFoundException();

        return new MatchResult(min_pos.x(), min_pos.y(),
                               needle.width(), needle.height(), 1.0 - min[0]);
    }
}
