package io.subutai.core.template.wizard.impl;


import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.ResourceHost;
import io.subutai.core.peer.api.ResourceHostException;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.template.wizard.api.InstallationPhase;
import io.subutai.core.template.wizard.impl.TemplateWizardManagerImpl;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TemplateWizardManagerImplTest
{

    TemplateWizardManagerImpl wizardManager;
    String templateName = "templateName";
    String newTemplateName = "newTemplateName";
    UUID uuid = UUID.randomUUID();
    List<String> postInstallationScripts = Lists.newArrayList();
    List<String> preInstallationScripts = Lists.newArrayList();
    List<String> products = Lists.newArrayList( "product1", "product2" );

    @Mock
    TrackerOperation trackerOperation;

    @Mock
    PeerManager peerManager;

    @Mock
    TemplateRegistry templateRegistry;

    @Mock
    LocalPeer localPeer;

    @Mock
    ResourceHost resourceHost;

    @Mock
    ContainerHost containerHost;

    @Mock
    CommandResult commandResult;

    @Mock
    Template template;


    @Before
    public void setUp() throws Exception
    {
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getResourceHostById( uuid ) ).thenReturn( resourceHost );
        when( localPeer.getContainerHostById( uuid ) ).thenReturn( containerHost );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( resourceHost.createContainer( templateName, newTemplateName, 90 ) ).thenReturn( containerHost );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( templateRegistry.getTemplate( templateName ) ).thenReturn( template );
        when( template.getTemplateName() ).thenReturn( templateName );

        wizardManager = new TemplateWizardManagerImpl();
        wizardManager.setPeerManager( peerManager );
        wizardManager.setTemplateRegistry( templateRegistry );
    }


    @Test
    public void testGetCurrentPhase() throws Exception
    {
        assertEquals( InstallationPhase.NO_OP, wizardManager.getCurrentPhase() );
    }


    @Test
    public void testCreateTemplate() throws Exception
    {
        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );

        verify( resourceHost, Mockito.times( 3 ) ).execute( any( RequestBuilder.class ) );
        verify( templateRegistry ).getTemplate( templateName );
        verify( trackerOperation, Mockito.times( 6 ) ).addLog( anyString() );
    }


    @Test
    public void testCreateTemplateExceptionInRegisterTemplate() throws Exception
    {
        String cmd = String.format( "subutai register %s", newTemplateName );
        RequestBuilder requestBuilder = new RequestBuilder( cmd );
        requestBuilder.withTimeout( 180 );

        when( resourceHost.execute( requestBuilder ) )
                .thenThrow( new CommandException( new Exception( "Command Exception" ) ) );

        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );
        verify( trackerOperation ).addLogFailed( anyString() );
    }


    @Test
    public void testCreateTemplateExceptionInExportTemplate() throws Exception
    {
        String cmd = String.format( "subutai export %s", newTemplateName );
        RequestBuilder requestBuilder = new RequestBuilder( cmd );
        requestBuilder.withTimeout( 180 );

        when( resourceHost.execute( requestBuilder ) )
                .thenThrow( new CommandException( new Exception( "Command Exception" ) ) );

        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );
        verify( trackerOperation ).addLogFailed( anyString() );
    }


    @Test
    public void testCreateTemplateExceptionInCreateContainerHost() throws Exception
    {

        when( resourceHost.createContainer( templateName, newTemplateName, 90 ) )
                .thenThrow( new ResourceHostException( new Exception( "Couldn't create container host" ) ) )
                .thenReturn( containerHost );

        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );
        verify( trackerOperation ).addLog( anyString() );
    }


    @Test( expected = RuntimeException.class )
    public void testCreateTemplateExceptionInInstallProducts() throws Exception
    {
        when( containerHost.execute( any( RequestBuilder.class ) ) )
                .thenThrow( new CommandException( new Exception( "Error installing product" ) ) );

        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );
        verify( trackerOperation ).addLogFailed( anyString() );
        verify( trackerOperation ).addLog( anyString() );
    }


    @Test
    public void testCreateTemplateExceptionInPromoteContainerHost() throws Exception
    {
        String cmd = String.format( "subutai promote %s", newTemplateName );
        RequestBuilder requestBuilder = new RequestBuilder( cmd );
        requestBuilder.withTimeout( 90 );

        when( resourceHost.execute( requestBuilder ) )
                .thenThrow( new CommandException( new Exception( "Error promoting container" ) ) );

        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );
        verify( trackerOperation ).addLogFailed( anyString() );
        verify( trackerOperation, Mockito.times( 4 ) ).addLog( anyString() );
    }


    @Test( expected = RuntimeException.class )
    public void testCreateTemplateExceptionInCreateTemplate() throws Exception
    {
        when( localPeer.getResourceHostById( uuid ) ).thenThrow( new HostNotFoundException( "Host not found" ) );
        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );
        verify( templateRegistry ).getTemplate( templateName );
    }


    @Test
    public void testCreateContainerHost() throws Exception
    {
        wizardManager.createContainerHost( newTemplateName, templateName, uuid, trackerOperation );
        verify( templateRegistry ).getTemplate( templateName );
        verify( peerManager ).getLocalPeer();
        verify( localPeer ).getResourceHostById( uuid );
        verify( resourceHost ).createContainer( templateName, newTemplateName, 90 );
    }


    @Test( expected = RuntimeException.class )
    public void testCreateContainerHostWithException() throws Exception
    {
        when( localPeer.getResourceHostById( uuid ) )
                .thenThrow( new HostNotFoundException( "Host not found exception" ) );
        wizardManager.createContainerHost( newTemplateName, templateName, uuid, trackerOperation );
        verify( templateRegistry ).getTemplate( templateName );
        verify( peerManager ).getLocalPeer();
        verify( localPeer ).getResourceHostById( uuid );
    }


    @Test
    public void testCreateTemplateNullContainerHostInTriggerTemplateCreation() throws Exception
    {
        when( resourceHost.createContainer( templateName, newTemplateName, 90 ) ).thenReturn( null );
        wizardManager.createTemplate( newTemplateName, templateName, postInstallationScripts, products,
                preInstallationScripts, uuid, trackerOperation );
        verify( templateRegistry ).getTemplate( templateName );
    }


    @Test
    public void testInstallProducts() throws Exception
    {
        wizardManager.installProducts( products, uuid, trackerOperation );
        verify( containerHost, Mockito.times( 2 ) ).execute( any( RequestBuilder.class ) );
        verify( peerManager ).getLocalPeer();
        verify( localPeer ).getContainerHostById( uuid );
    }


    @Test( expected = RuntimeException.class )
    public void testInstallProductsWithException() throws Exception
    {
        when( localPeer.getContainerHostById( uuid ) )
                .thenThrow( new HostNotFoundException( "Host not found exception" ) );
        wizardManager.installProducts( products, uuid, trackerOperation );
        verify( peerManager ).getLocalPeer();
        verify( localPeer ).getContainerHostById( uuid );
    }


    @Test
    public void testPreInstallationScripts() throws Exception
    {
        wizardManager.preInstallationScripts( preInstallationScripts );
    }


    @Test
    public void testPostInstallationScripts() throws Exception
    {
        wizardManager.postInstallationScripts( postInstallationScripts );
    }


    @Test
    public void testGetPeerManager() throws Exception
    {
        wizardManager.setPeerManager( peerManager );
        assertEquals( peerManager, wizardManager.getPeerManager() );
    }


    @Test
    public void testSetPeerManager() throws Exception
    {
        wizardManager.setPeerManager( null );
        assertNotEquals( peerManager, wizardManager.getPeerManager() );
    }


    @Test
    public void testGetTemplateRegistry() throws Exception
    {
        wizardManager.setTemplateRegistry( templateRegistry );
        assertEquals( templateRegistry, wizardManager.getTemplateRegistry() );
    }


    @Test
    public void testSetTemplateRegistry() throws Exception
    {
        wizardManager.setTemplateRegistry( null );
        assertNotEquals( templateRegistry, wizardManager.getTemplateRegistry() );
    }
}