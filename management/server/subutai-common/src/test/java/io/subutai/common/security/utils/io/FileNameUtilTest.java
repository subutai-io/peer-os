package io.subutai.common.security.utils.io;


import org.junit.Test;

import io.subutai.common.security.utils.io.FileNameUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class FileNameUtilTest
{
    private FileNameUtil fileNameUtil = new FileNameUtil();

    @Test
    public void testCleanFileName() throws Exception
    {
        assertNotNull( FileNameUtil.cleanFileName( "test" ) );
    }


    @Test
    public void testRemoveExtension() throws Exception
    {
        assertNotNull( FileNameUtil.removeExtension( "test.test" ) );
        assertNull( FileNameUtil.removeExtension( null ) );
    }


    @Test
    public void testRemoveExtensionNoExtensionFound() throws Exception
    {
        assertNotNull( FileNameUtil.removeExtension( "test" ) );
    }

}