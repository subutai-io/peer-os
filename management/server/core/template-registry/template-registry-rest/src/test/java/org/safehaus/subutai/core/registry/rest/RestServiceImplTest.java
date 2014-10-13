package org.safehaus.subutai.core.registry.rest;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 10/3/14.
 */
public class RestServiceImplTest
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final String EXCEPTION_HEADER = "exception";
    private static final String TEMPLATE_PARENT_DELIMITER = " ";
    private static final String TEMPLATES_DELIMITER = "\n";

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();


    private TemplateRegistry templateRegistry;
    private RestServiceImpl restService;
    private Template template;
    private List<Template> templates;

    private String templateName = "hadoop";
    private String configFile = "# Template used to create this container: /usr/share/lxc/templates/lxc-ubuntu\n"
            + "# Parameters passed to the template: -u subutai -S /root/.ssh/id_dsa.pub\n"
            + "# For additional config options, please look at lxc.conf(5)\n" + "\n" + "# Common configuration\n"
            + "lxc.include = /usr/share/lxc/config/ubuntu.common.conf\n" + "\n" + "# Container specific configuration\n"
            + "lxc.rootfs = /var/lib/lxc/hadoop/rootfs\n" + "lxc.mount = /var/lib/lxc/hadoop/fstab\n"
            + "lxc.utsname = hadoop\n" + "lxc.arch = amd64\n" + "\n" + "# Network configuration\n"
            + "lxc.network.type = veth\n" + "lxc.network.flags = up\n" + "lxc.network.link = br0\n"
            + "lxc.network.hwaddr = 00:16:3e:5:5e:67\n" + "subutai.config.path = /etc\n"
            + "lxc.hook.pre-start = /usr/bin/pre_start_hook\n" + "subutai.parent = master\n"
            + "subutai.git.branch = hadoop\n" + "lxc.mount.entry = /lxc/hadoop-opt opt none bind,rw 0 0\n"
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
        template =
                new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiParent, subutaiGitBranch, subutaiGitUuid,
                        packagesFile, md5sum );
        return template;
    }


    @Before
    public void setupClasses()
    {
        template = parseTemplate( configFile, "packagesFile", "md5sum" );
        templates = Lists.newArrayList( template );
        templateRegistry = mock( TemplateRegistry.class );
        restService = new RestServiceImpl();
        restService.setTemplateRegistry( templateRegistry );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionForSettingNullValue()
    {
        restService.setTemplateRegistry( null );
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
        when( templateRegistry.getTemplate( templateName ) ).thenReturn( template );
        Response response = restService.getTemplate( templateName );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( template, responseTemplate );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


//    @Ignore
    @Test
    public void shouldReturnBadRequestStatusForRegisterTemplate()
    {
        Response response = restService.registerTemplate( "", "", "" );
        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Ignore
    @Test
    public void shouldReturnOkStatusForRegisterTemplate()
    {

        Response response = restService.registerTemplate( "", "", "" );
        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldGetBadRequestOnUnregisterTemplate() throws RegistryException
    {
        when( templateRegistry.unregisterTemplate( templateName ) ).thenThrow( new RuntimeException() );
        Response response = restService.unregisterTemplate( templateName );
        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldGetOkOnUnregisterTemplate() throws RegistryException
    {
        when( templateRegistry.unregisterTemplate( templateName ) ).thenReturn( true );
        Response response = restService.unregisterTemplate( templateName );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnTemplateOnGetTemplate()
    {
        when( templateRegistry.getTemplate( templateName, Common.DEFAULT_LXC_ARCH ) ).thenReturn( template );
        Response response = restService.getTemplate( templateName, Common.DEFAULT_LXC_ARCH );
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
        when( templateRegistry.getParentTemplate( templateName ) ).thenReturn( null );
        Response response = restService.getParentTemplate( templateName );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnOkOnGetParentTemplate()
    {
        when( templateRegistry.getParentTemplate( templateName ) ).thenReturn( template );
        Response response = restService.getParentTemplate( templateName );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( template, responseTemplate );
    }


    @Test
    public void shouldReturnNotFoundOnGetParentTemplateWithTwoParameters()
    {
        when( templateRegistry.getParentTemplate( templateName, Common.DEFAULT_LXC_ARCH ) ).thenReturn( null );
        Response response = restService.getParentTemplate( templateName, Common.DEFAULT_LXC_ARCH );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnOkOnGetParentTemplateWithTwoParameters()
    {
        when( templateRegistry.getParentTemplate( templateName, Common.DEFAULT_LXC_ARCH ) ).thenReturn( template );
        Response response = restService.getParentTemplate( templateName, Common.DEFAULT_LXC_ARCH );
        Template responseTemplate = GSON.fromJson( String.valueOf( response.getEntity() ), Template.class );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( template, responseTemplate );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetParentTemplates()
    {
        when( templateRegistry.getParentTemplates( templateName ) ).thenReturn( templates );
        Response response = restService.getParentTemplates( templateName );

        Type templateType = new TypeToken<List<String>>()
        {
        }.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( templateName ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetParentTemplatesWithTwoParameters()
    {
        when( templateRegistry.getParentTemplates( templateName, Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.getParentTemplates( templateName, Common.DEFAULT_LXC_ARCH );

        Type templateType = new TypeToken<List<String>>()
        {
        }.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( templateName ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetChildTemplates()
    {
        when( templateRegistry.getChildTemplates( templateName ) ).thenReturn( templates );
        Response response = restService.getChildTemplates( templateName );

        Type templateType = new TypeToken<List<String>>()
        {
        }.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( templateName ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetChildTemplatesWithTwoParameters()
    {
        when( templateRegistry.getParentTemplates( templateName, Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.getParentTemplates( templateName, Common.DEFAULT_LXC_ARCH );

        Type templateType = new TypeToken<List<String>>()
        {
        }.getType();

        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( templateName ).toArray(), templateNames.toArray() );
    }


    @Test
    public void shouldReturnListOfTemplatesOnGetTemplateTree()
    {
        TemplateTree templateTree = mock( TemplateTree.class );
        when( templateTree.getRootTemplates() ).thenReturn( templates );
        when( templateRegistry.getTemplateTree() ).thenReturn( templateTree );

        Type templateType = new TypeToken<List<Template>>()
        {
        }.getType();

        Response response = restService.getTemplateTree();

        List<Template> responseTemplates = GSON.fromJson( String.valueOf( response.getEntity() ), templateType );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( templates.toArray(), responseTemplates.toArray() );
    }


    @Test
    public void shouldThrowRegistryExceptionOnIsTemplateInUse() throws RegistryException
    {
        when( templateRegistry.isTemplateInUse( templateName ) ).thenReturn( true );
        Response response = restService.isTemplateInUse( templateName );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldSetTemplateInUseOnSetTemplateInUse() throws RegistryException
    {
        when( templateRegistry.updateTemplateUsage( templateName, templateName, true ) ).thenReturn( true );
        Response response = restService.setTemplateInUse( templateName, templateName, "True" );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        verify( templateRegistry ).updateTemplateUsage( templateName, templateName, true );
    }


    @Test
    public void shouldRespondWithTemplateNamesOnListTemplates()
    {
        when( templateRegistry.getAllTemplates() ).thenReturn( templates );
        Response response = restService.listTemplates();
        Type templateListType = new TypeToken<List<String>>()
        {
        }.getType();
        List<String> responseTemplates = GSON.fromJson( String.valueOf( response.getEntity() ), templateListType );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( templateName ).toArray(), responseTemplates.toArray() );
    }


    @Test
    public void shouldRespondWithTemplateNamesOnListTemplatesWithParameter()
    {
        when( templateRegistry.getAllTemplates( Common.DEFAULT_LXC_ARCH ) ).thenReturn( templates );
        Response response = restService.listTemplates( Common.DEFAULT_LXC_ARCH );

        Type templatesType = new TypeToken<List<String>>()
        {
        }.getType();
        List<String> templateNames = GSON.fromJson( String.valueOf( response.getEntity() ), templatesType );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertArrayEquals( Lists.newArrayList( templateName ).toArray(), templateNames.toArray() );
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
