package io.subutai.core.peer.rest;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationData;
import io.subutai.core.peer.api.PeerManager;


/**
 * REST endpoint implementation of registration process
 */
public class RegistrationServiceImpl implements RegistrationService
{
    private static Logger LOG = LoggerFactory.getLogger( RegistrationData.class );

    private PeerManager peerManager;


    public RegistrationServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public RegistrationData processRegistrationRequest( final RegistrationData registrationData )
    {
        try
        {
            return peerManager.processRegistrationRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processCancelRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processCancelRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processRejectRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processRejectRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processApproveRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processApproveRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processUnregisterRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processUnregisterRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    //    @Override
    //    public Response getRegisteredPeerInfo( final String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            PeerInfo peerInfo = peerManager.getPeer( peerId ).getPeerInfo();
    //            return Response.ok( jsonUtil.to( peerInfo ) ).build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error getting peer info #getRegisteredPeerInfo", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }
    //
    //
    //    @Override
    //    public Response ping()
    //    {
    //        return Response.ok().build();
    //    }


    //    @Override
    //    public Response processRegistrationRequest( PeerInfo remotePeerInfo )
    //    {
    //        try
    //        {
    //            if ( peerManager.getPeerInfo( remotePeerInfo.getId() ) != null )
    //            {
    //                return Response.status( Response.Status.CONFLICT )
    //                               .entity( String.format( "%s already registered", remotePeerInfo.getName() ) )
    // .build();
    //            }
    //            else
    //            {
    //                peerManager.register( remotePeerInfo );
    //
    //                return Response.ok().build();
    //            }
    //        }
    //        catch ( PeerException e )
    //        {
    //            LOGGER.error( "Error processing register request #processRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }
    // @Override
    //    public Response processRegistrationRequest( PeerInfo peerInfo )
    //    {
    //        try
    //        {
    //            // ******* Convert HexString to Byte Array ****** Decrypt data
    //            EncryptionTool encTool = securityManager.getEncryptionTool();
    //            KeyManager keyManager = securityManager.getKeyManager();
    //
    //            byte[] data = HexUtil.hexStringToByteArray( peerInfo.getId() );
    //            data = encTool.decrypt( data );
    //            //************************************************
    //
    //            PeerInfo p = jsonUtil.from( new String( data ), PeerInfo.class );
    //
    //            if ( peerManager.getPeerInfo( p.getId() ) != null )
    //            {
    //                return Response.status( Response.Status.CONFLICT )
    //                               .entity( String.format( "%s already registered", p.getName() ) ).build();
    //            }
    //            else
    //            {
    //                //Encrypt Local Peer
    //                PGPPublicKey pkey = keyManager.getRemoteHostPublicKey( p.getId(), p.getIp() );
    //                PeerInfo localPeer = peerManager.getLocalPeerInfo();
    //
    //                if ( pkey != null )
    //                {
    //                    localPeer.setKeyPhrase( p.getKeyPhrase() );
    //                    String jsonData = jsonUtil.to( localPeer );
    //                    data = encTool.encrypt( jsonData.getBytes(), pkey, false );
    //
    //                    // Save to DB
    //                    p.setStatus( PeerStatus.REQUESTED );
    //                    p.setName( String.format( "Peer %s", p.getId() ) );
    //                    peerManager.register( p );
    //
    //                    return Response.ok( HexUtil.byteArrayToHexString( data ) ).build();
    //                }
    //                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error processing register request #processRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response doRegistrationRequest( final String peerIp )
    //    {
    //        try
    //        {
    //            String baseUrl =
    //                    String.format( "https://%s:%s/rest/v1/peer/register", peerIp, ChannelSettings
    // .SECURE_PORT_X1 );
    //            WebClient client = restUtil.getTrustedWebClient( baseUrl, provider );
    //            client.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.APPLICATION_JSON );
    //            Form form = new Form();
    //            form.set( "peer", jsonUtil.to( peerManager.getLocalPeerInfo() ) );
    //
    //            //TODO: refactor to return POJO
    //            Response response = client.post( peerManager.getLocalPeerInfo() );
    //
    //
    //            //            Response response = client.path( "peer/register" ).form( form );
    //            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
    //            {
    //                return registerPeerCert( response );
    //            }
    //            else if ( response.getStatus() == Response.Status.CONFLICT.getStatusCode() )
    //            {
    //                String reason = response.readEntity( String.class );
    //                LOGGER.warn( reason );
    //                return Response.serverError().entity( reason ).build();
    //            }
    //            else
    //            {
    //                LOGGER.warn( "Response for registering peer: " + response.toString() );
    //                return Response.serverError().build();
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "error sending request", e );
    //            return Response.serverError().entity( e.toString() ).build();
    //        }
    //    }


    //    protected Response registerPeerCert( final Response response )
    //    {
    //        try
    //        {
    //            String responseString = response.readEntity( String.class );
    //            LOGGER.info( response.toString() );
    //            PeerInfo remotePeerInfo = jsonUtil.from( responseString, new TypeToken<PeerInfo>()
    //            {}.getType() );
    //            if ( remotePeerInfo != null )
    //            {
    //                remotePeerInfo.setStatus( PeerStatus.REQUEST_SENT );
    //                try
    //                {
    //                    peerManager.register( remotePeerInfo );
    //                }
    //                catch ( PeerException e )
    //                {
    //                    LOGGER.error( "Couldn't register peer", e );
    //                }
    //            }
    //            return Response.ok().build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error registering peer certificate #registerPeerCert", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response unregisterPeer( String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            boolean result = peerManager.unregister( peerId );
    //            if ( result )
    //            {
    //                //************ Delete Trust SSL Cert **************************************
    //                securityManager.getKeyStoreManager().removeCertFromTrusted( ChannelSettings.SECURE_PORT_X2,
    // peerId );
    //                httpContextManager.reloadTrustStore();
    //                //***********************************************************************
    //
    //
    //                return Response.ok( "Successfully unregistered peer: " + peerId ).build();
    //            }
    //            else
    //            {
    //                return Response.status( Response.Status.NOT_FOUND ).build();
    //            }
    //        }
    //        catch ( Exception pe )
    //        {
    //            LOGGER.error( "Error unregistering peer #unregisterPeer", pe );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( pe.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response rejectForRegistrationRequest( final String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            PeerInfo p = peerManager.getPeerInfo( peerId );
    //            p.setStatus( PeerStatus.REJECTED );
    //            peerManager.update( p );
    //
    //            return Response.noContent().build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error rejecting registration request #rejectForRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response removeRegistrationRequest( final String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            UUID id = UUID.fromString( peerId );
    //            peerManager.unregister( id.toString() );
    //            return Response.status( Response.Status.NO_CONTENT ).build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error removing registration request #removeRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    //        }
    //    }
}
