package io.subutai.core.hubmanager.impl.processor;


import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.VersionInfoDto;

import static java.lang.String.format;


public class VersionInfoProcessor extends HubRequester
{
    private ConfigManager configManager;

    private PeerManager peerManager;


    public VersionInfoProcessor( final HubManagerImpl hubManager, final PeerManager peerManager,
                                 final ConfigManager configManager, final RestClient restClient )
    {
        super( hubManager, restClient );

        this.peerManager = peerManager;
        this.configManager = configManager;
    }


    @Override
    public void request() throws HubManagerException
    {
        sendVersionInfo();
    }


    private void sendVersionInfo() throws HubManagerException
    {
        String path = format( "/rest/v1/peers/%s/version-info", peerManager.getLocalPeer().getId() );

        VersionInfoDto versionInfoDto = new VersionInfoDto();

        versionInfoDto.setPeerId( configManager.getPeerId() );
        versionInfoDto.setSsVersion( SubutaiInfo.getVersion() );
        versionInfoDto.setBuildTime( SubutaiInfo.getBuildTime() );
        versionInfoDto.setBranch( SubutaiInfo.getBranch() );
        versionInfoDto.setCommitId( SubutaiInfo.getCommitId() );

        try
        {
            ResourceHost host = configManager.getPeerManager().getLocalPeer().getManagementHost();

            versionInfoDto.setP2pVersion( host.getP2pVersion().replace( "p2p Cloud project", "" ).trim() );
            versionInfoDto.setRhVersion( host.getRhVersion().replace( "Subutai version", "" ).trim() );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }

        RestResult<Object> restResult = restClient.post( path, versionInfoDto );

        if ( !restResult.isSuccess() )
        {
            throw new HubManagerException( "Error on sending version info to hub: " + restResult.getError() );
        }
    }
}
