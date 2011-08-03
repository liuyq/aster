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

import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import com.googlecode.javacv.ObjectFinder;

import java.io.FileNotFoundException;

class TestResult {
    public String name;
    public double duration;
    public MatchResult result;

    @Override
    public String toString() {
        return String.format("%fms, %s", duration, result);
    }
}

public class Owl {
    public static void main(String [] args) {
        doTest();
    }

    private static void doTest() {
        performTest(new PlainTemplateMatcher());
        performTest(new PyramidTemplateMatcher());
        performTest(new SURFMatcher());
    }

    private static void performTest(IMatcher matcher) {
        long start = 0, end = 0;
        TestResult result = new TestResult();
        MatchResult r = new MatchResult();

        try {
            start = System.nanoTime();
            r = Finder.dispatch(matcher, "test-images/Haystack.png",
                                         "test-images/Needle.png");
            end = System.nanoTime();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            result.name = matcher.getClass().getName();
            result.result = r;
            result.duration = (int)(end - start) / 1000000.0;
        }

        System.out.println(result);
        System.out.println("-------------------------------------------");
    }
}
