package org.safehaus.subutai.plugin.flume.impl.handler;

import com.google.common.base.Preconditions;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;

import java.util.Iterator;

/**
 * Created by ebru on 09.11.2014.
 */
public class NodeOperationHandler extends AbstractOperationHandler<FlumeImpl, FlumeConfig> {

    private String clusterName;
    private String hostName;
    private NodeOperationType operationType;

    public NodeOperationHandler( final FlumeImpl manager, final String clusterName, final String hostName,
                                 NodeOperationType operationType )
    {
        super( manager, manager.getCluster( clusterName) );
        this.hostName = hostName;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker()
                .createTrackerOperation( FlumeConfig.PRODUCT_KEY,
                        String.format( "Checking %s cluster...", clusterName ) );
    }

    @Override
    public void run() {
        FlumeConfig config = manager.getCluster(clusterName);
        if (config == null) {
            trackerOperation.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID(config.getEnvironmentId());
        Iterator iterator = environment.getContainers().iterator();
        ContainerHost host = null;
        while (iterator.hasNext()) {
            host = (ContainerHost) iterator.next();
            if (host.getHostname().equals(hostName)) {
                break;
            }
        }

        if (host == null) {
            trackerOperation.addLogFailed(String.format("No Container with ID %s", hostName));
            return;
        }

        try {
            CommandResult result = null;
            switch (operationType) {
                case START:
                    result = host.execute(new RequestBuilder(Commands.make(CommandType.START)));
                    break;
                case STOP:
                    result = host.execute(new RequestBuilder(Commands.make(CommandType.STOP)));
                    break;
                case SERVICE_STATUS:
                    result = host.execute(new RequestBuilder(Commands.make(CommandType.SERVICE_STATUS)));
                    break;
                case STATUS:
                    result = host.execute(new RequestBuilder(Commands.make(CommandType.STATUS)));
                    break;
            }
            logStatusResults(trackerOperation, result);
        } catch (CommandException e) {
            trackerOperation.addLogFailed(String.format("Command failed, %s", e.getMessage()));
        }
    }
    public static void logStatusResults( TrackerOperation po, CommandResult result )
    {
        Preconditions.checkNotNull(result);
        StringBuilder log = new StringBuilder();
        String status = "UNKNOWN";
        if ( result.getExitCode() == 0 )
        {
            status = "Flume is running";
        }
        else if ( result.getExitCode() == 256 )
        {
            status = "Flume is not running";
        }
        else
        {
            status = result.getStdOut();
        }
        log.append( String.format( "%s", status ) );
        po.addLogDone( log.toString() );
    }
}
