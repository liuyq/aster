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
 *             Wei-Ning Huang <azhuang@0xlab.org>
 *             Julian Chu <walkingice@0xlab.org>
 */

package org.zeroxlab.aster;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;
import javax.swing.JComponent;

public class AsterWorkspace extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {

    public final static int LANDSCAPE_WIDTH  = 400;
    public final static int LANDSCAPE_HEIGHT = 240;
    public final static int PORTRAIT_WIDTH  = 240;
    public final static int PORTRAIT_HEIGHT = 400;

    private BufferedImage mSourceImage;
    private BufferedImage mDrawingBuffer;

    private Vector<SnapshotListener> mSnapListeners;

    private Rectangle mImgRect;
    private int mSourceWidth;
    private int mSourceHeight;
    private int mWidth;
    private int mHeight;

    private boolean mValid;
    private int mPressX;
    private int mPressY;

    private final int NONE = 0;
    private final int POINT_L = 1;
    private final int POINT_C = 2;
    private final int POINT_R = 3;
    private final int POINT_D = 4;
    private int mMoving = NONE;
    private ClipRegion mRegion;

    public AsterWorkspace() {
        this(new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB));
    }

    public AsterWorkspace(BufferedImage img) {
        if (mSnapListeners == null) {
            mSnapListeners = new Vector<SnapshotListener>();
        }

        mRegion = new ClipRegion();
        mImgRect = new Rectangle();
        mDrawingBuffer = new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
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
        g.drawImage(mDrawingBuffer, mImgRect.x, mImgRect.y, null);

        mRegion.paint(g);
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

        if (!mImgRect.contains(x, y)) {
            return;
        }

        mRegion.moveC(x, y);
        x = (int)((mSourceWidth * (x - mImgRect.x)) / mImgRect.width);
        y = (int)((mSourceHeight * (y - mImgRect.y)) / mImgRect.height);
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

        if (mRegion.inLT(mPressX, mPressY)) {
            mMoving = POINT_L;
        } else if (mRegion.inRB(mPressX, mPressY)) {
            mMoving = POINT_R;
        } else if (mRegion.inD(mPressX, mPressY)) {
            mMoving = POINT_D;
        } else {
            mMoving = NONE;
        }
    }

    public void mouseReleased(MouseEvent e) {
        double distance;
        int rX = e.getX();
        int rY = e.getY();
        int pX = mPressX;
        int pY = mPressY;

        if (!mImgRect.contains(pX, pY) || !mImgRect.contains(rX, rY)) {
            return;
        }

        /* if the drag distance too short, ignore it */
        distance = Math.pow(rX - pX, 2);
        distance += Math.pow(rY - pY, 2);
        if (distance < 16) {
            return;
        }

        pX = (int)((mSourceWidth * (pX - mImgRect.x)) / mImgRect.width);
        pY = (int)((mSourceHeight * (pY - mImgRect.y)) / mImgRect.height);
        rX = (int)((mSourceWidth * (rX - mImgRect.x)) / mImgRect.width);
        rY = (int)((mSourceHeight * (rY - mImgRect.y)) / mImgRect.height);
        for (SnapshotListener listener: mSnapListeners){
            listener.dragged(pX, pY, rX, rY);
        }
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (mMoving == POINT_L) {
            mRegion.moveL(x, y);
        } else if (mMoving == POINT_R) {
            mRegion.moveR(x, y);
        } else if (mMoving == POINT_D) {
            mRegion.moveD(x, y);
        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    private void generateDrawingBuffer() {
        boolean isLandscape = (mSourceImage.getWidth() > mSourceImage.getHeight());

        if (isLandscape) {
            mImgRect.width  = LANDSCAPE_WIDTH;
            mImgRect.height = LANDSCAPE_HEIGHT;
        } else {
            mImgRect.width  = PORTRAIT_WIDTH;
            mImgRect.height = PORTRAIT_HEIGHT;
        }

        mImgRect.x = (mWidth  - mImgRect.width) / 2;
        mImgRect.y = (mHeight - mImgRect.height) / 2;
        mImgRect.x = Math.max(mImgRect.x, 0);
        mImgRect.y = Math.max(mImgRect.y, 0);

        if (mDrawingBuffer == null
                || mDrawingBuffer.getWidth() != mImgRect.width
                || mDrawingBuffer.getHeight() != mImgRect.height) {
            mDrawingBuffer = new BufferedImage(mImgRect.width, mImgRect.height, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private void updateDrawingBuffer(BufferedImage source) {
        mSourceWidth  = source.getWidth();
        mSourceHeight = source.getHeight();
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

    class ClipRegion {
        final int W = 10;
        final int H = 10;
        int dx, dy;

        final Point pL, pC, pR, pD;

        private boolean mVisible;

        ClipRegion() {
            pL = new Point(0, 0);
            pC = new Point(30, 30);
            pR = new Point(60, 60);
            pD = new Point(-1, -1);
            mVisible = false;
        }

        public void visible(boolean visible) {
            mVisible = visible;
        }

        public void paint(Graphics g) {
            if (!mVisible) {
                return;
            }

            g.setColor(Color.RED);
            g.drawRect(pL.x, pL.y, pR.x - pL.x, pR.y - pL.y);
            g.setColor(Color.BLUE);
            g.fillRect(pL.x, pL.y, W, H);
            g.fillRect(pR.x - W, pR.y - H, W, H);
            g.fillRect(pC.x - (int)(W / 2), pC.y - (int)(H / 2), W, H);

            if (pD.x != -1 || pD.y != -1) {
                g.drawLine(pC.x, pC.y, pD.x, pD.y);
                g.fillRect(pD.x - (int)(W / 2), pD.y - (int)(H / 2), W, H);
            }
        }

        public void moveC(int x, int y) {
            dx = x - pC.x;
            dy = y - pC.y;
            pC.setLocation(x, y);
            pL.translate(dx, dy);
            pR.translate(dx, dy);
            if (pD.x != -1 && pD.y != -1) {
                pD.translate(dx, dy);
            }
            repaint();
        }

        public void moveD(int x, int y) {
            pD.setLocation(x, y);
        }

        public void moveL(int x, int y) {
            dx = x - pC.x;
            dy = y - pC.y;
            if (dx > -1 * W || dy > -1 * H) {
                return;
            }

            pL.setLocation(x , y);
            repaint();
        }

        public void moveR(int x, int y) {
            dx = x - pC.x;
            dy = y - pC.y;
            if (dx < W || dy < H) {
                return;
            }

            pR.setLocation(x , y);
            repaint();
        }

        public boolean inLT(int x, int y) {
            return (x >= pL.x && x <= (pL.x + W)
                    && y >= pL.y && y <= (pL.y + H));
        }

        public boolean inRB(int x, int y) {
            return (x >= (pR.x - W) && x <= pR.x
                    && y >= (pR.y - H) && y <= pR.y);
        }

        public boolean inD(int x, int y) {
            if (pD.x == -1 || pD.y == -1) {
                return false;
            }

            dx = x - pD.x;
            dy = y - pD.y;
            return (Math.abs(dx) < (W / 2) || Math.abs(dy) < (H / 2));
        }
    }
}
