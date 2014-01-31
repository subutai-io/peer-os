package org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.logging.Logger;

public class ActionListener {

    private final Logger LOG = Logger.getLogger(getClass().getName());
    protected String expectedRegex[];

    public ActionListener(String ... expectedRegex) {
        this.expectedRegex = expectedRegex;
    }

    public void onExecute(Context context, String programLine) {
        LOG.info("Executing command: " + programLine);
    }

    public boolean onResponse(Context context, String stdOut, String stdErr, ResponseType responseType) {

        LOG.info(
                "\n---"
                + "\n stdOut: " + stdOut
                + "\n stdErr: " + stdErr
                + "\n responseType: " + responseType
                + "\n expected regex matched: " + allRegexMatched(stdOut)
                + "\n---\n"
        );


        return false;
    }

    // TODO with regex
    protected boolean allRegexMatched(String stdOut) {

        for (String regex : expectedRegex) {
            if (!stdOut.contains(regex)) {
                return false;
            }
        }

        return true;
    }

}
