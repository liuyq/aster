package org.zeroxlab.aster;

import java.awt.event.*;
import javax.swing.*;

public class AsterMainMenuBar extends JMenuBar {

    JMenu mFileMenu;

    public AsterMainMenuBar() {
	mFileMenu = new JMenu("File");
	mFileMenu.setMnemonic(KeyEvent.VK_F);
	add(mFileMenu);
    }
}
