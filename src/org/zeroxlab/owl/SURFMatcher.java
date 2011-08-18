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

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.ObjectFinder;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class SURFMatcher implements IMatcher {
    @Override
    public MatchResult find(IplImage haystack, IplImage needle, double similarity)
        throws TemplateNotFoundException {
        IplImage img = IplImage.create(haystack.width(), haystack.height(),
                                        IPL_DEPTH_8U, 1);
        IplImage tmpl = IplImage.create(needle.width(), needle.height(),
                                        IPL_DEPTH_8U, 1);
        cvCvtColor(haystack, img, CV_RGB2GRAY);
        cvCvtColor(needle, tmpl, CV_RGB2GRAY);

        try {
            ObjectFinder finder = new ObjectFinder(tmpl);
            double[] results = finder.find(img);
            return new MatchResult((int)results[0],
                                   (int)results[1],
                                   (int)(results[2] - results[0]),
                                   (int)(results[3] - results[1]), 1.0);
        } catch(Exception e) {
            throw new TemplateNotFoundException();
        }
    }
}
