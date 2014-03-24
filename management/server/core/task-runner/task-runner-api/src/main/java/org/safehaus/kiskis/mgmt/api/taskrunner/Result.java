/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

/**
 *
 * @author dilshat
 */
public class Result {

    private final String stdOut;
    private final String stdErr;
    private final Integer exitCode;

    public Result(String stdOut, String stdErr, Integer exitCode) {
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.exitCode = exitCode;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public Integer getExitCode() {
        return exitCode;
    }

}
