package org.zeroxlab.aster.cmds;


public class TouchWithText extends TouchWithAbstract {
    private static final String name = "TouchWithText";
    private static final String tipMsg = "Please input text you want to touch";
    private static final String targetKey = "text";

    public TouchWithText() {
        super();
    }

    public TouchWithText(String rootPath, String line) {
        super(rootPath, line);
    }

    @Override
    public String getTipMsg() {
        return tipMsg;
    }

    @Override
    public String getTargetKey() {
        return targetKey;
    }

    @Override
    public String getName() {
        return name;
    }

}
