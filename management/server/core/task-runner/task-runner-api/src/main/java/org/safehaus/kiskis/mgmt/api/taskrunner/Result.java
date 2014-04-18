/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

/**
 * This class is used to hold a result of execution of a single request to a
 * single agent/node
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

    /**
     * @return - cumulated std out
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * @return - cumulated std err
     */
    public String getStdErr() {
        return stdErr;
    }

    /**
     * @return - exit code of executed command or null if timeout occurred
     */
    public Integer getExitCode() {
        return exitCode;
    }

}
