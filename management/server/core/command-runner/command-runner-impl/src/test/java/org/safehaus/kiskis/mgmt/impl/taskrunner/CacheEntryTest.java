/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import static org.junit.Assert.*;
import org.junit.Test;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CacheEntry;

/**
 *
 * @author dilshat
 */
public class CacheEntryTest {

    private final Object SOME_VALUE = new Object();
    private final Integer TIME_TO_LIVE_MS = 100;

    @Test(expected = NullPointerException.class)
    public void constructorShouldFailNullValue() {
         new CacheEntry(null, TIME_TO_LIVE_MS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailZeroTtl() {
         new CacheEntry(SOME_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailNegativeTtl() {
        new CacheEntry(SOME_VALUE, -10);
    }

    @Test
    public void shouldReturnValue() {
        CacheEntry cacheEntry = new CacheEntry(SOME_VALUE, TIME_TO_LIVE_MS);

        assertEquals(SOME_VALUE, cacheEntry.getValue());
    }

    @Test
    public void entryShouldBeExpired() throws InterruptedException {
        CacheEntry cacheEntry = new CacheEntry(SOME_VALUE, TIME_TO_LIVE_MS);

        Thread.sleep(101);

        assertTrue(cacheEntry.isExpired());
    }

    @Test
    public void entryShouldNotBeExpired() throws InterruptedException {
        CacheEntry cacheEntry = new CacheEntry(SOME_VALUE, TIME_TO_LIVE_MS);

        Thread.sleep(90);

        assertFalse(cacheEntry.isExpired());
    }
}
