/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.tracker;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.api.dbmanager.DbManager;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for TrackerImpl class
 */
public class TrackerImplUT {

	private final UUID poID = UUID.randomUUID();
	private final String SOURCE = "source";
	private final String DESCRIPTION = "description";

	private DbManager dbManager;
	private TrackerImpl ti;


	@Before
	public void setupMethod() {
		dbManager = mock(DbManager.class);
		ti = new TrackerImpl();
		ti.setDbManager(dbManager);
	}


	@Test
	public void shouldCallDbManagerExecuteUpdateWhenCreatePO() {

		ti.createProductOperation(SOURCE, DESCRIPTION);

		verify(dbManager).executeUpdate(any(String.class), anyVararg());
	}


	@Test
	public void shouldCallDbManagerExecuteUpdateWhenSavePO() {

		ProductOperationImpl poi = new ProductOperationImpl(SOURCE, DESCRIPTION, ti);

		ti.saveProductOperation(SOURCE, poi);

		verify(dbManager).executeUpdate(any(String.class), anyVararg());
	}


	@Test
	public void shouldCallDbManagerExecuteQueryWhenGetPO() {

		ti.getProductOperation(SOURCE, poID);

		verify(dbManager).executeQuery(any(String.class), anyVararg());
	}


	@Test
	public void shouldCallDbManagerExecuteQueryWhenGetPOs() {

		ti.getProductOperations(SOURCE, mock(Date.class), mock(Date.class), 1);

		verify(dbManager).executeQuery(any(String.class), anyVararg());
	}


	@Test
	public void shouldCallDbManagerExecuteQueryWhenGetPOSources() {

		ti.getProductOperationSources();

		verify(dbManager).executeQuery(any(String.class), anyVararg());
	}


	@Test
	public void shouldCallDbManagerExecuteQueryWhenPrintOperationLog() {

		ti.printOperationLog(SOURCE, poID, 100);

		verify(dbManager).executeQuery(any(String.class), anyVararg());
	}
}
