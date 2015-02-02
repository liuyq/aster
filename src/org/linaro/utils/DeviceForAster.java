package org.linaro.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.linaro.utils.RuntimeWrapper.RuntimeResult;
import org.zeroxlab.owl.Finder;
import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.PyramidTemplateMatcher;
import org.zeroxlab.owl.TemplateNotFoundException;
import org.zeroxlab.wookieerunner.ImageUtils;

public abstract class DeviceForAster {

    private String serial = null;
    private static DeviceForAster instance = null;

    protected abstract ArrayList<String> getAdbSerialArrayList();

    public abstract String getScreenShotPath();

    protected DeviceForAster(String serial) {
        this.serial = serial;
    }

    public String getSerial() {
        return serial;
    }

    // public abstract boolean isConnected();
    public static void initialize(String adbType, String serial)
            throws Exception {
        if (adbType == null || Constants.ADB_TYPE_LOCAL.equals(adbType)) {
            instance = new LocalAdb(serial);
            return;
        } else if (adbType.equals(Constants.ADB_TYPE_SSH)) {
            instance = new SshAdb(serial, Constants.SSH_ADB_HOST);
            return;
        } else if (adbType.equals(Constants.ADB_TYPE_MONKEYRUNNER)) {
            throw new Exception("Monkeyrunner still not implemented yet");
        }
        throw new Exception("Not supported ADB Type:" + adbType);
    }

    public static DeviceForAster getInstance() throws Exception {
        if (instance != null) {
            return instance;
        }
        throw new Exception(
                "The DeviceForAster.initialize(String adbType, String serial) should be call at least once!");
    }

    private void stayon() {
        executeAdbShell("svc", "power", "stayon", "true");
    }

    public DeviceForAster connect() {
        if (serial != null && !serial.isEmpty()) {
            executeAdbCommands("connect", serial);
        }
        stayon();
        return instance;
    }

    public void disconnect(String serial) {
        if (serial != null && !serial.isEmpty()) {
            executeAdbCommands("disconnect", serial);
        }
    }

    public BufferedImage getScreenshotBufferedImage(boolean isRotated) {
        BufferedImage image = ImageUtils.getBufferedImage(getScreenShotPath());
        if (image != null && isRotated) {
            // By default, a NOT-ROTATED-Image should be Portrait
            image = ImageUtils.rotate(image);
        }
        return image;
    }

    public RuntimeResult executeAdbShell(String... cmds) {
        String[] shell = new String[] { "shell" };
        return executeAdbCommands(RuntimeWrapper.merge2Strings(shell, cmds));
    }

    public RuntimeResult executeAdbCommands(String... cmds) {
        ArrayList<String> cmdArray = getAdbSerialArrayList();
        return RuntimeWrapper.executeCommand(RuntimeWrapper.merge2Strings(
                cmdArray, cmds));
    }

    public BufferedReader monitorAdbCmdsOutput(String... cmds) {
        ArrayList<String> cmdArray = getAdbSerialArrayList();
        return RuntimeWrapper.monitorOutput(RuntimeWrapper.merge2Strings(
                cmdArray, cmds));
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
        // TODO: need to normalize the text here more
        // Now only use single quote to do the normalization simply
        executeAdbShell("input", "text", "'" + text + "'");
    }

    public MatchResult findImage(String targetImage, String baseImage,
            Double similarity) {
        IMatcher matcher = new PyramidTemplateMatcher();
        try {
            return Finder.dispatch(matcher, baseImage, targetImage, similarity);
        } catch (TemplateNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MatchResult waitImage(String targetImage, String baseImage) {
        return findImage(targetImage, baseImage, Constants.DEFAULT_SIMILARITY);
    }

    public MatchResult waitImage(String targetImage, String baseImage,
            boolean rotated) {
        if (!rotated) {
            return findImage(targetImage, baseImage,
                    Constants.DEFAULT_SIMILARITY);
        } else {
            String newBaseImage = ImageUtils.rotateImage(baseImage,
                    Constants.DEFAULT_ROTATE_ORDER);
            return findImage(targetImage, newBaseImage,
                    Constants.DEFAULT_SIMILARITY);
        }

    }

    public MatchResult waitImageUntil(String targetImage, String baseImage,
            double timeoutInSecond, boolean rotated) {
        long st = System.nanoTime();
        while (true) {
            MatchResult matchResult = waitImage(targetImage, baseImage, rotated);
            if (matchResult == null) {
                if (((System.nanoTime() - st) / 1000000000.0) >= timeoutInSecond) {
                    if (Constants.DEBUG_IMAGE_MATCH) {
                        String str = String
                                .format("Can't not find the targetImage(%s)"
                                        + "in baseImage(%s) within timeout of %d",
                                        targetImage, baseImage, timeoutInSecond);
                        System.out.println(str);
                    }
                    return null;
                }
                continue;
            } else {
                if (Constants.DEBUG_IMAGE_MATCH) {
                    String str = String.format("Image match result(%d, %d)",
                            matchResult.cx(), matchResult.cy());
                    System.out.println(str);
                }
                return matchResult;
            }
        }
    }

    public MatchResult waitImageUntil(String targetImage, String baseImage) {
        return waitImageUntil(targetImage, baseImage,
                Constants.DEFAULT_TIMEOUT, false);
    }

    public MatchResult waitImageUntil(String targetImage, String baseImage,
            boolean rotated) {
        if (!rotated) {
            return waitImageUntil(targetImage, baseImage);
        } else {
            String newBaseImage = ImageUtils.rotateImage(baseImage,
                    Constants.DEFAULT_ROTATE_ORDER);
            if (newBaseImage != null) {
                return waitImage(targetImage, newBaseImage);
            }
        }
        return null;
    }
}
