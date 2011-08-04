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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.zeroxlab.aster.ActionListModel;
import org.zeroxlab.aster.JActionList;

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
	installComponents();
	installListeners();

	c.setLayout(createLayoutManager());
    }

    /*
     * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
     */
    public void uninstallUI(JComponent c) {
	c.setLayout(null);
	uninstallListeners();
	uninstallComponents();
	uninstallDefaults();

	this.actionList = null;
    }

    public void installDefaults() {
    }

    public void installComponents() {
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
		    actionList.repaint();
		}
	    };
	this.actionList.getModel().addChangeListener(
	    this.actionListChangeListener);
    }

    public void uninstallDefaults() {
    }

    public void uninstallComponents() {
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
    }

    /**
     * Invoked by <code>installUI</code> to create a layout manager object to
     * manage the {@link JActionList}.
     *
     * @return a layout manager object
     */
    protected LayoutManager createLayoutManager() {
	return new ActionListLayout();
    }

    /**
     * Layout for the action list.
     */
    protected class ActionListLayout implements LayoutManager {
	public void addLayoutComponent(String name, Component c) {
	}

	public void removeLayoutComponent(Component c) {
	}

	public Dimension preferredLayoutSize(Container c) {
	    return new Dimension(0, 0);
	}

	public Dimension minimumLayoutSize(Container c) {
	    return this.preferredLayoutSize(c);
	}

	public void layoutContainer(Container c) {
	}
    }
}
