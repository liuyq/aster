package org.zeroxlab.aster;

import java.io.BufferedReader;

import javax.swing.SwingUtilities;

import org.linaro.utils.DeviceForAster;

public class StatusBarKmsgUpdateSession implements Runnable {

    @Override
    public void run() {
        try {
            DeviceForAster device = DeviceForAster.getInstance();
            BufferedReader kmsgReader = device.monitorAdbCmdsOutput("shell",
                    "su", "0", "cat", "/proc/kmsg");
            String line = null;
            if (kmsgReader != null) {
                while (true) {
                    line = kmsgReader.readLine();
                    if (line != null) {
                        kmsgMessage(line);
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void kmsgMessage(final String line) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StatusBar.getInstance().kmsg(line);
            }
        });
    }
}
