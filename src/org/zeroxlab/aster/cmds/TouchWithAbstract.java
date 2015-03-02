package org.zeroxlab.aster.cmds;

import java.awt.Point;
import java.io.IOException;

import javax.script.SimpleBindings;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpGetInput;

public abstract class TouchWithAbstract extends AsterCommand {
    protected String target = "";

    public abstract String getTipMsg();

    public abstract String getTargetKey();

    public abstract String getName();

    public TouchWithAbstract() {
        target = new String();
        super.mOps = new AsterOperation[1];
        super.mOps[0] = new OpGetInput(getTipMsg(), "");
    }

    public TouchWithAbstract(String rootPath,
            String line) {
        super.setFilled(true);
        String[] args = splitArgs(line);

        if (args.length == 1) {
            // AdbShell(text)
            target = stripQuote(args[0]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = new OpGetInput(getTipMsg(), "");
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Text", target);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings == null) {
            return;
        }

        if (settings.containsKey("Text")) {
            target = (String) settings.get("Text");
        }
    }

    @Override
    public String toScript() {
        return String.format("%s('%s')\n", getName(), target);
    }

    public void execute() {
        if (target != null && !target.isEmpty()) {
            String bounds = getTargetBounds(getAllNodes());
            if (bounds != null) {
                Point center = getCenter(bounds);
                device.touch((int) center.getX(), (int) center.getY());
            }
        }
    }

    @Override
    protected String getCommandPrefix() {
        return getName();
    }

    public NodeList getAllNodes() {
        return device.getAllNodes();
    }

    public String getTargetBounds(NodeList nl) {
        if (nl == null || nl.getLength() == 0) {
            return null;
        }
        for (int i = 0; i < nl.getLength(); i++) {
            NamedNodeMap attributes = nl.item(i).getAttributes();
            if (null != attributes) {
                String text = attributes.getNamedItem(getTargetKey())
                        .getNodeValue();
                if (text.equals(target)) {
                    return attributes.getNamedItem("bounds").getNodeValue();
                }
            }
        }
        return null;
    }

    public static Point getCenter(String boundsRaw) {
        String bounds = boundsRaw.replaceAll("\\]\\[", ",").replaceAll(
                "\\]|\\[", "");
        String[] pointsStr = bounds.split(",");
        int topLeft_x = Integer.parseInt(pointsStr[0]);
        int topLeft_y = Integer.parseInt(pointsStr[1]);
        int bottomRight_x = Integer.parseInt(pointsStr[2]);
        int bottomRight_y = Integer.parseInt(pointsStr[3]);

        int centerX = topLeft_x + (bottomRight_x - topLeft_x) / 2;
        int centerY = topLeft_y + (bottomRight_y - topLeft_y) / 2;
        return new Point(centerX, centerY);
    }
}
