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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.zeroxlab.aster.cmds.AsterCommand;

public class DefaultActionListModel implements ActionListModel {
    /** The listeners waiting for model changes. */
    protected EventListenerList listenerList = new EventListenerList();

    /** The listeners waiting for command changes. */
    protected EventListenerList cmdListenerList = new EventListenerList();

    protected boolean mChangeEnabled = true;

    @Override
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    @Override
    public void addCommandChangeListener(ChangeListener l) {
        cmdListenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeCommandChangeListener(ChangeListener l) {
        cmdListenerList.remove(ChangeListener.class, l);
    }

    @Override
    public void disableChangeListener() {
        mChangeEnabled = false;
    }

    @Override
    public void enableChangeListener() {
        mChangeEnabled = true;
    }

    /**
     * Run each <code>ChangeListener</code>'s <code>stateChanged</code>
     * method.
     */
    protected void fireStateChanged() {
        if (!mChangeEnabled)
            return;
        ChangeEvent ev = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                if (ev == null) {
                    ev = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(ev);
            }
        }
    }

    /**
     * Run each <code>ChangeListener</code>'s <code>stateChanged</code>
     * method.
     */
    protected void fireCommandChanged() {
        ChangeEvent ev = null;
        Object[] listeners = cmdListenerList.getListenerList();
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
        return listenerList.getListeners(ChangeListener.class);
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
    public ChangeListener[] getCommandChangeListeners() {
        return (ChangeListener[]) cmdListenerList.getListeners(ChangeListener.class);
    }

    /** The command stack */
    Deque<AsterCommand> actionList = new ArrayDeque<AsterCommand>();
    /** The recall command */
    AsterCommand recall;

    public DefaultActionListModel() {
    }

    @Override
    public void setRecall(AsterCommand cmd) {
        recall = cmd;
        fireStateChanged();
    }

    @Override
    public AsterCommand getRecall() {
        return recall;
    }

    @Override
    public void pushCmd(AsterCommand cmd) {
        actionList.addLast(cmd);
        fireStateChanged();
    }

    @Override
    public void popCmd() {
        try {
            actionList.removeLast();
            fireStateChanged();
        } catch (NoSuchElementException e) {
            // log.w("try to pop empty command list");
        }
    }

    @Override
    public boolean empty() {
        return actionList.isEmpty();
    }

    @Override
    public Iterable<AsterCommand> getCommands() {
        return actionList;
    }

    @Override
    public void clear() {
        actionList.clear();
        fireStateChanged();
    }

    @Override
    public void trigger() {
        fireCommandChanged();
    }

    @Override
    public AsterCommand[] toArray() {
        AsterCommand[] commands = new AsterCommand[actionList.size()+1];
        commands[0] = recall;
        System.arraycopy(actionList.toArray(), 0, commands, 1, actionList.size());
        return commands;
    }
}
