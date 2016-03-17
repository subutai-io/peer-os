package io.subutai.core.hubmanager.impl.environment;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.digest.DigestUtils;

import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.LocalPeer;


public class EnvironmentBuilder
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final List<Helper> helpers = new ArrayList<>();


    public EnvironmentBuilder( LocalPeer localPeer )
    {
        helpers.add( new PekHelper( localPeer ) );
        helpers.add( new VniHelper( localPeer ) );
        helpers.add( new P2PHelper( localPeer ) );
        helpers.add( new TemplateHelper( localPeer ) );
        helpers.add( new ContainerCloneHelper( localPeer ) );
    }


    public void test()
    {
        long vniId = 4100000; // Same value for all peers within env

        String envId = "41e3e4de-2bf9-45e6-98f4-f09d65a86700"; // Should be UUID. Otherwise reserving VNI doesn't work.

        String p2pSubnet = "10.11.41.0"; // Should be free on each peer

        String p2pIp = "10.11.41.1";

        String p2pSharedKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );

        String containerHostname = "41c51c6d-8122-4f4d-8de1-c3dd8914df11";

        String containerName = "Container Name 41";

        String containerIp = "192.168.41.2"; // Starts from 192.168.x.2.

        String templateName = "master";

        ContainerSize containerSize = ContainerSize.TINY;

        // TODO. The DTO comes from Hub
        PeerEnvironmentDto dto = new PeerEnvironmentDto(
                envId, vniId, p2pSubnet, p2pIp, p2pSharedKey,
                containerHostname, containerName, containerIp,
                templateName, containerSize
        );

        EnvironmentBuildResultDto resultDto = build( dto );

        // TODO. The result goes to Hub
        log.info( "{}", resultDto );
    }


    public EnvironmentBuildResultDto build( PeerEnvironmentDto dto )
    {
        try
        {
            for ( Helper helper : helpers )
            {
                // TODO. Check for error results in each helper's execute() and throw exception
                helper.execute( dto );
            }

            return new EnvironmentBuildResultDto( dto.getEnvironmentId(), true, "Environment successfully built." );
        }
        catch ( Exception e )
        {

            log.error( "Error to build environment: ", e );

            return new EnvironmentBuildResultDto( dto.getEnvironmentId(), false, e.getMessage() );
        }
    }

}
