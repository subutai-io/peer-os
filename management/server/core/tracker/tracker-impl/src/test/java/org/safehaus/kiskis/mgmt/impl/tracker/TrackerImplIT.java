/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.tracker;

import java.util.Date;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.impl.dbmanager.DbManagerImpl;

/**
 *
 * @author dilshat
 */
public class TrackerImplIT {

    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("po.sql", true, true, "test"));
    private final DbManager dbManager = new DbManagerImpl();
    private final Tracker tracker = new TrackerImpl();

    private final String source = "source";
    private final String description = "description";

    @Before
    public void setUp() {
        ((DbManagerImpl) dbManager).setSession(cassandraCQLUnit.session);
        ((TrackerImpl) tracker).setDbManager(dbManager);
    }

    @After
    public void tearDown() {
        dbManager.executeUpdate("truncate product_operation;");
    }

    @Test
    public void testCreateNGetProductOperation() {

        ProductOperation po = tracker.createProductOperation(source, description);

        assertNotNull(tracker.getProductOperation(source, po.getId()));
    }

    @Test
    public void testGetProductOperations() {

        ProductOperation po = tracker.createProductOperation(source, description);
        po = tracker.createProductOperation(source, description);

        Date endDate = new Date();
        Date startDate = new Date(endDate.getTime() - 5 * 1000);
        assertEquals(tracker.getProductOperations(source, startDate, endDate, 100).size(), 2);
    }

    @Test
    public void testGetProductOperationSources() {

        ProductOperation po = tracker.createProductOperation("source1", description);

        po = tracker.createProductOperation("source2", description);
        po = tracker.createProductOperation("source3", description);

        assertEquals(3, tracker.getProductOperationSources().size());

    }

}
