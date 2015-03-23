//import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;

import org.zeroxlab.owl.TemplateNotFoundException;

import com.android.ddmlib.Log;
import com.android.ddmlib.RawImage;

interface IServiceManager {
    public int getService(String name);
}

abstract class ServiceManagerNative implements IServiceManager {
    public static void test() {
        System.out.println("ServiceManagerNative.test");
    }
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
}

public class Test {
    /**
     * @param args
     * @throws TemplateNotFoundException
     * @throws FileNotFoundException
     */
    public static final int DDMS_RAWIMAGE_VERSION = 1;

    public static void main(String[] args) throws TemplateNotFoundException,
            IOException {
        String rawFilePath = "/home/liuyq/workspace/aster/images/screencap2.raw";
        RawImage rawImage = new RawImage();
        File rawFile = new File(rawFilePath);
        FileInputStream rawFileInputStream = new FileInputStream(rawFile);

        byte[] whf = new byte[12];
        rawFileInputStream.read(whf);
        ByteBuffer buf = ByteBuffer.wrap(whf);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int w = buf.getInt();
        int h = buf.getInt();
        int format = buf.getInt();

        System.out.println(String.format("w=%d, h=%d, f=%d", w, h, format));
        int version = DDMS_RAWIMAGE_VERSION;
        int headerSize = RawImage.getHeaderSize(version);

        // read the header
        // read(adbChan, reply);
        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize * 4);
        FBInfo fbinfo = new FBInfo();
        if (format == 1){ /* RGBA_8888 */
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
            return;
        }
        headerBuf.putInt(fbinfo.bpp);
        headerBuf.putInt(fbinfo.size);
        headerBuf.putInt(fbinfo.width);
        headerBuf.putInt(fbinfo.height);
        headerBuf.putInt(fbinfo.red_offset);
        headerBuf.putInt(fbinfo.red_length);
        headerBuf.putInt(fbinfo.green_offset);
        headerBuf.putInt(fbinfo.green_length);
        headerBuf.putInt(fbinfo.blue_offset);
        headerBuf.putInt(fbinfo.blue_length);
        headerBuf.putInt(fbinfo.alpha_offset);
        headerBuf.putInt(fbinfo.alpha_length);
        headerBuf.flip();

        // fill the RawImage with the header
        if (rawImage.readHeader(version, headerBuf) == false) {
            Log.e("Screenshot", "Unsupported version: " + version);
            // return null;
        }

        Log.e("ddms", "image params: bpp=" + rawImage.bpp + ", size="
                + rawImage.size + ", width=" + rawImage.width + ", height="
                + rawImage.height);

        byte[] imageData = new byte[rawImage.size];
        rawFileInputStream.read(imageData);
        rawImage.data = imageData;
        rawFileInputStream.close();

        // BufferedImage bufferedImage = ImageUtils.convertImage(rawImage);

        // // convert raw data to an Image
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
        String filePath = "/tmp/screencap-from-rap.png";
        File file = new File(filePath);
        file.delete();
        ImageIO.write(bufferedImage, "png", file);

        // String aaa = "^\\d+$";
        // String bbb = "";
        // if (bbb.matches(aaa)) {
        // System.out.println("Match");
        // } else {
        // System.out.println("Not Match");
        // }

        // String bounds = "[678,1774][851,1915]";
        // bounds = bounds.replaceAll("\\]\\[", ",").replaceAll("\\]|\\[", "");
        // System.out.println(bounds);
        // String[] pointsStr = bounds.split(",");
        // int topLeft_x = Integer.parseInt(pointsStr[0]);
        // int topLeft_y = Integer.parseInt(pointsStr[1]);
        // int bottomRight_x = Integer.parseInt(pointsStr[2]);
        // int bottomRight_y = Integer.parseInt(pointsStr[3]);
        // int centerX = topLeft_x + (bottomRight_x - topLeft_x) / 2;
        // int centerY = topLeft_y + (bottomRight_y - topLeft_y) / 2;
        // System.out.println(String.format("%s,%s", centerX, centerY));
        // ServiceManagerNative.test();
        // break;
        //
        // System.out.println("Started");
        // String haystack = "/home/liuyq/workspace/Aster/classes/logo.png";
        // IplImage img = cvLoadImage(haystack);
        // System.out.println("Finished");
        // if (img == null){
        // System.out.println("haystack=" + haystack );
        // }
        //
        // Properties props = System.getProperties();
        // Iterator<Object> keyIt = props.keySet().iterator();
        // while(keyIt.hasNext()){
        // String key = keyIt.next().toString();
        // String value = props.getProperty(key);
        // System.out.println(key+"="+value);
        // }
        //
        // System.out.println("===============");
        // Map<String, String> env = System.getenv();
        // for (Entry<String, String> en : env.entrySet()){
        // System.out.println(en.getKey() + "="+en.getValue());
        // }
    }

    public static void test() throws FileNotFoundException {
        /*
         * TemplateNotFoundException { String fullScreen =
         * "/tmp/workspace/test//owl.png"; String needle =
         * "/tmp/workspace/test/0.png"; double similarity = 0.9;
         * PyramidTemplateMatcher matcher = new PyramidTemplateMatcher();
         * MatchResult r = dispatch(matcher, fullScreen, needle, similarity);
         */
        // System.out.println("end");
        // System.out.println("r.cx()" + "=" + r.cx() + ",r.cy()=" + r.cy());
    }
    // public static MatchResult dispatch(IMatcher matcher, String haystack,
    // String needle, double similarity) throws FileNotFoundException,
    // TemplateNotFoundException {

    // Properties props = System.getProperties();
    // Iterator<Object> keyIt = props.keySet().iterator();
    // while (keyIt.hasNext()) {
    // String key = keyIt.next().toString();
    // String value = props.getProperty(key);
    // System.out.println(key + "=" + value);
    // }
    // System.out.println("===============");
    // Map<String, String> env = System.getenv();
    // for (Entry<String, String> en : env.entrySet()) {
    // System.out.println(en.getKey() + "=" + en.getValue());
    // }

    // needle = (new File(needle)).getAbsolutePath();
    //
    // IplImage img = cvLoadImage(haystack);
    //
    // if (img == null) {
    // System.out.println("haystack=" + haystack);
    // throw new FileNotFoundException("can't open `" + haystack + "'");
    // }
    // IplImage tmpl = cvLoadImage(needle);
    // if (tmpl == null) {
    // System.out.println("needle=" + needle);
    // throw new FileNotFoundException("can't open `" + needle + "'");
    // }
    // MatchResult result = matcher.find(img, tmpl, similarity);
    // return result;
    // }
}
