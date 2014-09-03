/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.commandrunner.impl;


import java.util.UUID;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.common.protocol.Response;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Implementation of AgentResult interface
 */
public class AgentResultImpl implements AgentResult {

    //tagent UUID
    private final UUID agentUUID;
    //std out of command execution
    private final StringBuilder stdOut = new StringBuilder();
    //std err of command execution
    private final StringBuilder stdErr = new StringBuilder();
    //exit code of command execution
    private Integer exitCode;


    /**
     * Constructor
     *
     * @param agentUUID - UUID of agent
     */
    public AgentResultImpl( UUID agentUUID ) {
        Preconditions.checkNotNull( agentUUID, "Agent UUID is null" );

        this.agentUUID = agentUUID;
    }


    /**
     * When a response arrives this method is called by command runner to append results of command execution to this
     * object
     *
     * @param response - received response
     */
    public void appendResults( Response response ) {
        if ( response != null && exitCode == null && agentUUID.equals( response.getUuid() ) ) {
            if ( !Strings.isNullOrEmpty( response.getStdOut() ) ) {
                stdOut.append( response.getStdOut() );
            }
            if ( !Strings.isNullOrEmpty( response.getStdErr() ) ) {
                stdErr.append( response.getStdErr() );
            }
            if ( response.isFinal() && response.getExitCode() != null ) {
                exitCode = response.getExitCode();
            }
        }
    }


    /**
     * Returns command exit code
     */
    public Integer getExitCode() {
        return exitCode;
    }


    /**
     * Returns command cumulative std output
     */
    public String getStdOut() {
        return stdOut.toString();
    }


    /**
     * Returns command cumulative err output
     */
    public String getStdErr() {
        return stdErr.toString();
    }


    /**
     * Returns target agent uuid
     */
    public UUID getAgentUUID() {
        return agentUUID;
    }
}
