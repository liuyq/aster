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
        return String.format("%s:\n%fms, %s", name, duration, result);
    }
}

public class Owl {
    public static void main(String [] args) {
        doTest();

        //IplImage img = cvLoadImage("Haystack.png");
        //IplImage tmpl = cvLoadImage("Needle.png");

        //IplImage imgp = IplImage.create(img.width(), img.height(),
        //                                IPL_DEPTH_8U, 1);
        //IplImage tmplp = IplImage.create(tmpl.width(), tmpl.height(),
        //                                IPL_DEPTH_8U, 1);
        //cvCvtColor(img, imgp, CV_RGB2GRAY);
        //cvCvtColor(tmpl, tmplp, CV_RGB2GRAY);

        //try {
        //    ObjectFinder matcher = new ObjectFinder(imgp);
        //    double[] results = matcher.find(tmplp);
        //    for (int i = 0; i < results.length; ++i)
        //        System.out.println(results[i]);
        //} catch (Exception e) {
        //    System.out.println(e);
        //    System.exit(1);
        //}
    }

    private static void doTest() {
        performTest(new PlainTemplateMatcher());
        performTest(new PyramidTemplateMatcher());
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

        System.out.println("-------------------------------------------");
        System.out.println(result);
    }
}
