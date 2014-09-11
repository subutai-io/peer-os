/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.api;


import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationView;

import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * This is an interface for Tracker
 */
public interface Tracker
{

    /**
     * Get view of product operation by operation id
     *
     * @param source           - source of product operation, usually this is a module name
     * @param operationTrackId - id of operation
     * @return - product operation view
     */
    public ProductOperationView getProductOperation( String source, UUID operationTrackId );

    /**
     * Creates product operation and save it to DB
     *
     * @param source      - source of product operation, usually this is a module
     * @param description - description of operation
     * @return - returns created product operation
     */
    public ProductOperation createProductOperation( String source, String description );

    /**
     * Returns list of product operations (views) filtering them by date interval
     *
     * @param source   - source of product operation, usually this is a module
     * @param fromDate - beginning date of filter
     * @param toDate   - ending date of filter
     * @param limit    - limit of records to return
     * @return - list of product operation views
     */
    public List<ProductOperationView> getProductOperations( String source, Date fromDate, Date toDate, int limit );

    /**
     * Returns list of all sources of product operations for which product operations exist in DB
     *
     * @return list of product operation sources
     */
    public List<String> getProductOperationSources();

    /**
     * Prints log of product operation to std out stream
     *
     * @param source                 - source of product operation, usually this is a module name
     * @param operationTrackId       - id of operation
     * @param maxOperationDurationMs - max operation duration timeout after which printing ceases
     */
    public void printOperationLog( String source, UUID operationTrackId, long maxOperationDurationMs );
}
