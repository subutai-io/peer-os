package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain.Context;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class ActionListener {

    public static enum Result {
        CONTINUE, SKIP, INTERRUPT,
    }

    protected String expectedRegex[];

    public ActionListener(String ... expectedRegex) {
        this.expectedRegex = expectedRegex;
    }

    protected Result onStart(Context context, String programLine) {
        return Result.CONTINUE;
    }

    protected void onResponse(Context context, Response response) {}

    protected boolean onComplete(Context context, String stdOut, String stdErr, Response response) {
        return false;
    }

    // TODO with regex
    /*
    protected boolean allRegexMatched(String stdOut) {

        for (String regex : expectedRegex) {
            if (!stdOut.contains(regex)) {
                return false;
            }
        }

        return true;
    }
    */
}
