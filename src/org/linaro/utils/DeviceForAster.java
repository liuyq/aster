package org.linaro.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.zeroxlab.owl.Finder;
import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.PyramidTemplateMatcher;
import org.zeroxlab.owl.TemplateNotFoundException;

public abstract class DeviceForAster {

    String serial = null;

    public DeviceForAster(String serial) {
        this.serial = serial;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    protected abstract ArrayList<String> getAdbSerialArrayList();

    public abstract void executeAdbShell(String... cmds);

    public abstract void executeAdbCommands(String... cmds);

    public BufferedImage getScreenshotBufferedImage() {
        File screencap_file = new File(getScreenShotPath());
        BufferedImage image = null;
        if (screencap_file.exists() && screencap_file.canRead()) {
            try {
                image = ImageIO.read(screencap_file);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return image;
    }

    public abstract String getScreenShotPath();

    public void connect(String serial) {
        if (serial == null || serial.isEmpty()){
            return;
        }
        this.serial = null;
        executeAdbCommands("connect", serial);
        this.serial = serial;
    }

    public void disconnect(String serial) {
        this.serial = null;
        executeAdbCommands("disconnect", serial);
    }

    public void touch(int x, int y) {
        executeAdbShell("input", "tap", "" + x, "" + y);
    }

    public void drag(int x1, int y1, int x2, int y2) {
        executeAdbShell("input", "swipe", "" + x1, "" + y1, "" + x2, "" + y2);
    }

    public void press(String keycodeName) {
        executeAdbShell("input", "keyevent", keycodeName);
    }

    public void inputText(String text) {
        executeAdbShell("input", "text", text);
    }

    public MatchResult findImage(String targetImage, String baseImage,
            Double similarity) {
        IMatcher matcher = new PyramidTemplateMatcher();
        MatchResult r = new MatchResult();
        try {
            r = Finder.dispatch(matcher, baseImage, targetImage, similarity);
            return r;
        } catch (TemplateNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public MatchResult waitImage(String targetImage, String baseImage) {
        Double similarity = Contants.CMP_SIMILIARITY;
        return findImage(targetImage, baseImage, similarity);
    }

    public MatchResult waitImageUntil(String targetImage, String baseImage,
            double timeoutInSecond) {
        long st = System.nanoTime();
        while (true) {
            MatchResult matchResult = waitImage(targetImage, baseImage);
            if (matchResult == null) {
                if (((System.nanoTime() - st) / 1000000000.0) >= timeoutInSecond) {
                    return null;
                }
                continue;
            } else {
                return matchResult;
            }
        }
    }
}
