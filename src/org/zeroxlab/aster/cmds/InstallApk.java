package org.zeroxlab.aster.cmds;

import java.io.File;
import java.io.IOException;

import javax.script.SimpleBindings;

import org.linaro.utils.LinaroUtils;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpFileChooser;

/**
 * Action for install apk files
 * 
 */
public class InstallApk extends AsterCommand {
    private static final String name = "InstallApk";

    public InstallApk() {
        mOps = new AsterOperation[1];
        mOps[0] = new OpFileChooser();
    }

    public InstallApk(String rootPath, String argline)
            throws IllegalArgumentException {
        super.setRootPath(rootPath);
        super.setFilled(true);
        String[] args = splitArgs(argline);

        if (args.length == 1) {
            // InstallApk(ApkFileName)
            inputFileName = stripQuote(args[0]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[1];
        mOps[0] = new OpFileChooser();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("FilePath", inputFileName);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings.containsKey("FilePath")) {
            String inputFilePath = (String) settings.get("FilePath");
            inputFileName = new File(inputFilePath).getName();
            LinaroUtils.copyFile(new File(inputFilePath),
                    new File(this.getRootPath(),
                    inputFileName));
        }
    }

    @Override
    public String toScript() {
        return String.format("%s('%s')\n", name, inputFileName);
    }

    @Override
    public void execute() {
        super.device.installApk(new File(getRootPath(), inputFileName)
                .getAbsolutePath());
    }

    @Override
    protected String getCommandPrefix() {
        return name;
    }
}
