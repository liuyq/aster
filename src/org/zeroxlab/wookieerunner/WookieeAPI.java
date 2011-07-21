/*
 * WookieeAPI.java
 */
package org.zeroxlab.wookieerunner;

import org.zeroxlab.owl.Finder;
import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.PyramidTemplateMatcher;
import org.zeroxlab.owl.TemplateNotFoundException;

import com.android.chimpchat.ChimpChat;
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
    private static ChimpChat chimpchat;

    public WookieeAPI(long timeout, String id) {
        chimpchat = ChimpChat.getInstance();
        impl = chimpchat.waitForConnection(timeout, id);
        matcher = new PyramidTemplateMatcher();
    }

    private String getCurrentSnapshot() {
        IChimpImage image = impl.takeSnapshot();
        image.writeToFile("/tmp/owl.png", "png");
        return "/tmp/owl.png";
    }

    public IChimpDevice getImpl() {
        return impl;
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

    public void touch(String name, String typestr, double timeout)
        throws FileNotFoundException, TemplateNotFoundException {
        TouchPressType type = TouchPressType.fromIdentifier(typestr);
        if (type == null)
            type = TouchPressType.DOWN_AND_UP;

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
        impl.touch(r.cx(), r.cy(), type);
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
        impl.drag(rs.cx(), rs.cy(), re.cx(), rs.cy(), steps, (long)sec*1000);
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
