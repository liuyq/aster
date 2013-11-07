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

package org.zeroxlab.wookieerunner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.imageio.ImageIO;

import org.zeroxlab.owl.Finder;
import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.PyramidTemplateMatcher;
import org.zeroxlab.owl.TemplateNotFoundException;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.TouchPressType;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;

public class WookieeAPI {
    private IChimpDevice impl;
    private IMatcher matcher;
    public WookieeAPI(IChimpDevice impl) {
        this.impl = impl;
        this.matcher = new PyramidTemplateMatcher();
        /*
         * Math.min( Integer.parseInt(getProperty("display.width")),
         * Integer.parseInt(getProperty("display.height")) );
         */
    }

    private String getCurrentSnapshot(boolean landscape) {
        IChimpImage image = impl.takeSnapshot();
        String tmpdir = System.getProperty("java.io.tmpdir");
        File output = new File(tmpdir, "owl.png");

        if (landscape) {
            BufferedImage bimage = image.createBufferedImage();
            bimage = ImageUtils.rotate(bimage);
            try {
                ImageIO.write(bimage, "png", output);
            } catch(IOException e) {
                System.err.printf("Fatal error: %s", e.toString());
            }
        } else {
            image.writeToFile(output.getAbsolutePath(), "png");
        }
        return output.getAbsolutePath();
    }

    public IChimpDevice getImpl() {
        return impl;
    }

    public IChimpImage takeSnapshot() {
        return impl.takeSnapshot();
    }

    public HierarchyViewer getHierarchyViewer() {
        return impl.getHierarchyViewer();
    }

    public String getProperty(String key) {
        return impl.getProperty(key);
    }

    public String getSystem(String key) {
        return impl.getProperty(key);
    }

    public void touch(int x, int y, String typestr) {
        TouchPressType type = TouchPressType.fromIdentifier(typestr);
        if (type == null)
            type = TouchPressType.DOWN_AND_UP;
        impl.touch(x, y, type);
    }

    public void touch(String name, String typestr, double timeout,
                      double similarity, boolean landscape)
        throws FileNotFoundException, TemplateNotFoundException {
        long st = System.nanoTime();
        MatchResult r = new MatchResult();
        while (true) {
            try {
                r = Finder.dispatch(matcher, getCurrentSnapshot(landscape),
                                    name, similarity);
            } catch (TemplateNotFoundException e) {
                if (((System.nanoTime() - st) / 1000000000.0) >= timeout)
                    throw e;
                continue;
            }
            break;
        }

        touch(r.cx(), r.cy(), typestr);
    }

    public void drag(int x0, int y0, int x1, int y1, int steps, double sec) {
        long ms = (long) (sec * 1000.0);
        impl.drag(x0, y0, x1, y1, steps, ms);
    }

    public void drag(String start_img, int dx, int dy, int steps, double sec,
                     double timeout, double similarity, boolean landscape)
        throws FileNotFoundException, TemplateNotFoundException {
        MatchResult rs = new MatchResult();
        String current;
        long st = System.nanoTime();
        while (true) {
            try {
                current = getCurrentSnapshot(landscape);
                rs = Finder.dispatch(matcher, current, start_img, similarity);
            } catch (TemplateNotFoundException e) {
                if (((System.nanoTime() - st) / 1000000000.0) >= timeout)
                    throw e;
                continue;
            }
            break;
        }
        drag(rs.cx(), rs.cy(), rs.cx() + dx, rs.cy() + dy, steps, sec);
    }

    public void drag(String start_img, String end_img, int steps, double sec,
                     double timeout, double similarity, boolean landscape)
        throws FileNotFoundException, TemplateNotFoundException {
        MatchResult rs = new MatchResult();
        MatchResult re = new MatchResult();
        String current;
        long st = System.nanoTime();

        while (true) {
            try {
                current = getCurrentSnapshot(landscape);
                rs = Finder.dispatch(matcher, current, start_img, similarity);
                re = Finder.dispatch(matcher, current, end_img, similarity);
            } catch (TemplateNotFoundException e) {
                if (((System.nanoTime() - st) / 1000000000.0) >= timeout)
                    throw e;
                continue;
            }
            break;
        }
        drag(rs.cx(), rs.cy(), re.cx(), rs.cy(), steps, sec);
    }

    public void press(String name, String typestr) {
        TouchPressType type = TouchPressType.fromIdentifier(typestr);
        if (type == null)
            type = TouchPressType.DOWN_AND_UP;
        impl.press(name, type);
    }

    public void type(String text) {
        impl.type(text);
    }

    public void wait(String name, double timeout, double similarity,
                        boolean landscape)
        throws FileNotFoundException, TemplateNotFoundException {
        long st = System.nanoTime();
        MatchResult r = new MatchResult();
        while (true) {
            try {
                r = Finder.dispatch(matcher, getCurrentSnapshot(landscape),
                                    name, similarity);
            } catch (TemplateNotFoundException e) {
                if (((System.nanoTime() - st) / 1000000000.0) >= timeout)
                    throw e;
                continue;
            }
            break;
        }
    }

    public void shell(String cmd) {
        impl.shell(cmd);
    }

    public void reboot(String into) {
        impl.reboot(into);
    }

    public void push(String local, String remote) {
        impl.pushFile(local, remote);
    }

    public void pull(String remote, String local) {
        impl.pullFile(remote, local);
    }

    public boolean installPackage(String path) {
        return impl.installPackage(path);
    }

    public boolean removePackage(String name) {
        return impl.removePackage(name);
    }

    public void startActivity(String uri, String action, String data,
                              String mimetype, Collection<String> categories,
                              Map<String, Object> extras,
                              String component, int flags) {
        impl.startActivity(uri, action, data, mimetype, categories,
                             extras, component, flags);
    }

    public void broadcastIntent(String uri, String action, String data,
                                String mimetype, Collection<String> categories,
                                Map<String, Object> extras,
                                String component, int flags) {
        impl.broadcastIntent(uri, action, data, mimetype, categories,
                               extras, component, flags);
    }

    public Map<String, Object> instrument(String class_name,
                                          Map<String, Object> args) {
        return impl.instrument(class_name, args);
    }

    public void wake() {
        impl.wake();
    }
}
