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

import org.zeroxlab.aster.cmds.AsterCommand;
import org.zeroxlab.aster.cmds.AsterCommand.ExecutionResult;

/**
 * Control for the play/step/stop buttons
 * 
 * @author liuyq
 * 
 */
class PlayStepStopController {

    private static PlayStepStopController sController;
    private static ScreenUpdateSession sCmdConn;
    private static IActionListContoller sListModel;
    private static PlayStepStopButtonActionExecutor sExecutor;

    public static PlayStepStopController getInstance() {
        if (sController == null) {
            sController = new PlayStepStopController();
        }

        return sController;
    }

    private PlayStepStopController() {
        sExecutor = new PlayStepStopButtonActionExecutor();
    }

    /*FIXME: Its bad since the Connection is always needed*/
    public static void setConnection(ScreenUpdateSession conn) {
        sCmdConn = conn;
    }

    /*FIXME: Its bad since the Model is always needed*/
    public static void setModel(IActionListContoller model) {
        sListModel = model;
    }

    public static PlayStepStopButtonActionExecutor getCommandExecutionListener() {
        return sExecutor;
    }

    class PlayStepStopButtonActionExecutor implements PlayStepStopPannel.PlayStepStopButtonClickListener,
                                    ICommandExecutionListener {
          AsterCommand[] mList;

          /*FIXME: Controller should not use Dashboard directly */
          PlayStepStopPannel mButtonPanel;

          int mIndex; // refer to a command which is going to be executed
          // but not happen yet
          boolean mInPlaying = false;

          PlayStepStopButtonActionExecutor() {
              mButtonPanel = PlayStepStopPannel.getInstance();
              mButtonPanel.setListener(this);
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
                  mButtonPanel.setRunning();
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
              mButtonPanel.resetButtons();
          }

          public void processResult(ExecutionResult result) {
            if ( /* result.mSuccess && */mIndex < mList.length) {
                  // process success and is not in the end. (1 for Recall)
                  if (mInPlaying) {
                      this.onStepClicked(); // go to next step automatically
                  } else {
                      mButtonPanel.setStep();
                  }
              } else {
                // if (!result.mSuccess) {
                if (mIndex != mList.length) {
                    if (mIndex == 1 /* && !sCmdMgr.getSaved() */) {
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
                  mButtonPanel.resetButtons();
              }
          }
    }
}
