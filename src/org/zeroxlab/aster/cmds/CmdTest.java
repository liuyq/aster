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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class CmdTest {
    static public void main(String argv[]) {

        Drag drag1 = new Drag(".", "('0.png', (400, 400), 0.1, 10, 4, False)");
        Drag drag2 = new Drag(".", "((0, 0), (400, 400), 0.1, 10, 4, False)");
        Touch touch1 = new Touch(".", "('test.png', 'downAndUp', 4, False)");
        Touch touch2 = new Touch(".", "((400, 400), 'downAndUp', 4, False)");
        Press press = new Press("('KEYCODE_HOME', 'downAndUp')");
        Type type = new Type("('testing')");

        AsterCommand[] cmds = { drag1, drag2, touch1, touch2, press, type };

        AsterCommandManager manager = new AsterCommandManager();
        manager.connect();
        for (AsterCommand c: cmds) {
            c.execute();
        }

        try {
            manager.dump(cmds, "cmds.ast");
            cmds = manager.load("cmds.ast");
            for (AsterCommand c: cmds) {
                System.out.printf("%s", c.toScript());
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
