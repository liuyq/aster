package org.zeroxlab.aster.cmds;

import java.io.IOException;

import javax.script.SimpleBindings;

import org.linaro.utils.RuntimeWrapper.RuntimeResult;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpGetInput;

public class AdbShell extends AsterCommand {
    private static final String name = "AdbShell";
    private String mCommandStr = "";
    String tipMsg = "Please input the shell command to run on the device";

    public AdbShell() {
        mCommandStr = new String();
        super.mOps = new AsterOperation[1];
        super.mOps[0] = new OpGetInput(tipMsg, "");
    }

    public AdbShell(String rootPath, String line) {
        super.setFilled(true);
        String[] args = splitArgs(line);

        if (args.length == 1) {
            // AdbShell(text)
            mCommandStr = stripQuote(args[0]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = new OpGetInput(tipMsg, "");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("Text", mCommandStr);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings == null) {
            return;
        }

        if (settings.containsKey("Text")) {
            mCommandStr = (String) settings.get("Text");
        }
    }

    @Override
    public String toScript() {
        return String.format("%s('%s')\n", name, mCommandStr);
    }

    @Override
    public void execute() {
        if (mCommandStr != null && !mCommandStr.isEmpty()) {
            RuntimeResult res = super.device.executeAdbShell(mCommandStr);
            System.out.println("========stdout==========");
            for (String line : res.getStdOutput()) {
                System.out.println(line);
            }
            System.out.println("========stderr==========");
            for (String line : res.getErrorOutput()) {
                System.out.println(line);
            }
            System.out
                    .println("=====Exit Status:" + res.getStatus() + " =====");

        }
    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }

}
