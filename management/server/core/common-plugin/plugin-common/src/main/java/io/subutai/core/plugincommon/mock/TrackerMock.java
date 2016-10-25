package io.subutai.core.plugincommon.mock;


import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.tracker.TrackerOperationView;
import io.subutai.core.tracker.api.Tracker;


public class TrackerMock implements Tracker
{
    @Override
    public TrackerOperationView getTrackerOperation( String source, UUID operationTrackId )
    {
        return null;
    }


    @Override
    public TrackerOperation createTrackerOperation( String source, String description )
    {
        return new TrackerOperationMock();
    }


    @Override
    public List<TrackerOperationView> getTrackerOperations( String source, Date fromDate, Date toDate, int limit )
    {
        return Collections.emptyList();
    }


    @Override
    public List<String> getTrackerOperationSources()
    {
        return Collections.emptyList();
    }


    @Override
    public void printOperationLog( String source, UUID operationTrackId, long maxOperationDurationMs )
    {

    }


    @Override
    public void setOperationViewState( String source, UUID operationId, boolean viewed ) throws SQLException
    {

    }


    @Override
    public void setOperationsViewStates( boolean viewed ) throws SQLException
    {

    }


    @Override
    public List<TrackerOperationView> getNotifications() throws SQLException
    {
        return Collections.emptyList();
    }
}