/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.common.command;


/**
 * Status of command
 */
public enum CommandStatus
{

    /**
     * command just created
     */
    NEW,
    /**
     * command has been just sent to container
     */
    RUNNING,
    /**
     * command had timed out before container sent response
     */
    TIMEOUT,
    /**
     * command succeeded, exit code was 0
     */
    SUCCEEDED,
    /**
     * command failed, exit code was not 0
     */
    FAILED,
    /**
     * command was killed by agent due to timeout
     */
    KILLED
}
