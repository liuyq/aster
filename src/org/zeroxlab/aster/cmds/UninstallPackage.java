package org.zeroxlab.aster.cmds;

import java.io.IOException;

import javax.script.SimpleBindings;

import org.linaro.utils.RuntimeWrapper.RuntimeResult;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpGetInput;

public class UninstallPackage extends AsterCommand {
    private static final String name = "UninstallPackage";
    private String apkPackage = "";
    String tipMsg = "Please input the package you want to uninstall:";

    public UninstallPackage() {
        apkPackage = new String();
        super.mOps = new AsterOperation[1];
        super.mOps[0] = new OpGetInput(tipMsg, "");
    }

    public UninstallPackage(String rootPath, String line) {
        super.setFilled(true);
        String[] args = splitArgs(line);

        if (args.length == 1) {
            // UninstallPackage(text)
            apkPackage = stripQuote(args[0]);
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
        settings.put("Text", apkPackage);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings == null) {
            return;
        }

        if (settings.containsKey("Text")) {
            apkPackage = (String) settings.get("Text");
        }
    }

    @Override
    public String toScript() {
        return String.format("%s('%s')\n", name, apkPackage);
    }

    @Override
    public void execute() {
        if (apkPackage != null && !apkPackage.isEmpty()) {
            RuntimeResult res = super.device.executeAdbCommands("uninstall",
                    apkPackage);
        }
    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }

}
