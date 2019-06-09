package io.subutai.core.bazaarmanager.impl.environment.state.backup;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.openpgp.PGPException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import io.subutai.bazaar.share.dto.backup.CdnBackupFileDto;
import io.subutai.bazaar.share.dto.backup.ContainerRestoreCommandDto;
import io.subutai.bazaar.share.dto.backup.RestoreCommandsDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodesDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.settings.Common;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.create.BuildContainerStateHandler;


public class RestoreStateHandler extends StateHandler
{
    private final String path = "/rest/v1/environments/%s/peers/%s/restore-containers";

    private ExecutorService executor = Executors.newFixedThreadPool( 5 );


    public RestoreStateHandler( final Context ctx )
    {
        super( ctx, "Container restore processor" );
    }


    @Override
    protected Object doHandle( final EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        RestoreCommandsDto restoreCommands =
                ctx.restClient.getStrict( path( path, peerDto ), RestoreCommandsDto.class );

        log.info( "Has {} containers to restore", restoreCommands.getContainerRestoreCommands().size() );

        if ( restoreCommands.getContainerRestoreCommands().isEmpty() )
        {
            return restoreCommands;
        }

        // Step-1: prepare templates
        log.info( "1. Preparing templates..." );
        prepareTemplates( peerDto, restoreCommands );


        // Step-2: prepare PGP keys
        log.info( "2. Setting up environment keys..." );
        try
        {
            setupPeerEnvironmentKey( peerDto );
        }
        catch ( PeerException | PGPException e )
        {
            throw new BazaarManagerException( "Failed to setup peer-environment keys.", e );
        }


        // Step-3: download all backup files
        log.info( "3. Downloading backup files..." );
        Map<String, ArrayList<String>> backupFilesByContainers;
        try
        {
            backupFilesByContainers = downloadBackupFiles( restoreCommands );
        }
        catch ( ResourceHostException | HostNotFoundException e )
        {
            throw new BazaarManagerException( "Failed to download backup files.", e );
        }


        // restore containers
        for ( final ContainerRestoreCommandDto restoreCmd : restoreCommands.getContainerRestoreCommands() )
        {
            try
            {
                ResourceHost rh = ctx.localPeer.getResourceHostById( restoreCmd.getResourceHostId() );
                log.info( "4. Restoring container: {}", restoreCmd );

                final String envId = peerDto.getEnvironmentInfo().getId();

                Set<ContainerHost> envContainers = ctx.localPeer.findContainersByEnvironmentId( envId );
                boolean containerExists = false;
                for ( final ContainerHost container : envContainers )
                {
                    if ( container.getHostname().equals( restoreCmd.getContainerHostname() ) )
                    {
                        containerExists = true;
                        break;
                    }
                }

                if ( containerExists )
                {
                    log.warn( "Container with hostname '{}' already exists in environment {}",
                            restoreCmd.getContainerHostname(), envId );
                    continue;
                }


                final String newContainerName =
                        generateContainerName( restoreCmd.getContainerHostname(), restoreCmd.getVlan(),
                                restoreCmd.getContainerIpAddress() );

                restoreCmd.getResult().setRestoredContainerName( newContainerName );


                // Step-4: restore container
                for ( final String filePath : backupFilesByContainers.get( restoreCmd.getContainerHostname() ) )
                {
                    log.info( "4.1. Recreating filesystem: {}", filePath );
                    rh.recreateContainerFilesystem( newContainerName, filePath );
                }

                log.info( "4.2. Recreating container..." );
                String restoredContainerId = rh.recreateContainer( newContainerName, restoreCmd.getContainerHostname(),
                        restoreCmd.getContainerIpAddress(), restoreCmd.getVlan(), envId );

                log.info( "4.3. Registering container {} on RH {} for env {} ...", restoredContainerId, rh.getId(),
                        envId );
                // logic taken from`LocalPeerImpl.registerContainer()` and `LocalPeerImpl.createEnvironmentContainers()`
                ctx.localPeer
                        .registerContainer( rh.getId(), envId, restoredContainerId, restoreCmd.getContainerHostname(),
                                DEFAULT_HOST_ARCH, restoreCmd.getTemplateId(),
                                peerDto.getEnvironmentInfo().getOwnerId(), restoreCmd.getContainerQuota() );


                ContainerHost containerHost = rh.getContainerHostById( restoredContainerId );

                int counter = 0;
                // if container is not ready, wait 5 minutes for it
                while ( StringUtils.isBlank( containerHost.getContainerName() ) && counter < 60 )
                {
                    counter++;
                    log.warn( "{}: Weird, container is created, it's ID is '{}', but name is null.", counter,
                            restoredContainerId );

                    try
                    {
                        Thread.sleep( 5000 );
                    }
                    catch ( InterruptedException e )
                    {
                        Thread.currentThread().interrupt();
                    }
                    containerHost = rh.getContainerHostById( restoredContainerId );
                }
                log.info( "Container successfully recreated, ID: {}, counter: {}", containerHost.getContainerId(),
                        counter );

                // Step-5: apply container quota
                log.info( "5. Setting container quota: {}", restoreCmd.getContainerQuota().toString() );
                rh.setContainerQuota( containerHost, restoreCmd.getContainerQuota() );


                // Update /etc/hosts with new hostname in case it has changed, call
                // Peer#updateEtcHostsWithNewContainerHostname on all participating peers

                // Step-6: generate SSH key for new container
                log.info( "6. Generating SSH key for restored container {}...", containerHost.getContainerId() );
                final String sshKey = createSshKey( containerHost );

                //Update authorized_keys file with new hostname in case it has changed, call
                // Peer#updateAuthorizedKeysWithNewContainerHostname on all participating peers


                restoreCmd.getResult().addSshKey( sshKey );
                restoreCmd.getResult().setRestoredContainerId( restoredContainerId );


                // Step-7: destroy old container
                if ( restoreCmd.isDestroyOldContainer() )
                {
                    try
                    {
                        ContainerHost oldContainerHost =
                                rh.getContainerHostByContainerName( restoreCmd.getContainerOldName() );

                        log.warn( "7. Destroying old original container: id={}, name={}.",
                                oldContainerHost.getContainerName(), oldContainerHost.getId() );
                        rh.destroyContainerHost( oldContainerHost );
                    }
                    catch ( HostNotFoundException e )
                    {
                        log.warn( "Container not found by name '{}'", restoreCmd.getContainerOldName() );
                    }
                }

                // Step-9: remove backup files
                log.debug( "8. Cleaning up backup files..." );
                for ( final String filePath : backupFilesByContainers.get( restoreCmd.getContainerHostname() ) )
                {
                    removeFileAsync( rh, filePath );
                }
            }
            catch ( ResourceHostException | PeerException e )
            {
                throw new BazaarManagerException( e );
            }
        }


        return restoreCommands;
    }


    /**
     * Copy of {@link BuildContainerStateHandler#prepareTemplates(EnvironmentPeerDto, EnvironmentNodesDto)}
     */
    private void prepareTemplates( final EnvironmentPeerDto peerDto, RestoreCommandsDto restoreCommandsDto )
            throws BazaarManagerException
    {
        Set<Node> nodes = new HashSet<>();

        for ( ContainerRestoreCommandDto restoreCmd : restoreCommandsDto.getContainerRestoreCommands() )
        {
            log.info( "- restoring container: oldName={}, hostname={}, template={}", restoreCmd.getContainerOldName(),
                    restoreCmd.getContainerHostname(), restoreCmd.getTemplateId() );

            Node node = new Node( restoreCmd.getContainerHostname(), restoreCmd.getContainerOldName(),
                    restoreCmd.getContainerQuota(), peerDto.getPeerId(), restoreCmd.getResourceHostId(),
                    restoreCmd.getTemplateId() );

            nodes.add( node );
        }


        // <hostId, templates>
        Map<String, Set<String>> rhTemplates = new HashMap<>();

        for ( final Node node : nodes )
        {
            Set<String> templates = rhTemplates.get( node.getHostId() );

            if ( templates == null )
            {
                templates = new HashSet<>();
                rhTemplates.put( node.getHostId(), templates );
            }

            templates.add( node.getTemplateId() );
        }

        if ( rhTemplates.isEmpty() )
        {
            return;
        }

        try
        {
            ctx.localPeer.prepareTemplates(
                    new PrepareTemplatesRequest( peerDto.getEnvironmentInfo().getId(), peerDto.getCdnToken(),
                            rhTemplates ) );
        }
        catch ( PeerException e )
        {
            throw new BazaarManagerException( e );
        }
    }


    /**
     * Copy of {@link BuildContainerStateHandler#setupPeerEnvironmentKey(EnvironmentPeerDto)}
     */
    private void setupPeerEnvironmentKey( EnvironmentPeerDto peerDto ) throws PeerException, PGPException
    {
        RelationLinkDto envLink =
                new RelationLinkDto( peerDto.getEnvironmentInfo().getId(), Environment.class.getSimpleName(),
                        PermissionObject.ENVIRONMENT_MANAGEMENT.getName(), peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.createPeerEnvironmentKeyPair( envLink );
    }


    private Map<String, ArrayList<String>> downloadBackupFiles( RestoreCommandsDto restoreCommandsDto )
            throws HostNotFoundException, ResourceHostException
    {
        Map<String, ArrayList<String>> backupFilesByContainers = new HashMap<>();

        for ( final ContainerRestoreCommandDto restoreCmd : restoreCommandsDto.getContainerRestoreCommands() )
        {
            ResourceHost rh = ctx.localPeer.getResourceHostById( restoreCmd.getResourceHostId() );
            for ( final CdnBackupFileDto cdnBackupFileDto : restoreCmd.getBackupFileSequence() )
            {
                String encryptedFilePath = rh.downloadRawFileFromCdn( cdnBackupFileDto.getCdnId(), null );

                String decryptedFilePath = rh.decryptFile( encryptedFilePath, cdnBackupFileDto.getPassword() );

                ArrayList<String> backupFiles = backupFilesByContainers.get( restoreCmd.getContainerHostname() );
                if ( backupFiles == null )
                {
                    backupFiles = new ArrayList<>();
                    backupFilesByContainers.put( restoreCmd.getContainerHostname(), backupFiles );
                }
                backupFiles.add( decryptedFilePath );

                try
                {
                    // remove encrypted file
                    ctx.localPeer
                            .execute( new RequestBuilder( String.format( "rm %s", encryptedFilePath ) ), rh, null );
                }
                catch ( Exception e )
                {
                    log.error( "Failed to remove encrypted backup file {}", encryptedFilePath, e );
                }
            }
        }

        return backupFilesByContainers;
    }


    private void removeFileAsync( final ResourceHost rh, final String pathToFile )
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                log.debug( "Removing restored backup file: {}", pathToFile );
                try
                {
                    ctx.localPeer.execute( new RequestBuilder( String.format( "rm %s", pathToFile ) ), rh, null );
                }
                catch ( CommandException e )
                {
                    log.error( "Failed to remove restored backup file {}", pathToFile, e );
                }
            }
        } );
    }


    /**
     * Copy of {@link io.subutai.common.environment.CloneContainerTask#generateContainerName()}
     */
    private static String generateContainerName( String hostname, int vlan, String ipAddress )
    {
        //update hostname to make it unique on this peer
        //append 3 random letters
        //append VLAN, it will make it unique on this peer
        //append additional suffix (last IP octet) that will make it unique inside host environment
        //13 symbols are appended

        return String.format( "%s-%s-%d-%s", hostname, RandomStringUtils.randomAlphanumeric( 3 ).toLowerCase(), vlan,
                StringUtils.substringAfterLast( ipAddress.split( "/" )[0], "." ) );
    }


    /**
     * Create ssh key for given container.
     *
     * Copy of {@link BuildContainerStateHandler#createSshKey(String)}
     */
    private String createSshKey( ContainerHost containerHost ) throws BazaarManagerException
    {
        CommandResult result;

        try
        {
            RequestBuilder rb = new RequestBuilder( String.format(
                    "rm -rf %1$s ; mkdir -p %1$s && chmod 700 %1$s && ssh-keygen -t rsa -P '' -f %1$s/id_rsa -q && "
                            + "cat %1$s/id_rsa.pub;", Common.CONTAINER_SSH_FOLDER ) )
                    .withTimeout( Common.CREATE_SSH_KEY_TIMEOUT_SEC );

            result = ctx.localPeer.execute( rb, containerHost );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }

        if ( !result.hasSucceeded() )
        {
            throw new BazaarManagerException(
                    "Failed to create SSH key for container " + containerHost.getId() + ": " + result.getStdErr() );
        }

        return result.getStdOut();
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( path, peerDto ), body );
    }
}
