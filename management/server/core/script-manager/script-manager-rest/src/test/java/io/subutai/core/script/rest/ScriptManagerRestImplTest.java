package io.subutai.core.script.rest;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.util.JsonUtil;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;

import com.google.common.reflect.TypeToken;

import io.subutai.core.script.rest.ScriptManagerRestImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ScriptManagerRestImplTest
{
    private static final String SCRIPTS_PATH = "./";
    private static final String SCRIPT_NAME = "test.sh";


    static class SUT extends ScriptManagerRestImpl
    {
        SUT()
        {
            this.scriptsDirectoryPath = SCRIPTS_PATH;
        }
    }


    SUT sut;
    boolean fileCreated = false;
    File file;


    @Before
    public void setUp() throws Exception
    {
        sut = new SUT();
        file = new File( SCRIPTS_PATH + "/" + SCRIPT_NAME );
        fileCreated = file.exists() || file.createNewFile();
    }


    @After
    public void tearDown() throws Exception
    {
        if ( fileCreated )
        {
            file.delete();
        }
    }


    @Test
    public void testUploadScript() throws Exception
    {
        Assume.assumeTrue( fileCreated );

        Attachment attachment = mock( Attachment.class );
        ContentDisposition disposition = mock( ContentDisposition.class );
        when( attachment.getContentDisposition() ).thenReturn( disposition );
        when( disposition.getParameter( "filename" ) ).thenReturn( SCRIPT_NAME );
        when( attachment.getObject( InputStream.class ) ).thenReturn( new FileInputStream( file ) );

        Response response = sut.uploadScript( attachment );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );


        InputStream in = mock( InputStream.class );
        doThrow( new IOException() ).when( in ).read( any( byte[].class ) );
        when( attachment.getObject( InputStream.class ) ).thenReturn( in );

        response = sut.uploadScript( attachment );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testRemoveScript() throws Exception
    {
        Assume.assumeTrue( fileCreated );

        Response response = sut.removeScript( SCRIPT_NAME );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );


        response = sut.removeScript( UUID.randomUUID().toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );


        response = sut.removeScript( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testDownloadScript() throws Exception
    {

        Assume.assumeTrue( fileCreated );

        Response response = sut.downloadScript( SCRIPT_NAME );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );


        response = sut.downloadScript( UUID.randomUUID().toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );


        response = sut.downloadScript( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testListScripts() throws Exception
    {

        Assume.assumeTrue( fileCreated );

        Response response = sut.listScripts();

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        Set<String> names =
                JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<String>>() {}.getType() );

        assertTrue( names.contains( SCRIPT_NAME ) );
    }
}
