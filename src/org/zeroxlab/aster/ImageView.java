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

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Vector;
import javax.swing.*;

public class ImageView extends JComponent implements ComponentListener, MouseListener{

    public final static int LANDSCAPE_WIDTH  = 400;
    public final static int LANDSCAPE_HEIGHT = 240;
    public final static int PORTRAIT_WIDTH  = 240;
    public final static int PORTRAIT_HEIGHT = 400;

    private BufferedImage mSourceImage;
    private BufferedImage mDrawingBuffer;

    private Vector<SnapshotListener> mSnapListeners;

    private int mImgPosX;
    private int mImgPosY;
    private int mWidth;
    private int mHeight;

    private int mPressX;
    private int mPressY;

    public ImageView() {
        this(new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB));
    }

    public ImageView(BufferedImage img) {
        if (mSnapListeners == null) {
            mSnapListeners = new Vector<SnapshotListener>();
        }

        mDrawingBuffer = new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        addComponentListener(this);
        addMouseListener(this);
        setImage(img);
        generateDrawingBuffer();
    }

    public void setImage(BufferedImage img) {
        mSourceImage = img;
        if (mSourceImage != null) {
            updateDrawingBuffer(mSourceImage);
        }
    }

    public void addSnapshotListener(SnapshotListener listener) {
        mSnapListeners.add(listener);
    }

    public void removeSnapshotListener(SnapshotListener listener) {
        mSnapListeners.remove(listener);
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

    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        for (SnapshotListener listener: mSnapListeners){
            listener.clicked(x, y);
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        mPressX = e.getX();
        mPressY = e.getY();
    }

    public void mouseReleased(MouseEvent e) {
        double distance;
        int rX = e.getX();
        int rY = e.getY();
        distance = Math.pow(rX - mPressX, 2);
        distance += Math.pow(rY - mPressY, 2);
        if (distance < 16) {
            return;
        }
        for (SnapshotListener listener: mSnapListeners){
            listener.dragged(mPressX, mPressY, rX, rY);
        }
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

    public interface SnapshotListener {
        public void clicked(int x, int y);
        public void dragged(int startX, int startY, int endX, int endY);
    }
}
