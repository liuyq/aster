/*
 * IMatcher.java
 */
package org.zeroxlab.owl;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

public interface IMatcher {
    /* Find needle in a haystack */
    double min_similarity = 0.90;
    public MatchResult find(IplImage haystack, IplImage needle)
        throws TemplateNotFoundException;
}
