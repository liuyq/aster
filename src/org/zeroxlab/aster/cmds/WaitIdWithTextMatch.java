package org.zeroxlab.aster.cmds;


public class WaitIdWithTextMatch extends WaitIdWithTextAbstract {
    private static final String name = "WaitIdWithTextMatch";

    public WaitIdWithTextMatch() {
        super();
    }

    public WaitIdWithTextMatch(String rootPath, String line) {
        super(rootPath, line);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected boolean shouldMatch() {
        return true;
    }
}
