package org.zeroxlab.aster;

import javax.swing.*;

public class AsterMain {

    public static void main(String[] args) {
	trySetupLookFeel();
	JFrame f = new JFrame("Aster");
	f.setJMenuBar(new AsterMainMenuBar());
	f.setContentPane(new AsterMainPanel());
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.pack();
	f.setVisible(true);
    }

    static void trySetupLookFeel() {
	System.setProperty("awt.useSystemAAFontSettings","on");
	System.setProperty("swing.aatext", "true");
	try {
	    // Set System L&F
	    UIManager.setLookAndFeel(
		"com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
	} 
	catch (UnsupportedLookAndFeelException e) {
	    // handle exception
	}
	catch (ClassNotFoundException e) {
	    // handle exception
	}
	catch (InstantiationException e) {
	    // handle exception
	}
	catch (IllegalAccessException e) {
	    // handle exception
	}
    }
}
