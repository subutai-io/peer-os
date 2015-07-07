package io.subutai.core.lxc.quota.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;

import io.subutai.core.lxc.quota.cli.ListQuotaTypes;

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
        for ( final QuotaType quotaType : QuotaType.values() )
        {
            assertTrue( getSysOut().contains( quotaType.getKey() ) );
        }
    }
}