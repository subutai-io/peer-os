/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.core.tracker.api;


import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.tracker.TrackerOperationView;


/**
 * This is an interface for Tracker
 */
public interface Tracker
{

    /**
     * Get view of operation by operation id
     *
     * @param source - source of operation, usually this is a module name
     * @param operationTrackId - id of operation
     *
     * @return - operation view
     */
    TrackerOperationView getTrackerOperation( String source, UUID operationTrackId );

    /**
     * Creates operation and save it to DB
     *
     * @param source - source of operation, usually this is a module
     * @param description - description of operation
     *
     * @return - returns created operation
     */
    TrackerOperation createTrackerOperation( String source, String description );

    /**
     * Returns list of operations (views) filtering them by date interval
     *
     * @param source - source of operation, usually this is a module
     * @param fromDate - beginning date of filter
     * @param toDate - ending date of filter
     * @param limit - limit of records to return
     *
     * @return - list of operation views
     */
    List<TrackerOperationView> getTrackerOperations( String source, Date fromDate, Date toDate, int limit );

    /**
     * Returns list of all sources of operations for which operations exist in DB
     *
     * @return list of operation sources
     */
    List<String> getTrackerOperationSources();

    /**
     * Prints log of operation to std out stream
     *
     * @param source - source of operation, usually this is a module name
     * @param operationTrackId - id of operation
     * @param maxOperationDurationMs - max operation duration timeout after which printing ceases
     */
    void printOperationLog( String source, UUID operationTrackId, long maxOperationDurationMs );


    /**
     * changes viewState
     * @param source
     * @param operationId
     * @param viewed
     */
    void setOperationViewState( String source, UUID operationId, boolean viewed ) throws SQLException;


    /**
     * changes all viewStates of user
     * @param viewed
     */
    void setOperationsViewStates( boolean viewed ) throws SQLException;


    /**
     * get not viewed operations
     * @throws SQLException
     */
    List<TrackerOperationView> getNotifications() throws SQLException;
}
