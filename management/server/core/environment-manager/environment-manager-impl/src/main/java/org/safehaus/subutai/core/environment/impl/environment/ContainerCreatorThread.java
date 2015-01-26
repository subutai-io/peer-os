package org.safehaus.subutai.core.environment.impl.environment;


import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/5/14.
 */
public class ContainerCreatorThread extends Observable implements Runnable
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerCreatorThread.class.getName() );
    private CloneContainersMessage message;
    private PeerManager peerManager;
    private UUID environmentId;
    private DataService dataService;


    public ContainerCreatorThread( final EnvironmentBuilderImpl environmentBuilder, final UUID environmentId,
                                   final CloneContainersMessage message, final PeerManager peerManager,
                                   final DataService dataService )
    {
        this.environmentId = environmentId;
        this.message = message;
        this.peerManager = peerManager;
        this.dataService = dataService;
    }


    @Override
    public void run()
    {
        try
        {
            Set<HostInfoModel> hostInfos = peerManager.getPeer( message.getTargetPeerId() ).
                    scheduleCloneContainers( message.getTargetPeerId(), message.getTemplates(),
                            message.getNumberOfNodes(), message.getStrategy().getStrategyId(),
                            message.getStrategy().getCriteriaAsList() );
            LOG.info( String.format( "Received %d containers for environment %s", hostInfos.size(), environmentId ) );
            Template template = message.getTemplates().get( message.getTemplates().size() - 1 );
            setChanged();
            Set<EnvironmentContainerImpl> containers = new HashSet<>();
            for ( HostInfoModel hostInfo : hostInfos )
            {
                EnvironmentContainerImpl container =
                        new EnvironmentContainerImpl( message.getTargetPeerId(), message.getNodeGroupName(), hostInfo );
                container.setCreatorPeerId( peerManager.getLocalPeer().getId().toString() );
                container.setPeer( peerManager.getPeer( message.getTargetPeerId() ) );
                container.setDataService( dataService );
                container.setTemplateName( template.getTemplateName() );
                containers.add( container );
            }
            notifyObservers( containers );
        }
        catch ( Exception e )
        {
            notifyObservers( e );
            LOG.error( e.getMessage(), e );
        }
    }

    //    @Override
    //    public void run()
    //    {
    //        try
    //        {
    //            Set<ContainerHost> containers = peerManager.getPeer( message.getTargetPeerId() ).
    //                    createContainers( message.getTargetPeerId(), environmentId, message.getTemplates(),
    //                            message.getNumberOfNodes(), message.getStrategy().getStrategyId(),
    //                            message.getStrategy().getCriteriaAsList(), message.getNodeGroupName() );
    //            LOG.info( String.format( "Received %d containers for environment %s", containers.size(),
    // environmentId ) );
    //            setChanged();
    //            notifyObservers( containers );
    //        }
    //        catch ( Exception e )
    //        {
    //            notifyObservers( e );
    //            LOG.error( e.getMessage(), e );
    //        }
    //    }
}
