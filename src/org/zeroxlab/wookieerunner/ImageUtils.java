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
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {
    public static BufferedImage rotate(BufferedImage src) {
        return rotate270(src);
    }

    private static BufferedImage rotate270(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage rotated = new BufferedImage(height, width, src
                .getColorModel().getTransparency());

        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                // rotated.setRGB(y, width -x -1, src.getRGB(x, y));
                rotated.setRGB(x, y, src.getRGB(y, height - x - 1));
            }
        }
        return rotated;
    }

    private static BufferedImage rotate90(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage rotated = new BufferedImage(height, width, src
                .getColorModel().getTransparency());

        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                rotated.setRGB(x, y, src.getRGB(width - y - 1, x));
            }
        }
        return rotated;
    }

    private static BufferedImage rotate180(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage rotated = new BufferedImage(height, width, src
                .getColorModel().getTransparency());

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                rotated.setRGB(x, y, src.getRGB(width - x - 1, y));
            }
        }
        return rotated;
    }

    public static String rotateImage(String orignal, int order, String filePath) {
        BufferedImage originalImage = getBufferedImage(orignal);
        BufferedImage rotatedImage = originalImage;
        if (order == 270) {
            rotatedImage = rotate270(originalImage);
        } else if (order == 180) {
            rotatedImage = rotate180(originalImage);
        } else if (order == 90) {
            rotatedImage = rotate90(originalImage);
        } else {
            return orignal;
        }
        try {
            File file = new File(filePath);
            file.delete();
            ImageIO.write(rotatedImage, "png", file);
            return filePath;
        } catch (IOException e) {
            return orignal;
        }
    }

    public static BufferedImage getBufferedImage(String path) {
        File file = new File(path);
        BufferedImage image = null;
        if (file.exists() && file.canRead()) {
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return image;
    }
}
