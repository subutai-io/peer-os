package org.safehaus.agent.command.model;


import java.util.UUID;


/**
 * Created with IntelliJ IDEA.
 * User: akarasulu
 * Date: 8/25/13
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExecuteResponse extends AbstractResponse<ExecuteCommand>
{
    private final int pid;
    private StringBuilder stderr = new StringBuilder();
    private StringBuilder stdout = new StringBuilder();


    protected ExecuteResponse( ExecuteCommand request, int pid, long responseSeqNum, UUID issuerId, UUID agentId, UUID messageId )
    {
        super( request, responseSeqNum, issuerId, agentId, messageId );
        this.pid = pid;
    }


    @Override
    public final Type getType()
    {
        return Type.EXECUTE_RESPONSE;
    }


    /**
     * Gets the POSIX system process id on the agent's host.
     *
     * @return the pid of the process
     */
    public final int getPid()
    {
        return pid;
    }


    public void appendStderr( String errput )
    {
        stderr.append( errput );
    }


    public void appendStdout( String output )
    {
        stdout.append( output );
    }


    public String getStderr()
    {
        return stderr.toString();
    }


    public String getStdout()
    {
        return stdout.toString();
    }
}
