package org.zeroxlab.aster.cmds;


public class TouchWithContentDesc extends TouchWithAbstract {
    private static final String name = "TouchWithContentDesc";
    private static final String tipMsg = "Please input content description of the view you want to touch";
    private static final String targetKey = "content-desc";

    public TouchWithContentDesc() {
        super();
    }

    public TouchWithContentDesc(String rootPath, String line) {
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
