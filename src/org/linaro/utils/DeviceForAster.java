package org.linaro.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.linaro.utils.RuntimeWrapper.RuntimeResult;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zeroxlab.owl.Finder;
import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.PyramidTemplateMatcher;
import org.zeroxlab.owl.TemplateNotFoundException;
import org.zeroxlab.wookieerunner.ImageUtils;

import com.android.ddmlib.Log;
import com.android.ddmlib.RawImage;

public abstract class DeviceForAster {

    private String serial = null;
    private static DeviceForAster instance = null;

    protected abstract ArrayList<String> getAdbSerialArrayList();

    public String getScreenShotPath() {
        return getScreenShot(getPathWithSerial("aster_screencap", "png"));
    }

    private String getScreenShot(String png_host_path) {
        File screenShot = new File(png_host_path);
        if (screenShot.exists()) {
            screenShot.delete();
        }
        executeAdbShell(String.format("screencap >%s",
                Constants.SCREENCAP_RAW_DEVICE));
        String rawHostPath = getPathWithSerial("aster_screencap", "raw");
        this.pull(rawHostPath, Constants.SCREENCAP_RAW_DEVICE);
        try {
            boolean result = this.convertRaw2Png(rawHostPath, png_host_path);
            if (!result) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return png_host_path;
    }

    private boolean convertRaw2Png(String rawFilePath, String pngFilePath)
            throws IOException {
        RawImage rawImage = new RawImage();
        File rawDataFile = new File(rawFilePath);
        FileInputStream rawFileInputStream = new FileInputStream(rawDataFile);

        byte[] whf = new byte[12];
        rawFileInputStream.read(whf);
        ByteBuffer buf = ByteBuffer.wrap(whf);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int w = buf.getInt();
        int h = buf.getInt();
        int format = buf.getInt();

        int version = Constants.DDMS_RAWIMAGE_VERSION;
        int headerSize = RawImage.getHeaderSize(version);

        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize * 4);
        FBInfo fbinfo = generateFBInfoBasedOnImgInfo(w, h, format);
        if (fbinfo == null) {
            rawFileInputStream.close();
            return false;
        }
        fbinfo.putToByteBuffer(headerBuf);

        // fill the RawImage with the header
        if (rawImage.readHeader(version, headerBuf) == false) {
            Log.e("Screenshot", "Unsupported version: " + version);
            rawFileInputStream.close();
            return false;
        }

        byte[] imageData = new byte[rawImage.size];
        rawFileInputStream.read(imageData);
        rawImage.data = imageData;
        rawFileInputStream.close();
        BufferedImage bufferedImage = new BufferedImage(rawImage.width,
                rawImage.height, BufferedImage.TYPE_INT_ARGB);
        int index = 0;
        int IndexInc = rawImage.bpp >> 3;
        for (int y = 0; y < rawImage.height; y++) {
            for (int x = 0; x < rawImage.width; x++) {
                int value = rawImage.getARGB(index);
                index += IndexInc;
                bufferedImage.setRGB(x, y, value);
            }
        }
        File file = new File(pngFilePath);
        file.delete();
        ImageIO.write(bufferedImage, "png", file);
        return true;
    }

    class FBInfo {
        int bpp;
        int size;
        int width;
        int height;
        int red_offset;
        int red_length;
        int green_offset;
        int green_length;
        int blue_offset;
        int blue_length;
        int alpha_offset;
        int alpha_length;

        public void putToByteBuffer(ByteBuffer headerBuf) {
            headerBuf.putInt(this.bpp);
            headerBuf.putInt(this.size);
            headerBuf.putInt(this.width);
            headerBuf.putInt(this.height);
            headerBuf.putInt(this.red_offset);
            headerBuf.putInt(this.red_length);
            headerBuf.putInt(this.blue_offset);
            headerBuf.putInt(this.blue_length);
            headerBuf.putInt(this.green_offset);
            headerBuf.putInt(this.green_length);
            headerBuf.putInt(this.alpha_offset);
            headerBuf.putInt(this.alpha_length);
            headerBuf.flip();
        }
    }

    private FBInfo generateFBInfoBasedOnImgInfo(int w, int h, int format) {
        FBInfo fbinfo = new FBInfo();
        if (format == 1) { /* RGBA_8888 */
            fbinfo.bpp = 32;
            fbinfo.size = w * h * 4;
            fbinfo.width = w;
            fbinfo.height = h;
            fbinfo.red_offset = 0;
            fbinfo.red_length = 8;
            fbinfo.green_offset = 8;
            fbinfo.green_length = 8;
            fbinfo.blue_offset = 16;
            fbinfo.blue_length = 8;
            fbinfo.alpha_offset = 24;
            fbinfo.alpha_length = 8;
        } else if (format == 2) { /* RGBX_8888 */
            fbinfo.bpp = 32;
            fbinfo.size = w * h * 4;
            fbinfo.width = w;
            fbinfo.height = h;
            fbinfo.red_offset = 0;
            fbinfo.red_length = 8;
            fbinfo.green_offset = 8;
            fbinfo.green_length = 8;
            fbinfo.blue_offset = 16;
            fbinfo.blue_length = 8;
            fbinfo.alpha_offset = 24;
            fbinfo.alpha_length = 0;
        } else if (format == 3) { /* RGB_888 */
            fbinfo.bpp = 24;
            fbinfo.size = w * h * 3;
            fbinfo.width = w;
            fbinfo.height = h;
            fbinfo.red_offset = 0;
            fbinfo.red_length = 8;
            fbinfo.green_offset = 8;
            fbinfo.green_length = 8;
            fbinfo.blue_offset = 16;
            fbinfo.blue_length = 8;
            fbinfo.alpha_offset = 24;
            fbinfo.alpha_length = 0;
        } else if (format == 4) { /* RGB_565 */
            fbinfo.bpp = 16;
            fbinfo.size = w * h * 2;
            fbinfo.width = w;
            fbinfo.height = h;
            fbinfo.red_offset = 11;
            fbinfo.red_length = 5;
            fbinfo.green_offset = 5;
            fbinfo.green_length = 6;
            fbinfo.blue_offset = 0;
            fbinfo.blue_length = 5;
            fbinfo.alpha_offset = 0;
            fbinfo.alpha_length = 0;
        } else if (format == 5) { /* BGRA_8888 */
            fbinfo.bpp = 32;
            fbinfo.size = w * h * 4;
            fbinfo.width = w;
            fbinfo.height = h;
            fbinfo.red_offset = 16;
            fbinfo.red_length = 8;
            fbinfo.green_offset = 8;
            fbinfo.green_length = 8;
            fbinfo.blue_offset = 0;
            fbinfo.blue_length = 8;
            fbinfo.alpha_offset = 24;
            fbinfo.alpha_length = 8;
        } else {
            Log.e("Screenshot", "Unsupported format: " + format);
            return null;
        }
        return fbinfo;
    }

    private String getPathWithSerial(String base, String ext) {
        if (serial != null && !serial.isEmpty()) {
            return String.format("%s/%s_%s.%s", Constants.SCR_DIR_HOST, base,
                    serial.replaceAll(":|\\.", "_"), ext);
        }
        return String.format("%s/%s.%s", Constants.SCR_DIR_HOST, base, ext);
    }

    public abstract void installApk(String apkFilePath);

    public abstract void push(String filePathHost);

    public abstract void pull(String filePathHost, String fileDevicePath);

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
        executeAdbShell("input", "text", text);
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
                    Constants.DEFAULT_ROTATE_ORDER,
                    getPathWithSerial("aster_screencap_rotated", "png"));
            return findImage(targetImage, newBaseImage,
                    Constants.DEFAULT_SIMILARITY);
        }
    }

    public ArrayList<String> dumpXMLLayout() {
        ArrayList<String> nodes = new ArrayList<String>();

        NodeList nl = getAllNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                NamedNodeMap attributes = nl.item(i).getAttributes();
                if (null != attributes) {
                    String classAttr = attributes.getNamedItem("class")
                            .getNodeValue();
                    String resId = attributes.getNamedItem("resource-id")
                            .getNodeValue();
                    String text = attributes.getNamedItem("text")
                            .getNodeValue();
                    String contentDesc = attributes
                            .getNamedItem("content-desc").getNodeValue();
                    String bounds = attributes.getNamedItem("bounds")
                            .getNodeValue();

                    nodes.add(String
                            .format("class=%s, resource-id=%s, text=%s, content-desc=%s,bounds=%s",
                                    classAttr, resId, text, contentDesc, bounds));
                }
            }
        }
        return nodes;
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
                    Constants.DEFAULT_ROTATE_ORDER,
                    getPathWithSerial("aster_screencap_rotated", "png"));
            if (newBaseImage != null) {
                return waitImage(targetImage, newBaseImage);
            }
        }
        return null;
    }

    public NodeList getAllNodes() {
        String hostPath = getPathWithSerial("uiautomator-dump-compressed",
                "xml");
        File f = new File(hostPath);
        if (f.exists())
            f.delete();
        executeAdbShell("uiautomator", "dump", "--compressed",
                Constants.XML_LAYOUT_FILE_DEVICE_PATH);
        pull(hostPath, Constants.XML_LAYOUT_FILE_DEVICE_PATH);

        f = new File(hostPath);
        if (!f.exists()) {
            return null;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);
            return doc.getElementsByTagName("node");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
