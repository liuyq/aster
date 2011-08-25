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
 *             Wei-Ning Huang <azhuang@0xlab.org>
 *             Julian Chu <walkingice@0xlab.org>
 */

package org.zeroxlab.aster;

import javax.swing.JOptionPane;

import org.zeroxlab.aster.ActionListModel;
import org.zeroxlab.aster.CmdConnection;
import org.zeroxlab.aster.cmds.AsterCommand;
import org.zeroxlab.aster.cmds.AsterCommand.CommandExecutionListener;
import org.zeroxlab.aster.cmds.AsterCommand.ExecutionResult;
import org.zeroxlab.aster.cmds.AsterCommandManager;
import org.zeroxlab.wookieerunner.ImageUtils;

class AsterController {

    private static AsterCommandManager sCmdMgr;
    private static AsterController sController;
    private static CmdConnection sCmdConn;
    private static ActionListModel sListModel;
    private static ActionExecutor sExecutor;

    public static AsterController getInstance() {
        if (sController == null) {
            sController = new AsterController();
        }

        return sController;
    }

    private AsterController() {
        sExecutor = new ActionExecutor();
    }

    /*FIXME: Its bad since the Connection is always needed*/
    public static void setConnection(CmdConnection conn) {
        sCmdConn = conn;
    }

    /*FIXME: Its bad since the Model is always needed*/
    public static void setModel(ActionListModel model) {
        sListModel = model;
    }

    /*FIXME: Its bad since the Manager is always needed*/
    public static void setCmdMgr(AsterCommandManager mgr) {
        sCmdMgr = mgr;
    }

    public static ActionExecutor getCommandExecutionListener() {
        return sExecutor;
    }

    class ActionExecutor implements ActionDashboard.ClickListener,
                                    CommandExecutionListener {
          AsterCommand[] mList;

          /*FIXME: Controller should not use Dashboard directly */
          ActionDashboard mDashboard;

          int mIndex; // refer to a command which is going to be executed
          // but not happen yet
          boolean mInPlaying = false;

          ActionExecutor() {
              mDashboard = ActionDashboard.getInstance();
              mDashboard.setListener(this);
              reset();
          }

          private void set() {
              mList = sListModel.toArray();
              mIndex = 0;
              sCmdConn.setListener(this);
          }

          private void reset() {
              if (mList != null) {
                  for (AsterCommand cmd : mList) {
                      cmd.setExecuting(false);
                  }
                  sListModel.trigger();
                  mList = null;
              }
              mIndex = -1;
              mInPlaying = false;
              sCmdConn.setListener(null);
          }

          public void onPlayClicked() {
              mInPlaying = true;
              onStepClicked();
          }

          public void onStepClicked() {
              if (mIndex == -1 || mList == null) {
                  set();
              }

              if (mIndex < mList.length) {
                  mDashboard.setRunning();
                  sCmdConn.runCommand(mList[mIndex]);
                  mList[mIndex].setExecuting(true);
                  sListModel.trigger();
                  mIndex++;
              } else {
                  onStopClicked();
              }
          }

          public void onStopClicked() {
              reset();
              mDashboard.resetButtons();
          }

          public void processResult(ExecutionResult result) {
              if(result.mSuccess && mIndex < mList.length) {
                  // process success and is not in the end. (1 for Recall)
                  if (mInPlaying) {
                      this.onStepClicked(); // go to next step automatically
                  } else {
                      mDashboard.setStep();
                  }
              } else {
                  if (!result.mSuccess) {
                      if (mIndex == 1 && !sCmdMgr.getSaved()) {
                          JOptionPane.showMessageDialog(null,
                                  "Since you have set recall script, you need to \n"
                                  + "save this script first for Aster to locate the\n"
                                  + " recall script.",
                                  "Execution failed",
                                  JOptionPane.ERROR_MESSAGE);
                      } else {
                          System.out.println("\nFailed\n");
                          JOptionPane.showMessageDialog(null,
                                  result.mMessage,
                                  "Execution failed",
                                  JOptionPane.ERROR_MESSAGE);
                      }
                  }
                  reset();
                  mDashboard.resetButtons();
              }
          }
    }
}
