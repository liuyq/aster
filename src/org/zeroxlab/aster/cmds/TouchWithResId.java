package org.zeroxlab.aster.cmds;


public class TouchWithResId extends TouchWithAbstract {
    private static final String name = "TouchWithResId";
    private static final String tipMsg = "Please input resource-id of the view you want to touch";
    private static final String targetKey = "resource-id";

    public TouchWithResId() {
        super();
    }

    public TouchWithResId(String rootPath, String line) {
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
