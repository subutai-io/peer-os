package org.safehaus.agent.command.model;


import java.util.UUID;


/**
 * A concrete execute command done response denoting the end of an ExecuteCommand.
 */
public class ExecuteResponseDone extends AbstractResponse<ExecuteCommand> implements Response
{
    private final int exitCode;
    private final int pid;


    protected ExecuteResponseDone( ExecuteCommand request, int pid, int exitCode, long responseSeqNum,
                                   UUID issuerId, UUID agentId, UUID messageId )
    {
        super( request, responseSeqNum, issuerId, agentId, messageId );
        this.exitCode = exitCode;
        this.pid = pid;
    }


    @Override
    public final Type getType()
    {
        return Type.EXECUTION_DONE;
    }


    /**
     * The exit code of the process.
     *
     * @return the process exit code
     */
    public final int getExitCode()
    {
        return exitCode;
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
}
