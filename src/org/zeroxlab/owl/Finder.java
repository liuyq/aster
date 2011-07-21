/*
 * Finder.java
 */
package org.zeroxlab.owl;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.io.FileNotFoundException;

public class Finder {
    public static MatchResult dispatch(IMatcher matcher,
                                       String haystack,
                                       String needle)
        throws FileNotFoundException, TemplateNotFoundException {
        IplImage img = cvLoadImage(haystack);
        IplImage tmpl = cvLoadImage(needle);

        if (img == null)
            throw new FileNotFoundException("can't open `" + haystack +"'");
        else if (tmpl == null)
            throw new FileNotFoundException("can't open `" + needle +"'");
        long start = System.nanoTime();
        MatchResult result = matcher.find(img, tmpl);
        System.out.println(result);
        System.out.printf("%f\n", (System.nanoTime() - start) / 1000000.0);
        return result;
    }
};
