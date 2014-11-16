package org.safehaus.subutai.wol.impl;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.*;
import org.safehaus.subutai.wol.api.WolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by emin on 14/11/14.
 */
public class WolImpl implements WolManager {
    private static final Logger LOG = LoggerFactory.getLogger(WolImpl.class.getName());
    private PeerManager peerManager;
    private ManagementHost managementHost;

    public WolImpl()
    {
        try {
            managementHost = peerManager.getLocalPeer().getManagementHost();
        }
        catch(PeerException e){
            LOG.error(e.getMessage(), e);
        }

    }

    public String sendMagicPackagebyMacId(String macId) {

       return managementHost.getHostname().toString();
       /* try {
            return getLocalPeer().getManagementHost().execute(new RequestBuilder("ls /root")).getStdOut();
        } catch (PeerException e) {
            LOG.error(e.getMessage(), e);
        } catch (CommandException e) {
            LOG.error(e.getMessage(), e);
        }
        */
    }



    @Override
    public Boolean sendMagicPackagebyList(ArrayList<String> macList){ return true; }

    @Override
    public String getWolName() {
        return "hello!";
    }


    @Override
    public String helloWol( final String name ) {
        return "Hello mydear " + name + "!";
    }
}
