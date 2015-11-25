package io.subutai.core.lxc.quota.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.resource.ResourceType;
import io.subutai.common.test.SystemOutRedirectTest;

import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class ListQuotaTypesTest extends SystemOutRedirectTest
{
    ListQuotaTypes listQuotaTypes;


    @Before
    public void setUp() throws Exception
    {
        listQuotaTypes = new ListQuotaTypes();
    }


    @Test
    public void testDoExecute() throws Exception
    {
        listQuotaTypes.doExecute();
        for ( final ResourceType resourceType : ResourceType.values() )
        {
            assertTrue( getSysOut().contains( resourceType.getKey() ) );
        }
    }
}