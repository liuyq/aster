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
