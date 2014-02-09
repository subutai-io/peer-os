package org.safehaus.kiskismgmt.protocol;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: aralbaev
 * Date: 10/21/13
 * Time: 5:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentOutput implements Serializable {
    private String stdOut;
    private String stdErr;

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }
}
