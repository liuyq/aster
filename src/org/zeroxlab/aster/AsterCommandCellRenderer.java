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

import org.zeroxlab.aster.Recall;
import org.zeroxlab.aster.Drag;
import org.zeroxlab.aster.Press;
import org.zeroxlab.aster.Touch;
import org.zeroxlab.aster.Type;

import java.awt.*;
import javax.swing.*;

public class AsterCommandCellRenderer
    implements ListCellRenderer {

    public AsterCommandCellRenderer()
    {
    }

    public Component getListCellRendererComponent
	(JList list,
	 Object val,
	 int idx,
	 boolean isSelected,
	 boolean cellHasFocus)
    {
        if (val instanceof Recall) {
	    return getRecallRenderer(list, (Recall)val, idx, isSelected,
                                    cellHasFocus);
        } else if (val instanceof Touch) {
	    return getTouchRenderer(list, (Touch)val, idx, isSelected,
                                    cellHasFocus);
	} else if (val instanceof Drag) {
	    return getDragRenderer(list, (Drag)val, idx, isSelected,
                                   cellHasFocus);
	} else if (val instanceof Press) {
	    return getPressRenderer(list, (Press)val, idx, isSelected,
                                   cellHasFocus);
	} else if (val instanceof Type) {
	    return getTypeRenderer(list, (Type)val, idx, isSelected,
                                   cellHasFocus);
	}

	return null;
    }

    private Component getRecallRenderer
	(JList list,
	 Recall val,
	 int idx,
	 boolean isSelected,
	 boolean cellHasFocus)
    {
	Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	JPanel p = new JPanel();
	p.setForeground(Color.white);
	p.setBackground(isSelected ?
			UIManager.getColor("Table.focusCellBackground") :
			Color.white);
	JLabel c = new JLabel();
	c.setFont(f);
        c.setText("RECALL");
	p.add(c);
	return p;
    }

    private Component getTouchRenderer
	(JList list,
	 Touch val,
	 int idx,
	 boolean isSelected,
	 boolean cellHasFocus)
    {
	Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	JPanel p = new JPanel();
	p.setForeground(Color.white);
	p.setBackground(isSelected ?
			UIManager.getColor("Table.focusCellBackground") :
			Color.white);
	JLabel c = new JLabel();
	c.setFont(f);
	if (val.isFixed()) {
	    Point pos = val.getPos();
	    c.setText("CLICK [pos]");
	} else if (val.isAuto()) {
	    c.setText("CLICK [pic]");
	}
	p.add(c);
	return p;
    }

    private Component getDragRenderer
	(JList list,
	 Drag val,
	 int idx,
	 boolean isSelected,
	 boolean cellHasFocus)
    {
	Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	JPanel p = new JPanel();
	p.setForeground(Color.white);
	p.setBackground(isSelected ?
			UIManager.getColor("Table.focusCellBackground") :
			Color.white);
	JLabel c = new JLabel();
	c.setFont(f);
	if (val.isFixed()) {
	    Point start = val.getStartPos();
	    Point end = val.getStartPos();
	    c.setText("DRAG [start] [end]");
	} else if (val.isAuto()) {
	    c.setText("DRAG [start_img] [end_img]");
	}
	p.add(c);
	return p;
    }

    private Component getPressRenderer
	(JList list,
	 Press val,
	 int idx,
	 boolean isSelected,
	 boolean cellHasFocus)
    {
	Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	JPanel p = new JPanel();
	p.setForeground(Color.white);
	p.setBackground(isSelected ?
			UIManager.getColor("Table.focusCellBackground") :
			Color.white);
	JLabel c = new JLabel();
	c.setFont(f);
	c.setText(String.format("PRESS %s", val.getKeyCode()));
	p.add(c);
	return p;
    }

    private Component getTypeRenderer
	(JList list,
	 Type val,
	 int idx,
	 boolean isSelected,
	 boolean cellHasFocus)
    {
	Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	JPanel p = new JPanel();
	p.setForeground(Color.white);
	p.setBackground(isSelected ?
			UIManager.getColor("Table.focusCellBackground") :
			Color.white);
	JLabel c = new JLabel();
	c.setFont(f);
	c.setText(String.format("TYPE %s", val.getText()));
	p.add(c);
	return p;
    }
}
