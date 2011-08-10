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
 * Authored by Kan-Ru Chen <kanru@0xlab.org>
 */

package org.zeroxlab.aster;

import java.util.*;
import javax.swing.event.*;

public class DefaultActionListModel implements ActionListModel {
    /** The listeners waiting for model changes. */
    protected EventListenerList listenerList = new EventListenerList();

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Run each <code>ChangeListener</code>'s <code>stateChanged</code>
     * method.
     */
    protected void fireStateChanged() {
        ChangeEvent ev = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                if (ev == null)
                    ev = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(ev);
            }
        }
    }

    /**
     * Returns an array of all the change listeners registered on this
     * <code>DefaultActionListModel</code>.
     *
     * @return all of this model's <code>ChangeListener</code>s or an empty
     *         arrary if no change listeners are currently registered
     *
     * @see #addchangeListener
     * @see #removeChangeListener
     */
    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[]) listenerList.getListeners(ChangeListener.class);
    }

    /** The command stack */
    Deque actionList = new ArrayDeque<AsterCommand>();
    /** The recall command */
    AsterCommand recall;

    public DefaultActionListModel() {
    }

    public void setRecall(AsterCommand cmd) {
        recall = cmd;
        fireStateChanged();
    }

    public AsterCommand getRecall() {
        return recall;
    }

    public void pushCmd(AsterCommand cmd) {
        actionList.addFirst(cmd);
        fireStateChanged();
    }

    public void popCmd() {
        try {
            actionList.removeFirst();
            fireStateChanged();
        } catch (NoSuchElementException e) {
            // log.w("try to pop empty command list");
        }
    }

    public Iterable<AsterCommand> getCommands() {
        return actionList;
    }
}
