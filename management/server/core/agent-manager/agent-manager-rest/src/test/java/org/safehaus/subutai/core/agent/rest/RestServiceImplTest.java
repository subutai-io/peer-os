package org.safehaus.subutai.core.agent.rest;


import org.junit.Test;


/**
 * Test for RestServiceImpl
 */
public class RestServiceImplTest
{

    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new RestServiceImpl( null );
    }



}
