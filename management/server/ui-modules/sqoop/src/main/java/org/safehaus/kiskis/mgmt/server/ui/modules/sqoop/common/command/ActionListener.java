package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class ActionListener {

    protected String expectedRegex[];

    public ActionListener(String ... expectedRegex) {
        this.expectedRegex = expectedRegex;
    }

    public void onExecute(Context context, String programLine) {}

    public boolean onResponse(Context context, String stdOut, String stdErr, Response response) {
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
