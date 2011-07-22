package org.zeroxlab.aster;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class ImageView extends JComponent {

    private BufferedImage image;

    public ImageView() {
    }

    public ImageView(BufferedImage img) {
	setImage(img);
    }

    public void setImage(BufferedImage img) {
	this.image = img;
    }

    public void paintComponent(Graphics g) {
	g.setColor(Color.BLACK);
	g.fillRect(0, 0, 2000, 2000);
        g.drawImage(image, 0, 0, null);
    }

    public Dimension getMinimumSize() {
	return new Dimension(1067, 600);
    }

    public Dimension getPreferredSize() {
	return new Dimension(1067, 600);
    }

    public Dimension getMaximumSize() {
	return new Dimension(1067, 600);
    }
}
