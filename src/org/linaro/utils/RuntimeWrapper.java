package org.linaro.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RuntimeWrapper {

    private static final String[] ZERO_LENGTH_STRING_ARRAY = new String[0];

    public static RuntimeResult executeCommand(String... command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            ArrayList<String> errorOutput = new ArrayList<String>();
            ArrayList<String> stdOutput = new ArrayList<String>();
            int status = grabProcessOutput(process, errorOutput, stdOutput,
                    true /* waitForReaders */);
            return RuntimeResult(status, stdOutput, errorOutput);
        } catch (IOException e) {
        } catch (InterruptedException e) {
        } finally {
        }
        return null;
    }

    private static RuntimeResult RuntimeResult(int status,
            ArrayList<String> stdOutput, ArrayList<String> errorOutput) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Get the stderr/stdout outputs of a process and return when the process is
     * done. Both <b>must</b> be read or the process will block on windows.
     * 
     * @param process
     *            The process to get the ouput from
     * @param errorOutput
     *            The array to store the stderr output. cannot be null.
     * @param stdOutput
     *            The array to store the stdout output. cannot be null.
     * @param displayStdOut
     *            If true this will display stdout as well
     * @param waitforReaders
     *            if true, this will wait for the reader threads.
     * @return the process return code.
     * @throws InterruptedException
     */
    private static int grabProcessOutput(final Process process,
            final ArrayList<String> errorOutput,
            final ArrayList<String> stdOutput, boolean waitforReaders)
            throws InterruptedException {
        assert errorOutput != null;
        assert stdOutput != null;
        // read the lines as they come. if null is returned, it's
        // because the process finished
        Thread t1 = new Thread("") { //$NON-NLS-1$
            @Override
            public void run() {
                // create a buffer to read the stderr output
                InputStreamReader is = new InputStreamReader(
                        process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);

                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            errorOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    // do nothing.
                }
            }
        };

        Thread t2 = new Thread("") { //$NON-NLS-1$
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(
                        process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);

                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            stdOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    // do nothing.
                }
            }
        };

        t1.start();
        t2.start();

        // it looks like on windows process#waitFor() can return
        // before the thread have filled the arrays, so we wait for both threads
        // and the
        // process itself.
        if (waitforReaders) {
            try {
                t1.join();
            } catch (InterruptedException e) {
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
            }
        }

        // get the return code from the process
        return process.waitFor();
    }

    public static String[] merge2Strings(String[] array1, String[] array2) {
        ArrayList<String> array = new ArrayList<String>();
        for (int len = 0; len < array1.length; len++) {
            array.add(array1[len]);
        }
        for (int len = 0; len < array2.length; len++) {
            array.add(array2[len]);
        }
        return array.toArray(ZERO_LENGTH_STRING_ARRAY);
    }

    public static String[] merge2Strings(ArrayList<String> array,
            String[] array2) {
        for (int len = 0; len < array2.length; len++) {
            array.add(array2[len]);
        }
        return array.toArray(ZERO_LENGTH_STRING_ARRAY);
    }
}

class RuntimeResult{
    int status;
    ArrayList<String> errorOutput;
    ArrayList<String> stdOutput;

    public RuntimeResult(int status, ArrayList<String> stdOutput,
            ArrayList<String> errorOutput) {
        super();
        this.status = status;
        this.stdOutput = stdOutput;
        this.errorOutput = errorOutput;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<String> getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(ArrayList<String> errorOutput) {
        this.errorOutput = errorOutput;
    }

    public ArrayList<String> getStdOutput() {
        return stdOutput;
    }

    public void setStdOutput(ArrayList<String> stdOutput) {
        this.stdOutput = stdOutput;
    }

}