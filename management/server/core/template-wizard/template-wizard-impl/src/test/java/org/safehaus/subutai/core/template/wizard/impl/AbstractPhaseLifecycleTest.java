package org.safehaus.subutai.core.template.wizard.impl;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class AbstractPhaseLifecycleTest
{

    AbstractPhaseLifeCycleMock phaseLifecycle;
    AbstractPhaseLifecycleListenerMock lifecycleListener;


    @Before
    public void setUp() throws Exception
    {
        lifecycleListener = new AbstractPhaseLifecycleListenerMock();
        phaseLifecycle = new AbstractPhaseLifeCycleMock();
        phaseLifecycle.addLifeCycleListener( lifecycleListener );
    }


    @Test
    public void testDoStart() throws Exception
    {
        phaseLifecycle.doStart();
    }


    @Test
    public void testDoStop() throws Exception
    {
        phaseLifecycle.doStop();
    }


    @Test
    public void testStart() throws Exception
    {
        phaseLifecycle.start();
        assertTrue( phaseLifecycle.isStarted() );
    }


    @Test
    public void testStop() throws Exception
    {
        phaseLifecycle.start();
        phaseLifecycle.stop();
        assertTrue( phaseLifecycle.isStopped() );
    }


    @Test
    public void testIsRunning() throws Exception
    {
        phaseLifecycle.start();
        assertTrue( phaseLifecycle.isRunning() );
    }


    @Test
    public void testIsStarted() throws Exception
    {
        phaseLifecycle.start();
        assertTrue( phaseLifecycle.isStarted() );
    }


    @Test
    public void testIsStarting() throws Exception
    {
        phaseLifecycle.start();
        assertFalse( phaseLifecycle.isStarting() );
    }


    @Test
    public void testIsStopping() throws Exception
    {
        phaseLifecycle.stop();
        assertFalse( phaseLifecycle.isStopping() );
    }


    @Test
    public void testIsStopped() throws Exception
    {
        phaseLifecycle.stop();
        assertTrue( phaseLifecycle.isStopped() );
    }


    @Test
    public void testIsFailed() throws Exception
    {
        phaseLifecycle.start();
        assertFalse( phaseLifecycle.isFailed() );
    }


    @Test
    public void testAddLifeCycleListener() throws Exception
    {
        phaseLifecycle.addLifeCycleListener( lifecycleListener );
    }


    @Test
    public void testRemoveLifeCycleListener() throws Exception
    {
        phaseLifecycle.addLifeCycleListener( lifecycleListener );
        phaseLifecycle.removeLifeCycleListener( lifecycleListener );
    }


    @Test
    public void testGetState() throws Exception
    {
        phaseLifecycle.start();
        phaseLifecycle.getState();

        phaseLifecycle.stop();
        phaseLifecycle.getState();
    }


    @Test
    public void testGetState1() throws Exception
    {
        phaseLifecycle.start();
        AbstractPhaseLifeCycleMock.getState( phaseLifecycle );

        phaseLifecycle.stop();
        AbstractPhaseLifeCycleMock.getState( phaseLifecycle );
    }


    private class AbstractPhaseLifeCycleMock extends AbstractPhaseLifecycle
    {
    }


    private class AbstractPhaseLifecycleListenerMock extends AbstractPhaseLifecycle.AbstractPhaseLifeCycleListener
    {
    }
}