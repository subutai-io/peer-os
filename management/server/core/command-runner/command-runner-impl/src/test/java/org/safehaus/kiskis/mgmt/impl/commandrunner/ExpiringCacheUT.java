/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.commandrunner;


import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

import static com.jayway.awaitility.Awaitility.to;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


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

        cache.put( KEY, VALUE, TIME_TO_LIVE_MS );

        assertNotNull( cache.get( KEY ) );
    }


    @Test
    public void shouldReturnNull() {

        assertNull( cache.get( KEY ) );
    }


    @Test
    public void shouldRemoveValue() {

        cache.put( KEY, VALUE, TIME_TO_LIVE_MS );
        cache.remove( KEY );

        assertNull( cache.get( KEY ) );
    }


    @Test
    public void shouldClearValues() {

        cache.put( KEY, VALUE, TIME_TO_LIVE_MS );
        cache.clear();

        assertNull( cache.get( KEY ) );
    }


    @Test
    public void shouldReturnEntries() {

        cache.put( KEY, VALUE, TIME_TO_LIVE_MS );

        assertFalse( cache.getEntries().isEmpty() );
    }


    @Test
    public void shouldExpireEntry() {

        cache.put( KEY, VALUE, TIME_TO_LIVE_MS );

        Awaitility.await().atMost( 200, TimeUnit.MILLISECONDS ).
                with().pollInterval( 10, TimeUnit.MILLISECONDS ).
                          untilCall( to( cache ).get( KEY ), equalTo( null ) );
    }


    @Test
    public void shouldNotExpireEntry() throws InterruptedException {

        cache.put( KEY, VALUE, TIME_TO_LIVE_MS );

        Thread.sleep( 50 );

        assertNotNull( cache.get( KEY ) );
    }
}
