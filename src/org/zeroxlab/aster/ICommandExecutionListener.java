package org.zeroxlab.aster;

import org.zeroxlab.aster.cmds.AsterCommand.ExecutionResult;

public interface ICommandExecutionListener {
    public void processResult(ExecutionResult result);
}