package org.linaro.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LinaroUtils {
    public static boolean copyFile(File src, File target) {
        FileOutputStream fs = null;
        InputStream inStream = null;
        try {
            int byteread = 0;
            fs = new FileOutputStream(new File(target.getAbsolutePath()));
            inStream = new FileInputStream(src);
            if (src.exists()) {
                byte[] buffer = new byte[4096];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
