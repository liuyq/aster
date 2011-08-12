/*
 * Copyright (C) 2011 0xlab - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authored by Wei-Ning Huang <azhuang@0xlab.org>
 */

package org.zeroxlab.aster;

import java.util.ArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class AsterCommandManager {
    
    static public void dump(AsterCommand[] cmds, String filename)
        throws IOException {
        if (!filename.endsWith(".ast"))
            filename += ".ast";

        String dirname = filename.substring(0, filename.length() - 4);
        File root = new File(dirname);
        if (root.mkdirs())
            throw new IOException(String.format("can not mkdir for '%s'",
                                                dirname));

        FileOutputStream out = new FileOutputStream(new File(dirname,
                                                    root.getName() + ".py"));
        for (int i = 0; i < cmds.length; ++i) {
            out.write(cmds[i].toScript().getBytes());
        }
    }

    static public AsterCommand[] load(String filename) {
        ArrayList<AsterCommand> cmds = new ArrayList<AsterCommand>();
        try {
            FileInputStream ist = new FileInputStream(new File(filename));

            byte[] buf = new byte[4096];
            String data = new String();

            try {
                while (ist.available() > 0) {
                    ist.read(buf);
                    data += new String(buf);
                }
            } catch (IOException e) {
                System.out.println(e);
            }

            for (String s: data.split("\n")) {
                System.out.println(s);
                if (s.startsWith("drag")) {
                    cmds.add(new Drag(s.substring(4, s.length())));
                } else if (s.startsWith("touch")) {
                    cmds.add(new Touch(s.substring(5, s.length())));
                } else if (s.startsWith("press")) {
                    cmds.add(new Press(s.substring(5, s.length())));
                } else if (s.startsWith("type")) {
                    cmds.add(new Type(s.substring(4, s.length())));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        AsterCommand[] cmd_array = new AsterCommand[cmds.size()];
        return cmds.toArray(cmd_array);
    }
}
