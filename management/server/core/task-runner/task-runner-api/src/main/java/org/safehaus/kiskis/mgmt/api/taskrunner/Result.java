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
     * @return - cumulated last 10000 symbols of std out, if all std out is more
     * than 10000, only last 10000 is returned
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * @return - cumulated last 10000 symbols of std err, if all std err is more
     * than 10000, only last 10000 is returned
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
