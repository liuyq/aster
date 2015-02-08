package org.zeroxlab.aster.operations;

import java.io.File;

import javax.script.SimpleBindings;
import javax.swing.JFileChooser;

public class OpFileChooser implements AsterOperation {
    public final static String sName = "Select the file to use:";
    private String mFilePath = null;

    @Override
    public String getName() {
        return sName;
    }

    @Override
    public void record(OperationListener listener) {
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            mFilePath = file.getAbsolutePath();
        }
        listener.operationFinished(this);
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("FilePath", mFilePath);
        return settings;
    }
}
