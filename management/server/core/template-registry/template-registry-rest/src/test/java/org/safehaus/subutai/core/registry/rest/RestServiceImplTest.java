package org.safehaus.subutai.core.registry.rest;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;
import org.safehaus.subutai.core.repository.api.RepositoryManager;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test or RestServiceImpl
 */
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

    private static final String TEMPLATE_NAME = "hadoop";
    private static final String CONFIG_FILE =
            "# Template used to create this container: /usr/share/lxc/templates/lxc-ubuntu\n"
                    + "# Parameters passed to the template: -u subutai -S /root/.ssh/id_dsa.pub\n"
                    + "# For additional config options, please look at lxc.conf(5)\n" + "\n"
                    + "# Common configuration\n" + "lxc.include = /usr/share/lxc/config/ubuntu.common.conf\n" + "\n"
                    + "# Container specific configuration\n" + "lxc.rootfs = /var/lib/lxc/hadoop/rootfs\n"
                    + "lxc.mount = /var/lib/lxc/hadoop/fstab\n" + "lxc.utsname = hadoop\n" + "lxc.arch = amd64\n" + "\n"
                    + "# Network configuration\n" + "lxc.network.type = veth\n" + "lxc.network.flags = up\n"
                    + "lxc.network.link = br0\n" + "lxc.network.hwaddr = 00:16:3e:5:5e:67\n"
                    + "subutai.config.path = /etc\n" + "lxc.hook.pre-start = /usr/bin/pre_start_hook\n"
                    + "subutai.parent = master\n" + "subutai.git.branch = hadoop\n" + "SUBUTAI_VERSION = 2.3\n"
                    + "lxc.mount.entry = /lxc/hadoop-opt opt none bind,rw 0 0\n"
                    + "lxc.mount.entry = /lxc-data/hadoop-home home none bind,rw 0 0\n"
                    + "lxc.mount.entry = /lxc-data/hadoop-var var none bind,rw 0 0\n"
                    + "subutai.git.uuid = ba7e115e4b6aa0f424a64e44e726c56dd7ba1c9e\n"
                    + "subutai.template.package = /lxc-data/tmpdir/hadoop-subutai-template_2.1.0_amd64.deb";


    private Template parseTemplate( String configFile, String packagesFile, String md5sum )
    {
        Template template;
        Properties properties = new Properties();
        try
        {
            properties.load( new ByteArrayInputStream( configFile.getBytes( Charset.defaultCharset() ) ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        String lxcUtsname = properties.getProperty( "lxc.utsname" );
        String lxcArch = properties.getProperty( "lxc.arch" );
        String subutaiConfigPath = properties.getProperty( "subutai.config.path" );
        String subutaiParent = properties.getProperty( "subutai.parent" );
        String subutaiGitBranch = properties.getProperty( "subutai.git.branch" );
        String subutaiGitUuid = properties.getProperty( "subutai.git.uuid" );
        String templateVersion = properties.getProperty( "SUBUTAI_VERSION" );
        template =
                new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiParent, subutaiGitBranch, subutaiGitUuid,
                        packagesFile, md5sum, templateVersion );
        return template;
    }


    @Before
    public void setupClasses()
    {
        template = parseTemplate( CONFIG_FILE, "packagesFile", "md5sum" );
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
        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );
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
        when( templateRegistry.unregisterTemplate( TEMPLATE_NAME ) ).thenThrow( new RuntimeException() );
        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );
        Response response = restService.unregisterTemplate( TEMPLATE_NAME );
        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldGetOkOnUnregisterTemplate() throws RegistryException
    {
        when( templateRegistry.unregisterTemplate( TEMPLATE_NAME ) ).thenReturn( true );
        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );

        Response response = restService.unregisterTemplate( TEMPLATE_NAME );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnTemplateOnGetTemplate()
    {
        when( templateRegistry.getTemplate( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH ) ).thenReturn( template );
        Response response = restService.getTemplate( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH );
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
        when( templateRegistry.getParentTemplate( TEMPLATE_NAME ) ).thenReturn( template );
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
        when( templateRegistry.getParentTemplate( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH ) ).thenReturn( template );
        Response response = restService.getParentTemplate( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( template, responseTemplate );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetParentTemplates()
    {
        when( templateRegistry.getParentTemplates( TEMPLATE_NAME ) ).thenReturn( templates );
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
        when( templateRegistry.getParentTemplates( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.getParentTemplates( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetChildTemplates()
    {
        when( templateRegistry.getChildTemplates( TEMPLATE_NAME ) ).thenReturn( templates );
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
        when( templateRegistry.getChildTemplates( TEMPLATE_NAME, ARCH ) ).thenReturn( templates );
        Response response = restService.getChildTemplates( TEMPLATE_NAME, ARCH );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetChildTemplatesWithTwoParameters()
    {
        when( templateRegistry.getParentTemplates( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.getParentTemplates( TEMPLATE_NAME, Common.DEFAULT_LXC_ARCH );

        Type templateType = new TypeToken<List<String>>()
        {}.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( TEMPLATE_NAME ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetTemplateTree()
    {
        TemplateTree templateTree = mock( TemplateTree.class );
        when( templateTree.getRootTemplates() ).thenReturn( templates );
        when( templateTree.getChildrenTemplates( any( Template.class ) ) )
                .thenReturn( Lists.newArrayList( mock( Template.class ) ) )
                .thenReturn( Collections.<Template>emptyList() );
        when( templateRegistry.getTemplateTree() ).thenReturn( templateTree );

        Type templateType = new TypeToken<List<Template>>()
        {}.getType();

        Response response = restService.getTemplateTree();

        List<Template> responseTemplates = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( templates.toArray(), responseTemplates.toArray() );
    }


    @Test
    public void testIsTemplateInUse() throws RegistryException
    {
        when( templateRegistry.isTemplateInUse( TEMPLATE_NAME ) ).thenReturn( true );
        Response response = restService.isTemplateInUse( TEMPLATE_NAME );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testIsTemplateInUseException() throws RegistryException
    {
        when( templateRegistry.isTemplateInUse( TEMPLATE_NAME ) ).thenThrow( new RegistryException( "" ) );
        Response response = restService.isTemplateInUse( TEMPLATE_NAME );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testSetTemplateInUse() throws RegistryException
    {
        when( templateRegistry.updateTemplateUsage( TEMPLATE_NAME, TEMPLATE_NAME, true ) ).thenReturn( true );
        Response response = restService.setTemplateInUse( TEMPLATE_NAME, TEMPLATE_NAME, "True" );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        verify( templateRegistry ).updateTemplateUsage( TEMPLATE_NAME, TEMPLATE_NAME, true );
    }


    @Test
    public void testSetTemplateInUseException() throws RegistryException
    {
        when( templateRegistry.updateTemplateUsage( TEMPLATE_NAME, TEMPLATE_NAME, true ) )
                .thenThrow( new RegistryException( "" ) );
        Response response = restService.setTemplateInUse( TEMPLATE_NAME, TEMPLATE_NAME, "True" );
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
}
