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

/**
 * Implementation for controling the actions/operations list event
 *
 * @author liuyq
 *
 */
public class ActionListController implements IActionListContoller {
    public ActionListController() {
    }

    // /////////////////////////////////////////////////////////////////////////
    // /////////////////////Operations about StateChangeListener//////////////
    // /////////////////////////////////////////////////////////////////////////
    /** The listeners waiting for model changes. */
    protected EventListenerList stateListenerList = new EventListenerList();

    protected boolean mChangeEnabled = true;

    @Override
    public void addStateChangeListener(ChangeListener l) {
        stateListenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeStateChangeListener(ChangeListener l) {
        stateListenerList.remove(ChangeListener.class, l);
    }

    /**
     * Returns an array of all the change listeners registered on this
     * <code>ActionListModel</code>.
     *
     * @return all of this model's <code>ChangeListener</code>s or an empty
     *         arrary if no change listeners are currently registered
     *
     * @see #addchangeListener
     * @see #removeChangeListener
     */
    public ChangeListener[] getChangeListeners() {
        return stateListenerList.getListeners(ChangeListener.class);
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
    private void fireStateChanged() {
        if (!mChangeEnabled)
            return;
        ChangeEvent ev = null;
        Object[] listeners = stateListenerList.getListenerList();
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
     * The command stack, but does not include the first command for
     * initialization and going to home
     */
    Deque<AsterCommand> actionsDeque = new ArrayDeque<AsterCommand>();

    /** The first command for init and go to home */
    AsterCommand initAndHomeCmd;

    /**
     * Get the command stack, but does not include the first command for
     * initialization and going to home
     */
    @Override
    public Iterable<AsterCommand> getCommands() {
        return actionsDeque;
    }

    @Override
    public void pushCmd(AsterCommand cmd) {
        actionsDeque.addLast(cmd);
        fireStateChanged();
    }

    @Override
    public void popCmd() {
        try {
            actionsDeque.removeLast();
            fireStateChanged();
        } catch (NoSuchElementException e) {
            // log.w("try to pop empty command list");
        }
    }

    @Override
    public boolean isCmdListEmpty() {
        return actionsDeque.isEmpty();
    }

    @Override
    public void clear() {
        actionsDeque.clear();
        fireStateChanged();
    }

    @Override
    public AsterCommand[] toArray() {
        AsterCommand[] commands = new AsterCommand[actionsDeque.size() + 1];
        commands[0] = initAndHomeCmd;
        System.arraycopy(actionsDeque.toArray(), 0, commands, 1,
                actionsDeque.size());
        return commands;
    }

    // operations on InitAndHome command
    public void setInitAndHomeCmd(AsterCommand cmd) {
        initAndHomeCmd = cmd;
        fireStateChanged();
    }

    @Override
    public AsterCommand getInitAndHomeCmd() {
        return initAndHomeCmd;
    }
    // /////////////////////////////////////////////////////////////////////////
    // /////////////////////Operations about CommandChangeListener//////////////
    // /////////////////////////////////////////////////////////////////////////

    /** The listeners waiting for command changes. */
    protected EventListenerList cmdListenerList = new EventListenerList();

    @Override
    public void addCommandChangeListener(ChangeListener l) {
        cmdListenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeCommandChangeListener(ChangeListener l) {
        cmdListenerList.remove(ChangeListener.class, l);
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
    public ChangeListener[] getCommandChangeListeners() {
        return (ChangeListener[]) cmdListenerList
                .getListeners(ChangeListener.class);
    }

    @Override
    public void trigger() {
        fireCommandChanged();
    }
}
