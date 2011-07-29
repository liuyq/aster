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

import org.zeroxlab.owl.Finder;
import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.PyramidTemplateMatcher;
import org.zeroxlab.owl.TemplateNotFoundException;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.ChimpImageBase;
import com.android.chimpchat.core.TouchPressType;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;

import java.io.FileNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public class WookieeAPI {
    private IChimpDevice impl;
    private IMatcher matcher;
    private int width;
    private int height;

    public WookieeAPI(IChimpDevice impl) {
        this.impl = impl;
        this.matcher = new PyramidTemplateMatcher();
        this.width = Integer.parseInt(getProperty("display.width"));
        this.height = Integer.parseInt(getProperty("display.height"));
    }

    private String getCurrentSnapshot() {
        IChimpImage image = impl.takeSnapshot();
        image.writeToFile("/tmp/owl.png", "png");
        return "/tmp/owl.png";
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

    public boolean isLandScape() {
        // To be implement
        return false;
    }

    public void touch(int x, int y, String typestr) {
        TouchPressType type = TouchPressType.fromIdentifier(typestr);
        if (type == null)
            type = TouchPressType.DOWN_AND_UP;
        if (isLandScape()) {
            int tmp = height - x;
            x = y;
            y = tmp;
        }
        impl.touch(x, y, type);
    }

    public void touch(String name, String typestr, double timeout)
        throws FileNotFoundException, TemplateNotFoundException {
        long st = System.nanoTime();
        MatchResult r = new MatchResult();
        while (true) {
            try {
                r = Finder.dispatch(matcher, getCurrentSnapshot(), name);
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
        if (isLandScape()) {
            int tmp = height - x0;
            x0 = y0;
            y0 = tmp;

            tmp = height - x1;
            x1 = y1;
            y1 = x1;
        }
        impl.drag(x0, y0, x1, y1, steps, ms);
    }

    public void drag(String start_img, String end_img, int steps, double sec,
                     double timeout)
        throws FileNotFoundException, TemplateNotFoundException {
        MatchResult rs = new MatchResult();
        MatchResult re = new MatchResult();
        String current;
        long st = System.nanoTime();

        while (true) {
            try {
                current = getCurrentSnapshot();
                rs = Finder.dispatch(matcher, current, start_img);
                re = Finder.dispatch(matcher, current, end_img);
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
