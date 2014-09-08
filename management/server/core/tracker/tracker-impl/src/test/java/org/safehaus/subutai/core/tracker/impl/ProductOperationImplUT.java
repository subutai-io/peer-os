/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import org.junit.Test;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.core.tracker.impl.ProductOperationImpl;
import org.safehaus.subutai.core.tracker.impl.ProductOperationViewImpl;
import org.safehaus.subutai.core.tracker.impl.TrackerImpl;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


/**
 * Test for ProductOperation class
 */
public class ProductOperationImplUT {

	private final UUID ID = UUID.randomUUID();
	private final String SOURCE = "source";
	private final String DUMMY_LOG = "log";
	private final String DESCRIPTION = "description";


	@Test (expected = IllegalArgumentException.class)
	public void constructorShouldFailNullSource() {
		new ProductOperationImpl(null, DESCRIPTION, mock(TrackerImpl.class));
	}


	@Test (expected = IllegalArgumentException.class)
	public void constructorShouldFailNullDescription() {
		new ProductOperationImpl(SOURCE, null, mock(TrackerImpl.class));
	}


	@Test (expected = NullPointerException.class)
	public void constructorShouldFailNullTracker() {
		new ProductOperationImpl(SOURCE, DESCRIPTION, null);
	}


	@Test
	public void shouldReturnValidValues() {
		ProductOperationImpl poi = new ProductOperationImpl(SOURCE, DESCRIPTION, mock(TrackerImpl.class));

		assertEquals(DESCRIPTION, poi.getDescription());
		assertEquals( ProductOperationState.RUNNING, poi.getState());
		assertNotNull(poi.createDate());
		assertNotNull(poi.getId());
	}


	@Test
	public void shouldAddLog() {
		ProductOperationImpl poi = new ProductOperationImpl(SOURCE, DESCRIPTION, mock(TrackerImpl.class));

		poi.addLog(DUMMY_LOG);

		assertEquals(DUMMY_LOG, poi.getLog());
	}


	@Test
	public void shouldAddLogNSucceed() {
		ProductOperationImpl poi = new ProductOperationImpl(SOURCE, DESCRIPTION, mock(TrackerImpl.class));

		poi.addLogDone(DUMMY_LOG);

		assertEquals(DUMMY_LOG, poi.getLog());

		assertEquals(ProductOperationState.SUCCEEDED, poi.getState());
	}


	@Test
	public void shouldAddLogNFail() {
		ProductOperationImpl poi = new ProductOperationImpl(SOURCE, DESCRIPTION, mock(TrackerImpl.class));

		poi.addLogFailed(DUMMY_LOG);

		assertEquals(DUMMY_LOG, poi.getLog());

		assertEquals(ProductOperationState.FAILED, poi.getState());
	}


	@Test
	public void shouldCallTracker() {
		TrackerImpl ti = mock(TrackerImpl.class);
		ProductOperationImpl poi = new ProductOperationImpl(SOURCE, DESCRIPTION, ti);

		poi.addLogFailed(DUMMY_LOG);

		verify(ti).saveProductOperation(SOURCE, poi);
	}


	@Test (expected = NullPointerException.class)
	public void poViewConstructorShouldFailNullPO() {
		new ProductOperationViewImpl(null);
	}


	@Test
	public void poViewShouldReturnSameValuesAsPO() {
		ProductOperationImpl poi = mock(ProductOperationImpl.class);
		when(poi.getId()).thenReturn(ID);
		when(poi.getDescription()).thenReturn(DESCRIPTION);
		when(poi.getState()).thenReturn(ProductOperationState.RUNNING);
		when(poi.getLog()).thenReturn(DUMMY_LOG);

		ProductOperationViewImpl povi = new ProductOperationViewImpl(poi);

		assertEquals(poi.getId(), povi.getId());
		assertEquals(poi.createDate(), povi.getCreateDate());
		assertEquals(poi.getLog(), povi.getLog());
		assertEquals(poi.getDescription(), povi.getDescription());
		assertEquals(poi.getState(), povi.getState());
	}
}
