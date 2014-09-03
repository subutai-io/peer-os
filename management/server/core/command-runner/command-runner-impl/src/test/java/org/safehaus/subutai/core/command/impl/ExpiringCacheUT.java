/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.command.impl.ExpiringCache;

import static org.junit.Assert.*;


/**
 * Test for ExpiringCache class
 */
public class ExpiringCacheUT {

	private final Object KEY = new Object();
	private final Object VALUE = new Object();
	private final Integer TIME_TO_LIVE_MS = 100;
	private ExpiringCache cache;


	@Before
	public void beforeMethod() {
		cache = new ExpiringCache();
	}


	@After
	public void afterMethod() {
		cache.dispose();
	}


	@Test
	public void shouldReturnValue() {

		cache.put(KEY, VALUE, TIME_TO_LIVE_MS);

		assertNotNull(cache.get(KEY));
	}


	@Test
	public void shouldReturnNull() {

		assertNull(cache.get(KEY));
	}


	@Test
	public void shouldRemoveValue() {

		cache.put(KEY, VALUE, TIME_TO_LIVE_MS);
		cache.remove(KEY);

		assertNull(cache.get(KEY));
	}


	@Test
	public void shouldClearValues() {

		cache.put(KEY, VALUE, TIME_TO_LIVE_MS);
		cache.clear();

		assertNull(cache.get(KEY));
	}


	@Test
	public void shouldReturnEntries() {

		cache.put(KEY, VALUE, TIME_TO_LIVE_MS);

		assertFalse(cache.getEntries().isEmpty());
	}


	@Test
	public void shouldExpireEntry() throws InterruptedException {

		cache.put(KEY, VALUE, TIME_TO_LIVE_MS);

		Thread.sleep(TIME_TO_LIVE_MS + 1);

		assertNull(cache.get(KEY));
	}


	@Test
	public void shouldNotExpireEntry() throws InterruptedException {

		cache.put(KEY, VALUE, TIME_TO_LIVE_MS);

		Thread.sleep(50);

		assertNotNull(cache.get(KEY));
	}
}
