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
 * Authored by Kan-Ru Chen <kanru@0xlab.org>
 */

package org.zeroxlab.aster;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ImageView extends JComponent implements ComponentListener {

    public final static int LANDSCAPE_WIDTH  = 400;
    public final static int LANDSCAPE_HEIGHT = 240;
    public final static int PORTRAIT_WIDTH  = 240;
    public final static int PORTRAIT_HEIGHT = 400;

    private BufferedImage mSourceImage;
    private BufferedImage mDrawingBuffer;

    private int mImgPosX;
    private int mImgPosY;
    private int mWidth;
    private int mHeight;

    public ImageView() {
        this(new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB));
    }

    public ImageView(BufferedImage img) {
        mDrawingBuffer = new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        addComponentListener(this);
        setImage(img);
        generateDrawingBuffer();
    }

    public void setImage(BufferedImage img) {
        mSourceImage = img;
        if (mSourceImage != null) {
            updateDrawingBuffer(mSourceImage);
        }
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, mWidth, mHeight);
        g.drawImage(mDrawingBuffer, mImgPosX, mImgPosY, null);
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        mWidth  = getWidth();
        mHeight = getHeight();
        generateDrawingBuffer();
        updateDrawingBuffer(mSourceImage);
        repaint();
    }

    public void componentShown(ComponentEvent e) {
    }

    private void generateDrawingBuffer() {
        int expectedW, expectedH;
        boolean isLandscape = (mSourceImage.getWidth() > mSourceImage.getHeight());

        if (isLandscape) {
            expectedW = LANDSCAPE_WIDTH;
            expectedH = LANDSCAPE_HEIGHT;
        } else {
            expectedW = PORTRAIT_WIDTH;
            expectedH = PORTRAIT_HEIGHT;
        }
        mImgPosX = (mWidth  - expectedW) / 2;
        mImgPosY = (mHeight - expectedH) / 2;
        mImgPosX = Math.max(mImgPosX, 0);
        mImgPosY = Math.max(mImgPosY, 0);

        if (mDrawingBuffer == null
                || mDrawingBuffer.getWidth() != expectedW
                || mDrawingBuffer.getHeight() != expectedH) {
            mDrawingBuffer = new BufferedImage(expectedW, expectedH, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private void updateDrawingBuffer(BufferedImage source) {
        mDrawingBuffer.getGraphics().drawImage(
                source, 0, 0,
                mDrawingBuffer.getWidth(),
                mDrawingBuffer.getHeight(),
                null
                );
    }
}
