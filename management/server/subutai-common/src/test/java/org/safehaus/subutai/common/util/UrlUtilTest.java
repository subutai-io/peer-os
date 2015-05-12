package org.safehaus.subutai.common.util;


import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class UrlUtilTest
{
    private UrlUtil urlUtil = new UrlUtil();


    @Test
    public void testGetQueryParameterValue() throws Exception
    {
        assertNotNull( UrlUtil.getQueryParameterValue( "test", "test=sdf" ) );
    }
}