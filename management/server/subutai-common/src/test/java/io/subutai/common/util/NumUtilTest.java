package io.subutai.common.util;


import org.junit.Test;

import io.subutai.common.util.NumUtil;


public class NumUtilTest
{
    @Test
    public void testIsIntBetween() throws Exception
    {
        NumUtil.isIntBetween( 5, 1, 10 );
    }
}