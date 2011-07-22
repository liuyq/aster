package org.zeroxlab.aster.cmds;

import org.zeroxlab.aster.AsterCommand;

import java.awt.image.BufferedImage;
import java.awt.Point;

public class Click extends AsterCommand {

    private static int FIXED = 1;
    private static int AUTO  = 2;

    BufferedImage mImage;
    Point mPosition;
    int mFixedOrAuto;

    public Click(BufferedImage img) {
	mImage = img;
	mFixedOrAuto = AUTO;
    }

    public Click(Point pos) {
	mPosition = pos;
	mFixedOrAuto = FIXED;
    }

    public boolean isAuto() {
	return mFixedOrAuto == AUTO;
    }

    public boolean isFixed() {
	return mFixedOrAuto == FIXED;
    }

    public BufferedImage getImage() {
	return mImage;
    }

    public Point getPos() {
	return mPosition;
    }
}
