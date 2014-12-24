package org.safehaus.subutai.plugin.common.mock;


import java.util.Date;
import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;


public class TrackerOperationMock implements TrackerOperation
{
    private final StringBuilder log = new StringBuilder();
    private final UUID uuid = UUID.randomUUID();
    private final Date created = new Date();
    private OperationState state = OperationState.RUNNING;


    @Override
    public String getDescription()
    {
        return "desc";
    }


    @Override
    public UUID getId()
    {
        return uuid;
    }


    @Override
    public String getLog()
    {
        return log.toString();
    }


    @Override
    public Date createDate()
    {
        return created;
    }


    @Override
    public OperationState getState()
    {
        return state;
    }


    @Override
    public void addLog( String logString )
    {
        log.append( logString );
    }


    @Override
    public void addLogDone( String logString )
    {
        addLog( logString );
        state = OperationState.SUCCEEDED;
    }


    @Override
    public void addLogFailed( String logString )
    {
        addLog( logString );
        state = OperationState.FAILED;
    }
}
