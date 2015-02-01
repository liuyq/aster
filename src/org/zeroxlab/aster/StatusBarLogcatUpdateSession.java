package org.zeroxlab.aster;

import java.io.BufferedReader;

import javax.swing.SwingUtilities;

import org.linaro.utils.DeviceForAster;

public class StatusBarLogcatUpdateSession implements Runnable {

    @Override
    public void run() {
        try {
            DeviceForAster device = DeviceForAster.getInstance();
            BufferedReader logcatBuggerReader = device.monitorAdbCmdsOutput(
                    "logcat", "-v", "time", "-b", "all", "*:V");
            String line = null;
            if (logcatBuggerReader != null) {
                while (true) {
                    line = logcatBuggerReader.readLine();
                    if (line != null) {
                        logcatMessage(line);
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void logcatMessage(final String line) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StatusBar.getInstance().logcat(line);
            }
        });
    }
}
