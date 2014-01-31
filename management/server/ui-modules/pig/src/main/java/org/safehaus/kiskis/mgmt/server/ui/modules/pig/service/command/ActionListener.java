package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.logging.Logger;

public class ActionListener {

    private final Logger LOG = Logger.getLogger(getClass().getName());
    private final String EXPECTED_REGEX[];

    public ActionListener(String ... expectedRegex) {
        EXPECTED_REGEX = expectedRegex;
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
    private boolean allRegexMatched(String stdOut) {

        for (String regex : EXPECTED_REGEX) {
            if (!stdOut.contains(regex)) {
                return false;
            }
        }

        return true;
    }

}
