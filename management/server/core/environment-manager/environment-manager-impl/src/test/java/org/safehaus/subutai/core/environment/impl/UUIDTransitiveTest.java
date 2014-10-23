package org.safehaus.subutai.core.environment.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.util.JsonUtil;


/**
 * Created by bahadyr on 10/22/14.
 */
public class UUIDTransitiveTest
{


    @Before
    public void setUp() throws Exception
    {


    }


    @Test
    public void testTransUUID() throws Exception
    {
        System.out.println( JsonUtil.toJson( new TransTest() ) );
    }


    static class TransTest
    {
        UUID testUUID;
        transient UUID transUUID;


        public TransTest()
        {
            this.testUUID = UUID.randomUUID();
            this.transUUID = UUID.randomUUID();
        }
    }
}
