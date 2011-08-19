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

import javax.swing.event.ChangeListener;

public interface ActionListModel {

    /**
     * The recall command is the start of every script. It should
     * setup the test environment and navigate to the home screen.
     */
    public void setRecall(AsterCommand cmd);

    public AsterCommand getRecall();

    /**
     * Push command to the command list
     *
     * @param cmd
     *            the AsterCommand to add
     */
    public void pushCmd(AsterCommand cmd);

    /**
     * Pop command from the command list
     */
    public void popCmd();

    /**
     * Test if the model is empty
     */
    public boolean empty();

    /**
     * Empty the model
     */
    public void clear();

    /**
     * Trigger change event
     */
    public void trigger();

    /**
     * Get command list
     */
    public Iterable<AsterCommand> getCommands();

    /**
     * Turn the model into a command Array (including the recall command)
     */
    public AsterCommand[] toArray();

    /**
     * Disable global change listener
     */
    public void disableChangeListener();

    /**
     * Enable global change listener
     */
    public void enableChangeListener();

    /**
     * Adds a ChangeListener to the model's listener list.
     *
     * @param x
     *            the ChangeListener to add
     * @see #removeChangeListener
     */
    public void addChangeListener(ChangeListener x);

    /**
     * Removes a ChangeListener from the model's listener list.
     *
     * @param x
     *            the ChangeListener to remove
     * @see #addChangeListener
     */
    public void removeChangeListener(ChangeListener x);

    /**
     * Add a ChangeListener to the model's listener list.
     *
     * The listener will only receive event on command change
     * triggered by trigger()
     *
     * @param x
     *            the ChangeListener to add
     * @see #removeCommandChangeListener
     */
    public void addCommandChangeListener(ChangeListener x);

    /**
     * Remove a ChangeListener from the model's listener list.
     *
     * The listener will only receive event on command change
     * triggered by trigger()
     *
     * @param x
     *            the ChangeListener to remove
     * @see #addCommandChangeListener
     */
    public void removeCommandChangeListener(ChangeListener x);
}
