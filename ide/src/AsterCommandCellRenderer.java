package org.zeroxlab.aster;

import org.zeroxlab.aster.cmds.*;

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
	if (val instanceof Click) {
	    return getClickRenderer(list,
				    (Click)val, idx, isSelected, cellHasFocus);
	}

	return null;
    }

    private Component getClickRenderer
	(JList list,
	 Click val,
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
}
