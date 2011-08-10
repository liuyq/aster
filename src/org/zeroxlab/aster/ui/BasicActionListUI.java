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

package org.zeroxlab.aster.ui;

import com.android.ninepatch.NinePatch;
import com.android.ninepatch.NinePatchChunk;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.zeroxlab.aster.ActionListModel;
import org.zeroxlab.aster.JActionList;
import org.zeroxlab.aster.AsterCommand;

/**
 * Basic UI for {@link JActionList}.
 */
public class BasicActionListUI extends ActionListUI {
    /**
     * The associated action list.
     */
    protected JActionList actionList;

    protected MouseListener mouseListener;

    protected MouseMotionListener mouseMotionListener;

    protected ChangeListener actionListChangeListener;

    /*
     * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
     */
    public static ComponentUI createUI(JComponent c) {
        return new BasicActionListUI();
    }

    /*
     * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
     */
    public void installUI(JComponent c) {
        this.actionList = (JActionList) c;
        installDefaults();
        installListeners();
    }

    /*
     * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
     */
    public void uninstallUI(JComponent c) {
        c.setLayout(null);
        uninstallListeners();
        uninstallDefaults();

        this.actionList = null;
    }

    public void installDefaults() {
    }

    public void installListeners() {
        this.mouseListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }
            };
        this.actionList.addMouseListener(this.mouseListener);

        this.mouseMotionListener = new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                }
            };
        this.actionList.addMouseMotionListener(this.mouseMotionListener);

        this.actionListChangeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    updateButtonList();
                    actionList.repaint();
                }
            };
        this.actionList.getModel().addChangeListener(
            this.actionListChangeListener);
    }

    public void uninstallDefaults() {
    }

    public void uninstallListeners() {
        this.actionList.removeMouseListener(this.mouseListener);
        this.mouseListener = null;

        this.actionList.removeMouseMotionListener(this.mouseMotionListener);
        this.mouseMotionListener = null;

        this.actionList.getModel().removeChangeListener(
            this.actionListChangeListener);
        this.actionListChangeListener = null;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        for (Component child: c.getComponents()) {
            child.paint(g);
        }
    }

    // ******************
    //   Layout Methods
    // ******************
    protected static int BUTTON_MARGIN = 10;
    protected static String ROOT_LABEL = "RECALL";
    protected java.util.List<JComponent> buttonList;

    protected void updateButtonList() {
        buttonList = new Vector<JComponent>();
        buttonList.add(new ActionButton(this.actionList.getModel().getRecall()));
        for (AsterCommand cmd : this.actionList.getModel().getCommands()) {
            buttonList.add(new ActionButton(cmd));
        }
        updateLayout();
        actionList.revalidate();
    }

    protected void updateLayout() {
        actionList.removeAll();
        int i = BUTTON_MARGIN;
        for (JComponent btn : buttonList) {
            btn.setSize(btn.getPreferredSize());
            btn.setLocation(BUTTON_MARGIN, i);
            i += btn.getHeight() + BUTTON_MARGIN;
            actionList.add(btn);
            LittleArrow arrow = new LittleArrow();
            arrow.setLocation(btn.getX(), btn.getY() + btn.getHeight() - 3); // TODO: How to calculate?
            arrow.setSize(btn.getWidth(), BUTTON_MARGIN);
            actionList.add(arrow);
        }
    }

    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    public Dimension getPreferredSize(JComponent c) {
        Dimension max = new Dimension(0, 0);
        for (Component child: c.getComponents()) {
            Rectangle r = child.getBounds();
            if (r.x + r.width > max.width) {
                max.width = r.x + r.width;
            }
            if (r.y + r.height > max.height) {
                max.height = r.y + r.height;
            }
        }
        max.width += BUTTON_MARGIN;
        return max;
    }

    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }

    static class ActionButton extends JComponent {
        static int TEXT_MARGIN = 10;
        AsterCommand mCommand;
        Font mFont;
        Rectangle mFontBox;
        static NinePatch mPatch;

        static {
            try {
                InputStream stream = ActionButton.class.getResourceAsStream("/green_border.9.png");
                mPatch = NinePatch.load(stream, true, false);
                stream.close();
            } catch (IOException e) {
            }
        }

	public ActionButton(AsterCommand cmd) {
            mCommand = cmd;
            mFont = new Font(Font.SANS_SERIF, Font.BOLD, 20);
            mFontBox = new Rectangle();
        }

        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(mFont);
            return new Dimension(fm.stringWidth("Name" /* mCommand.getName() */) + TEXT_MARGIN*2,
                                 fm.getDescent() + fm.getAscent() + TEXT_MARGIN*2);
        }

        public void setLocation(int x, int y) {
            super.setLocation(x, y);
            mFontBox.setLocation(getBounds().getLocation());
            FontMetrics fm = getFontMetrics(mFont);
            mFontBox.translate(TEXT_MARGIN, TEXT_MARGIN + fm.getAscent());
        }

        public void paint(Graphics g) {
            Rectangle bbox = getBounds();
            if (mPatch != null) {
                mPatch.draw((Graphics2D)g,
                            bbox.x,
                            bbox.y,
                            bbox.width,
                            bbox.height);
            }
            g.setFont(mFont);
            g.drawString("Name" /* mCommand.getName() */,
                         mFontBox.x, mFontBox.y);
        }
    }

    static class LittleArrow extends JComponent {
        public LittleArrow() {
        }
        public void paint(Graphics g) {
            Rectangle bbox = getBounds();
            Line2D.Double line = new Line2D.Double(bbox.x + bbox.width / 2.0,
                                                   bbox.y,
                                                   bbox.x + bbox.width / 2.0,
                                                   bbox.y + bbox.height);
            Polygon arrowHead = new Polygon();
            arrowHead.addPoint( 0,3);
            arrowHead.addPoint( -3, -3);
            arrowHead.addPoint( 3,-3);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.draw(line);
            g2d.translate(line.x2, line.y2);
            g2d.fill(arrowHead);
            g2d.dispose();
        }
    }
}
