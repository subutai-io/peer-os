package org.safehaus.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;


public class QuotaManagerImplTest2
{
    QuotaEnum parameter = QuotaEnum.MEMORY_LIMIT_IN_BYTES;
    QuotaManagerImpl quotaManager;


    @Before
    public void setupClasses()
    {
        quotaManager = new QuotaManagerImpl();
    }


    @Test
    public void testSetQuota() throws Exception
    {
        Container container = mock( Container.class );
        quotaManager.setQuota( container, parameter, "200M" );
        String value = quotaManager.getQuota( container, parameter );
        assertEquals( "200M", value );
    }


    @Test
    public void testGetQuota() throws Exception
    {
        Container container = mock( Container.class );
        String value = quotaManager.getQuota( container, parameter );
        quotaManager.setQuota( container, parameter, "200M" );
        assertNotEquals( value, "200M" );
    }
}