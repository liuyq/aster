package org.zeroxlab.aster.cmds;

import java.io.IOException;

import javax.script.SimpleBindings;

import org.linaro.utils.Constants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.zeroxlab.aster.operations.AsterOperation;
import org.zeroxlab.aster.operations.OpGetInput;

public abstract class WaitIdWithTextAbstract extends AsterCommand {
    private static final String timeoutInSecond = "3600";
    private String targetId = "";
    private String checkText = "";
    private String timeout = "";

    public WaitIdWithTextAbstract() {
        targetId = new String();
        checkText = new String();
        timeout = new String();
        super.mOps = new AsterOperation[3];
        super.mOps[0] = new OpGetInput(
                "Please input the resource-id you want to check", "",
                "targetId");
        super.mOps[1] = new OpGetInput(
                "Please input the text you want to check with", "", "checkText");
        super.mOps[2] = new OpGetInput(
                "Please input the time out value you want to wait",
                timeoutInSecond, "timeout");
    }

    public WaitIdWithTextAbstract(String rootPath, String line) {
        super.setFilled(true);
        String[] args = splitArgs(line);

        if (args.length == 3) {
            targetId = stripQuote(args[0]);
            checkText = stripQuote(args[1]);
            timeout = stripQuote(args[2]);
        } else {
            throw new IllegalArgumentException("Invalid argument line.");
        }
        mOps = new AsterOperation[3];
        super.mOps[0] = new OpGetInput(
                "Please input the resource-id you want to check", targetId,
                "targetId");
        super.mOps[1] = new OpGetInput(
                "Please input the text you want to check with", checkText,
                "checkText");
        super.mOps[2] = new OpGetInput(
                "Please input the time out value you want to wait", timeout,
                "timeout");
    }

    @Override
    public void execute() {
        if (targetId != null && !targetId.isEmpty() && checkText != null
                && !checkText.isEmpty() && timeout != null
                && !timeout.isEmpty()) {
            long st = System.nanoTime();
            while (true) {
                if (!checkIfMatch(targetId, checkText)) {
                    if (((System.nanoTime() - st) / 1000000000.0) >= Integer
                            .parseInt(timeout)) {
                        if (Constants.DEBUG_IMAGE_MATCH) {
                            String output = String
                                    .format("Specified id(%s) does not displayed with text(%s) in %s seconds",
                                            targetId, checkText, timeout);
                            System.out.println(output);
                        }
                        break;
                    }
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                } else {
                    if (Constants.DEBUG_IMAGE_MATCH) {
                        String output = String
                                .format("Specified id(%s) does displayed with text(%s) in %s seconds",
                                        targetId, checkText, timeout);
                        System.out.println(output);
                    }
                    return;
                }
            }
        }
    }

    protected abstract boolean shouldMatch();

    public boolean checkIfMatch(String targetId, String checkText) {
        NodeList nl = device.getAllNodes();
        if (nl == null || nl.getLength() == 0) {
            return false;
        }
        for (int i = 0; i < nl.getLength(); i++) {
            NamedNodeMap attributes = nl.item(i).getAttributes();
            if (null != attributes) {
                String text = attributes.getNamedItem("resource-id")
                        .getNodeValue();
                if (text.equals(targetId)) {
                    if (checkText == null || checkText.trim().isEmpty()) {
                        return true;
                    }
                    String textVal = attributes.getNamedItem("text")
                            .getNodeValue();
                    if (shouldMatch() && textVal.matches(checkText)) {
                        String contentDesc = attributes.getNamedItem(
                                "content-desc").getNodeValue();
                        String output = String.format(
                                "resouce-id=%s,text=%s,content-desc=%s",
                                targetId, textVal, contentDesc);
                        System.out.println(output);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public SimpleBindings getSettings() {
        SimpleBindings settings = new SimpleBindings();
        settings.put("targetId", targetId);
        settings.put("checkText", checkText);
        settings.put("timeout", timeout);
        return settings;
    }

    @Override
    protected void onFillSettings(SimpleBindings settings) throws IOException {
        if (settings == null) {
            return;
        }

        if (settings.containsKey("targetId")) {
            targetId = (String) settings.get("targetId");
        }
        if (settings.containsKey("checkText")) {
            checkText = (String) settings.get("checkText");
        }
        if (settings.containsKey("timeout")) {
            timeout = (String) settings.get("timeout");
        }

    }

    @Override
    public String toScript() {
        return String.format("%s('%s', '%s', '%s')\n", getName(), targetId,
                checkText, timeout);
    }

    @Override
    protected String getCommandPrefix() {
        return getName();
    }
}
