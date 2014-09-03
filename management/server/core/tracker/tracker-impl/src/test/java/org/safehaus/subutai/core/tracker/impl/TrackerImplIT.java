/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.impl.DbManagerImpl;
import org.safehaus.subutai.core.tracker.impl.TrackerImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Test for TrackerImpl class
 */
public class TrackerImplIT {

	private final DbManager dbManager = new DbManagerImpl();
	private final Tracker tracker = new TrackerImpl();
	private final String source = "source";
	private final String description = "description";
	private final String testLog = "test";
	@Rule
	public CassandraCQLUnit cassandraCQLUnit =
			new CassandraCQLUnit(new ClassPathCQLDataSet("po.sql", true, true, "test"));


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

		tracker.createProductOperation(source, description);
		tracker.createProductOperation(source, description);

		Date endDate = new Date();
		Date startDate = new Date(endDate.getTime() - 5 * 1000);
		assertEquals(tracker.getProductOperations(source, startDate, endDate, 100).size(), 2);
	}


	@Test
	public void testGetProductOperationSources() {

		tracker.createProductOperation("source1", description);

		tracker.createProductOperation("source2", description);
		tracker.createProductOperation("source3", description);

		assertEquals(3, tracker.getProductOperationSources().size());
	}


	@Test
	public void testPrintOperationLog() {
		final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(myOut));
		ProductOperation po = tracker.createProductOperation(source, description);

		po.addLogDone(testLog);

		tracker.printOperationLog(source, po.getId(), 5 * 1000);

		assertEquals(testLog, myOut.toString());
	}
}
