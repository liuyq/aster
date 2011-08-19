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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import javax.script.SimpleBindings;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.zeroxlab.aster.ActionListModel;
import org.zeroxlab.aster.AsterCommand;
import org.zeroxlab.aster.AsterCommand.CommandListener;
import org.zeroxlab.aster.AsterOperation.OperationListener;
import org.zeroxlab.aster.OpDrag;
import org.zeroxlab.aster.OpTouch;

public class AsterWorkspace extends JComponent implements ComponentListener
                                                         , MouseListener
                                                         , MouseMotionListener
                                                         , ChangeListener
                                                         , AsterOperation.OperationListener {

    public final static int LANDSCAPE_WIDTH  = 400;
    public final static int LANDSCAPE_HEIGHT = 240;
    public final static int PORTRAIT_WIDTH  = 240;
    public final static int PORTRAIT_HEIGHT = 400;

    private static JButton sDone;
    private static JButton sHome;
    private static JButton sMenu;
    private static JButton sBack;
    private static JButton sSearch;

    private BufferedImage mSourceImage;
    private BufferedImage mDrawingBuffer;

    private DragListener mDragListener;
    private TouchListener mTouchListener;

    private Rectangle mImgRect;
    private int mSourceWidth;
    private int mSourceHeight;
    private int mWidth;
    private int mHeight;

    private boolean mValid;
    private boolean mDragged;
    private int mPressX;
    private int mPressY;

    private final int NONE = 0;
    private final int POINT_L = 1;
    private final int POINT_C = 2;
    private final int POINT_R = 3;
    private final int POINT_D = 4;
    private int mMoving = NONE;
    private static ClipRegion sRegion;

    private static MainKeyListener sMainKeyListener;
    private static FillListener sFillListener;
    private static OperationListener sOpListener;
    private static AsterCommand sRecordingCmd;
    private static AsterOperation sRecordingOp;

    private static AsterWorkspace sMyself = null;

    public static AsterWorkspace getInstance() {
        if (sMyself == null) {
            sMyself = new AsterWorkspace();
        }

        return sMyself;
    }

    private AsterWorkspace() {
        this(new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB));
    }

    private AsterWorkspace(BufferedImage img) {
        initJComponents();

        sRegion = new ClipRegion();
        mImgRect = new Rectangle();
        mDrawingBuffer = new BufferedImage(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setImage(img);
        generateDrawingBuffer();
    }

    private void initJComponents() {
        /* It is not elegant but acceptable */
        setLayout(null);
        sDone = new JButton("Done");
        sDone.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sRegion.setVisible(false);
                        sDone.setEnabled(false);
                        mDragListener = null;
                        mTouchListener = null;
                        repaint();

                        AsterOperation[] ops = sRecordingCmd.getOperations();
                        for (int i = 0; i < ops.length; i++) {
                            try {
                                sRecordingCmd.fillSettings(ops[i].getSettings());
                            } catch (IOException exception) {
                                // Can't save Image, GUI notify
                                System.err.printf(exception.toString());
                            }
                        }

                        if (sFillListener != null) {
                            sFillListener.commandFilled(sRecordingCmd);
                        }
                    }
                });
        sDone.setBounds(10, 10, 100, 50);
        sDone.setEnabled(false);
        add(sDone);

        sBack   = new JButton("BACK");
        sMenu   = new JButton("MENU");
        sHome   = new JButton("HOME");
        sSearch = new JButton("SEARCH");
        MainKeyMonitor monitor = new MainKeyMonitor();
        sBack.addActionListener(monitor);
        sMenu.addActionListener(monitor);
        sHome.addActionListener(monitor);
        sSearch.addActionListener(monitor);
        GridLayout grid = new GridLayout(1, 4);
        JPanel btnPanel = new JPanel(grid);
        btnPanel.add(sBack);
        btnPanel.add(sMenu);
        btnPanel.add(sHome);
        btnPanel.add(sSearch);
        btnPanel.setBounds(220, 10, 280, 30);
        add(btnPanel);
    }

    public void setImage(BufferedImage img) {
        mSourceImage = img;
        if (mSourceImage != null) {
            generateDrawingBuffer();
            updateDrawingBuffer(mSourceImage);
        }
    }

    public void fillCmd(AsterCommand cmd, FillListener listener) {
        sRecordingCmd = cmd;
        sFillListener = listener;
        AsterOperation[] ops = sRecordingCmd.getOperations();
        if (ops == null || ops.length == 0) {
            System.err.println("You are asking me to fill an empty command");
            return;
        }

        sRegion.setVisible(false);
        sRegion.moveD(-1, -1); // hide
        setDragListener(null);
        setTouchListener(null);
        ops[0].record(this);
    }

    public void operationFinished(AsterOperation op) {
        AsterOperation[] ops = sRecordingCmd.getOperations();
        int now = 0;
        for (int i = 0; i < ops.length; i++) {
            if (ops[i] == op) {
                now = i;
            }
        }

        if (now == ops.length -1) { // tail
            sDone.setEnabled(true);
            return;
        }

        sDone.setEnabled(false);
        sRegion.setVisible(false);
        sRegion.moveD(-1, -1); // hide
        setDragListener(null);
        setTouchListener(null);
        sRecordingOp = ops[now + 1];
        repaint();
        ops[now + 1].record(this);
    }

    public void setMainKeyListener(MainKeyListener listener) {
        sMainKeyListener = listener;
    }

    public void setDragListener(DragListener listener) {
        mDragListener = listener;
    }

    public void setTouchListener(TouchListener listener) {
        mTouchListener = listener;
    }

    public void setFillListener(FillListener listener) {
        sFillListener = listener;
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, mWidth, mHeight);
        g.drawImage(mDrawingBuffer, mImgRect.x, mImgRect.y, null);

        sRegion.paint(g);
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
        sRegion.setVisible(false);
        sDone.setEnabled(false);
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

        if (mMoving != NONE) {
            // if clicked on draggable area, just return directly
            return;
        }

        sRegion.moveC(x, y);
        Point start = convertPointW2Img(x, y);
        if (mMoving == NONE && mTouchListener != null) {
            mTouchListener.clicked(start.x, start.y);
        } else if (mDragListener != null) {
            Point end = convertPointW2Img(sRegion.pD.x, sRegion.pD.y);
            mDragListener.dragged(start.x, start.y, end.x, end.y);
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        mPressX = e.getX();
        mPressY = e.getY();
        mDragged = false;

        if (sRegion.isHide()) {
            mMoving = NONE;
            return;
        }

        if (sRegion.inLT(mPressX, mPressY)) {
            mMoving = POINT_L;
        } else if (sRegion.inRB(mPressX, mPressY)) {
            mMoving = POINT_R;
        } else if (sRegion.inD(mPressX, mPressY) && mDragListener != null) {
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

        if (mTouchListener != null) {
            /* if we are recording Touch, just return cause
             * mouseClicked will handle it */
            return;
        }

        if (!mDragged) {
            return;
        }

        if (mMoving == NONE) {
            sRegion.moveC(mPressX, mPressY);
            sRegion.moveD(e.getX(), e.getY());
        }

        Point pressOnSource   = convertPointW2Img(sRegion.pC.x, sRegion.pC.y);
        Point releaseOnSource = convertPointW2Img(sRegion.pD.x, sRegion.pD.y);
        if (mDragListener != null){
            mDragListener.dragged(pressOnSource.x, pressOnSource.y, releaseOnSource.x, releaseOnSource.y);
        }
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        mDragged = true;
        if (mMoving == POINT_L) {
            sRegion.moveL(x, y);
        } else if (mMoving == POINT_R) {
            sRegion.moveR(x, y);
        } else if (mMoving == POINT_D) {
            sRegion.moveD(x, y);
        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    /* The source image will be resized and drawed on Workspace.
     * Convert the point on workspace to the point on the original image */
    private Point convertPointW2Img(int workspaceX, int workspaceY) {
        int iX, iY;
        iX = (int)((mSourceWidth  * (workspaceX - mImgRect.x)) / mImgRect.width);
        iY = (int)((mSourceHeight * (workspaceY - mImgRect.y)) / mImgRect.height);

        iX = Math.min(iX, mSourceWidth);
        iY = Math.min(iY, mSourceHeight);
        iX = Math.max(iX, 0);
        iY = Math.max(iY, 0);
        return new Point(iX, iY);
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

    public void stateChanged(ChangeEvent e) {
        if (!(e.getSource() instanceof ActionListModel)) {
            System.err.println("The source is not an ActionListModel");
            return;
        }

        ActionListModel model = (ActionListModel) e.getSource();

        if (model.empty()) {
            sRegion.setVisible(false);
            repaint();
            return;
        }

        Iterator<AsterCommand> iterator = model.getCommands().iterator();
        AsterCommand last = null;
        while(iterator.hasNext()) {
            last = iterator.next();
        }

        if (last != sRecordingCmd) {
            fillCmd(last, sFillListener);
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

    private BufferedImage createClipImage() {
        Point lt = convertPointW2Img(sRegion.pL.x, sRegion.pL.y);
        Point rb = convertPointW2Img(sRegion.pR.x, sRegion.pR.y);
        Rectangle r = new Rectangle(lt.x, lt.y, rb.x - lt.x, rb.y - lt.y);
        BufferedImage buf = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);
        buf.getGraphics().drawImage(mSourceImage
                                     , 0, 0, buf.getWidth(), buf.getHeight()
                                     , r.x, r.y, r.x + r.width, r.y + r.height, null
                                     );
        return buf;
    }

    public interface DragListener {
        public void dragged(int startX, int startY, int endX, int endY);
    }

    public interface TouchListener {
        public void clicked(int x, int y);
    }

    public interface FillListener {
        public void commandFilled(AsterCommand cmd);
    }

    public interface MainKeyListener {
        public void onClickHome();
        public void onClickMenu();
        public void onClickBack();
        public void onClickSearch();
    }

    public OpTouch getOpTouch() {
        return new MyTouch();
    }

    public OpDrag getOpDrag() {
        return new MyDrag();
    }

    private static int valid(int now, int min, int max) {
        now = Math.max(min, now);
        now = Math.min(now, max);
        return now;
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

        public boolean isHide() {
            return (mVisible == false);
        }

        public void setVisible(boolean visible) {
            mVisible = visible;
        }

        public void paint(Graphics g) {
            if (!mVisible) {
                return;
            }

            g.setColor(Color.RED);
            g.drawRect(pL.x, pL.y, pR.x - pL.x, pR.y - pL.y);
            g.setColor(Color.BLUE);
            g.fillOval(pC.x - (int)(W / 2), pC.y - (int)(H / 2), W, H);
            g.setColor(Color.YELLOW);
            g.fillRect(pL.x, pL.y, W, H);
            g.fillRect(pR.x - W, pR.y - H, W, H);

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

    class MyTouch extends OpTouch implements TouchListener {
        int mLTX, mLTY, mRBX, mRBY;
        OperationListener mListener;

        public void clicked(int x, int y) {
            setPoint(x, y);
        }

        public void setClip(int x1, int y1, int x2, int y2) {
            mLTX = valid(x1, 0, mSourceWidth);
            mLTY = valid(y1, 0, mSourceHeight);
            mRBX = valid(x2, 0, mSourceWidth);
            mRBY = valid(y2, 0, mSourceHeight);
        }

        public void setPoint(int x, int y) {
            x = Math.max(0, x);
            y = Math.max(0, y);
            x = Math.min(x, mSourceWidth);
            y = Math.min(y, mSourceHeight);
            super.set(x, y);
            sRegion.setVisible(true);
            if (mListener != null) {
                mListener.operationFinished(this);
            }
            repaint();
        }

        public String getName() {
            return "Touch";
        }

        public void record(AsterOperation.OperationListener listener) {
            sDone.setEnabled(false);
            mListener = listener;
            int x = super.getX();
            int y = super.getY();
            int ltx = mLTX;
            int lty = mLTY;
            int rbx = mRBX;
            int rby = mRBY;

            if (super.isValid()) {
                x = (int)((x * mImgRect.width) / mSourceWidth) ;
                y = (int)((y * mImgRect.height ) / mSourceHeight);
                ltx = (int)((ltx * mImgRect.width)  / mSourceWidth);
                lty = (int)((lty * mImgRect.height) / mSourceHeight);
                rbx = (int)((rbx * mImgRect.width)  / mSourceWidth);
                rby = (int)((rby * mImgRect.height) / mSourceHeight);
                sRegion.setVisible(true);
            } else {
                // move to center by default
                x = (int)(mImgRect.width / 2);
                y = (int)(mImgRect.height / 2);
                ltx = x - 30;
                lty = y - 30;
                rbx = x + 30;
                rby = y + 30;
                sRegion.setVisible(false);
            }

            sRegion.moveC(x + mImgRect.x, y + mImgRect.y);
            sRegion.moveL(ltx + mImgRect.x, lty + mImgRect.y);
            sRegion.moveR(rbx + mImgRect.x, rby + mImgRect.y);
            repaint();
            setTouchListener(this);
        }

        public SimpleBindings getSettings() {
            SimpleBindings settings = new SimpleBindings();
            settings.put("Pos", super.getPoint());
            BufferedImage buf = createClipImage();
            Point lt = convertPointW2Img(sRegion.pL.x, sRegion.pL.y);
            Point rb = convertPointW2Img(sRegion.pR.x, sRegion.pR.y);
            this.setClip(lt.x, lt.y, rb.x, rb.y);
            settings.put("Image", buf);
            if (AsterMainPanel.needRotate()) {
                settings.put("Landscape", true);
            }
            return settings;
        }
    }

    class MyDrag extends OpDrag implements DragListener {
        int mLTX, mLTY, mRBX, mRBY;
        OperationListener mListener;

        public void setClip(int x1, int y1, int x2, int y2) {
            mLTX = valid(x1, 0, mSourceWidth);
            mLTY = valid(y1, 0, mSourceHeight);
            mRBX = valid(x2, 0, mSourceWidth);
            mRBY = valid(y2, 0, mSourceHeight);
        }

        public String getName() {
            return "Drag";
        }

        public void record(AsterOperation.OperationListener listener) {
            sDone.setEnabled(false);
            mListener = listener;
            sOpListener = listener;
            int sX = super.getStartX();
            int sY = super.getStartY();
            int eX = super.getEndX();
            int eY = super.getEndY();
            int ltx = mLTX;
            int lty = mLTY;
            int rbx = mRBX;
            int rby = mRBY;

            Rectangle r = new Rectangle(0, 0, mSourceWidth, mSourceHeight);
            if (r.contains(sX, sY) && r.contains(eX, eY)
                    && sX != 0 && sY != 0) {
                // valid location
                sX = (int)((sX * mImgRect.width)  / mSourceWidth);
                sY = (int)((sY * mImgRect.height) / mSourceHeight);
                eX = (int)((eX * mImgRect.width)  / mSourceWidth);
                eY = (int)((eY * mImgRect.height) / mSourceHeight);
                ltx = (int)((ltx * mImgRect.width)  / mSourceWidth);
                lty = (int)((lty * mImgRect.height) / mSourceHeight);
                rbx = (int)((rbx * mImgRect.width)  / mSourceWidth);
                rby = (int)((rby * mImgRect.height) / mSourceHeight);
                sRegion.setVisible(true);
            } else {
                // move to center by default
                sX = (int)(mImgRect.width / 2);
                sY = (int)(mImgRect.height / 2);
                eX = sX + 100;
                eY = sY;
                ltx = sX - 30;
                lty = sY - 30;
                rbx = sX + 30;
                rby = sY + 30;
                sRegion.setVisible(false);
            }
            sRegion.moveC(sX + mImgRect.x, sY + mImgRect.y);
            sRegion.moveD(eX + mImgRect.x, eY + mImgRect.y);
            sRegion.moveL(ltx + mImgRect.x, lty + mImgRect.y);
            sRegion.moveR(rbx + mImgRect.x, rby + mImgRect.y);
            repaint();
            setDragListener(this);
        }

        public void dragged(int sx, int sy, int ex, int ey) {
            sx = Math.max(0, sx);
            sy = Math.max(0, sy);
            sx = Math.min(sx, mSourceWidth);
            sy = Math.min(sy, mSourceHeight);
            ex = Math.max(0, ex);
            ey = Math.max(0, ey);
            ex = Math.min(ex, mSourceWidth);
            ey = Math.min(ey, mSourceHeight);
            super.set(sx, sy, ex, ey);
            sRegion.setVisible(true);
            if (mListener != null) {
                mListener.operationFinished(this);
            }
        }

        public SimpleBindings getSettings() {
            SimpleBindings settings = new SimpleBindings();
            settings.put("StartPos", super.getStart());
            settings.put("EndPos", super.getEnd());
            BufferedImage buf = createClipImage();
            settings.put("Image", buf);
            Point lt = convertPointW2Img(sRegion.pL.x, sRegion.pL.y);
            Point rb = convertPointW2Img(sRegion.pR.x, sRegion.pR.y);
            this.setClip(lt.x, lt.y, rb.x, rb.y);
            if (AsterMainPanel.needRotate()) {
                settings.put("Landscape", true);
            }
            return settings;
        }
    }

    private class MainKeyMonitor implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (sMainKeyListener == null) {
                return;
            }

            Object o = e.getSource();
            if (o == sHome) {
                sMainKeyListener.onClickHome();
            } else if (o == sMenu) {
                sMainKeyListener.onClickMenu();
            } else if (o == sBack) {
                sMainKeyListener.onClickBack();
            } else if (o == sSearch) {
                sMainKeyListener.onClickSearch();
            } else {
                System.err.println("Clicked an unknow button");
            }
        }
    }
}
