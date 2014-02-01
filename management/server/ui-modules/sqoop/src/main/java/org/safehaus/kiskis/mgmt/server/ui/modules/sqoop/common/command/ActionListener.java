package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import java.util.logging.Logger;

public class ActionListener {

    protected final Logger LOG = Logger.getLogger(getClass().getName());
    protected String expectedRegex[];

    public ActionListener(String ... expectedRegex) {
        this.expectedRegex = expectedRegex;
    }

    public void onExecute(Context context, String programLine) {
        LOG.info("Executing command: " + programLine);
    }

    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {

        LOG.info(
                "\n---"
                + "\n stdOut: " + stdOut
                + "\n stdErr: " + stdErr
                + "\n response: " + response
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
