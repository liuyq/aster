package org.zeroxlab.aster.cmds;


public class WaitIdWithTextNotMatch extends WaitIdWithTextAbstract {
    private static final String name = "WaitIdWithTextNotMatch";

    public WaitIdWithTextNotMatch() {
        super();
    }

    public WaitIdWithTextNotMatch(String rootPath, String line) {
        super(rootPath, line);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected boolean shouldMatch() {
        return false;
    }
}
