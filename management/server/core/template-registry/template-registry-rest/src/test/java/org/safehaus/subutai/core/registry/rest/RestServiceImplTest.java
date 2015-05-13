package org.safehaus.subutai.core.registry.rest;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.datatypes.TemplateVersion;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test or RestServiceImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    private static final String ARCH = "arch";

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private static final String FILE_PATH = "test-file";


    private TemplateRegistry templateRegistry;
    private PeerManager peerManager;
    private RepositoryManager repositoryManager;
    private RestServiceImpl restService;
    private Template template;
    private List<Template> templates;

    private static final String TEMPLATE_NAME = "master";

    @Mock
    Attachment attachment;
    @Mock
    org.apache.cxf.jaxrs.ext.multipart.ContentDisposition contentDisposition;
    @Mock
    InputStream inputStream;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;


    @Before
    public void setupClasses() throws IOException
    {
        template = TestUtils.getParentTemplate();
        templates = Lists.newArrayList( template );
        templateRegistry = mock( TemplateRegistry.class );
        peerManager = mock( PeerManager.class );
        repositoryManager = mock( RepositoryManager.class );
        restService = new RestServiceImpl( repositoryManager, templateRegistry, peerManager );
    }


    @Test
    public void shouldGetResponseWithStatusNotFoundForGetTemplate()
    {
        when( templateRegistry.getTemplate( "" ) ).thenReturn( null );
        Response response = restService.getTemplate( "" );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldGetResponseWithTemplateAsJsonForGetTemplate()
    {
        when( templateRegistry.getTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) ) )
                .thenReturn( template );
        Response response = restService.getTemplate( TEMPLATE_NAME );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( template, responseTemplate );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnBadRequestStatusForRegisterTemplate()
    {
        Response response = restService.registerTemplate( "", "", "" );
        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnOkStatusForRegisterTemplate() throws IOException
    {
        File file = new File( FILE_PATH );
        file.createNewFile();


        Response response = restService.registerTemplate( FILE_PATH, FILE_PATH, "" );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        file.delete();
    }


    @Test
    public void shouldGetBadRequestOnUnregisterTemplate() throws RegistryException
    {
        when( templateRegistry
                .unregisterTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                        Common.DEFAULT_LXC_ARCH ) ).thenThrow( new RuntimeException() );
        when( templateRegistry.getTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) ) )
                .thenReturn( template );
        Response response = restService.unregisterTemplate( TEMPLATE_NAME );
        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldGetOkOnUnregisterTemplate() throws RegistryException
    {
        when( templateRegistry
                .unregisterTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                        Common.DEFAULT_LXC_ARCH ) ).thenReturn( true );
        when( templateRegistry.getTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) ) )
                .thenReturn( template );

        Response response = restService.unregisterTemplate( TEMPLATE_NAME );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnTemplateOnGetTemplate()
    {
        when( templateRegistry.getTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                Common.DEFAULT_LXC_ARCH ) ).thenReturn( template );
        Response response =
                restService.getTemplate( TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION, Common.DEFAULT_LXC_ARCH );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( template, responseTemplate );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnNotFoundOnGetTemplate()
    {
        when( templateRegistry.getTemplate( "", "" ) ).thenReturn( null );
        Response response = restService.getTemplate( "", "" );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnNotFoundOnGetParentTemplate()
    {
        when( templateRegistry.getParentTemplate( TEMPLATE_NAME ) ).thenReturn( null );
        Response response = restService.getParentTemplate( TEMPLATE_NAME );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnOkOnGetParentTemplate()
    {
        when( templateRegistry.getParentTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                Common.DEFAULT_LXC_ARCH ) ).thenReturn( template );
        Response response = restService.getParentTemplate( TEMPLATE_NAME );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( template, responseTemplate );
    }


    @Test
    public void shouldReturnNotFoundOnGetParentTemplateWithTwoParameters()
    {
        when( templateRegistry.getParentTemplate( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH ) ).thenReturn( null );
        Response response = restService.getParentTemplate( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnOkOnGetParentTemplateWithTwoParameters()
    {
        when( templateRegistry.getParentTemplate( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                Common.DEFAULT_LXC_ARCH ) ).thenReturn( template );
        Response response = restService
                .getParentTemplate( TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION, Common.DEFAULT_LXC_ARCH );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( template, responseTemplate );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetParentTemplates()
    {
        when( templateRegistry
                .getParentTemplates( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) ) )
                .thenReturn( templates );
        Response response = restService.getParentTemplates( TEMPLATE_NAME );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetParentTemplatesWithTwoParameters()
    {
        when( templateRegistry
                .getParentTemplates( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                        Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService
                .getParentTemplates( TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION, Common.DEFAULT_LXC_ARCH );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetChildTemplates()
    {
        when( templateRegistry
                .getChildTemplates( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) ) )
                .thenReturn( templates );
        Response response = restService.getChildTemplates( TEMPLATE_NAME );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetChildTemplatesArch()
    {
        when( templateRegistry
                .getChildTemplates( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), ARCH ) )
                .thenReturn( templates );
        Response response = restService.getChildTemplates( TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION, ARCH );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetChildTemplatesWithTwoParameters()
    {
        when( templateRegistry
                .getParentTemplates( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                        Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService
                .getParentTemplates( TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION, Common.DEFAULT_LXC_ARCH );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void testIsTemplateInUse() throws RegistryException
    {
        when( templateRegistry
                .isTemplateInUse( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) ) )
                .thenReturn( true );
        Response response = restService.isTemplateInUse( TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testIsTemplateInUseException() throws RegistryException
    {
        when( templateRegistry
                .isTemplateInUse( TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) ) )
                .thenThrow( new RegistryException( "" ) );
        Response response = restService.isTemplateInUse( TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testSetTemplateInUse() throws RegistryException
    {
        when( templateRegistry.updateTemplateUsage( TEMPLATE_NAME, TEMPLATE_NAME,
                new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), true ) ).thenReturn( true );
        Response response =
                restService.setTemplateInUse( TEMPLATE_NAME, TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION, "True" );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        verify( templateRegistry ).updateTemplateUsage( TEMPLATE_NAME, TEMPLATE_NAME,
                new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), true );
    }


    @Test
    public void testSetTemplateInUseException() throws RegistryException
    {
        when( templateRegistry.updateTemplateUsage( TEMPLATE_NAME, TEMPLATE_NAME,
                new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), true ) )
                .thenThrow( new RegistryException( "" ) );
        Response response =
                restService.setTemplateInUse( TEMPLATE_NAME, TEMPLATE_NAME, Common.DEFAULT_TEMPLATE_VERSION, "True" );
        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldRespondWithTemplateNamesOnListTemplates()
    {
        when( templateRegistry.getAllTemplates() ).thenReturn( templates );
        Response response = restService.listTemplates();
        Type templateListType = new TypeToken<List<String>>()
        {}.getType();
        List<String> responseTemplates = GSON.fromJson( String.valueOf( response.getEntity() ), templateListType );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), responseTemplates.toArray() );
    }


    @Test
    public void shouldRespondWithTemplateNamesOnListTemplatesWithParameter()
    {
        when( templateRegistry.getAllTemplates( Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.listTemplates( Common.DEFAULT_LXC_ARCH );

        Type templatesType = new TypeToken<List<String>>()
        {}.getType();
        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templatesType );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldListTemplatesOnListTemplatesPlain()
    {
        when( templateRegistry.getAllTemplates( Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.listTemplatesPlain();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        verify( templateRegistry ).getAllTemplates( Common.DEFAULT_LXC_ARCH );
    }


    @Test
    public void shouldListTemplatesOnListTemplatesPlainWithParameter()
    {
        when( templateRegistry.getAllTemplates( Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.listTemplatesPlain();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        verify( templateRegistry ).getAllTemplates( Common.DEFAULT_LXC_ARCH );
    }


    @Test
    public void testRemoveTemplate() throws RepositoryException
    {
        when( templateRegistry.getTemplate( anyString(), any( TemplateVersion.class ) ) ).thenReturn( template );

        Response response = restService.removeTemplate( "testTemplateName", "555" );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        verify( repositoryManager ).removePackageByName( anyString() );
    }


    @Test
    public void testImportTemplate() throws HostNotFoundException
    {
        when( attachment.getContentDisposition() ).thenReturn( contentDisposition );
        when( contentDisposition.getParameter( anyString() ) ).thenReturn( "test" );
        when( attachment.getObject( InputStream.class ) ).thenReturn( inputStream );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );

        Response response = restService.importTemplate( attachment, "testConfigDir" );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }
}
