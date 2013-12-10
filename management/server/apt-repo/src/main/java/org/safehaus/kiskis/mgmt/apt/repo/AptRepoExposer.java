/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.apt.repo;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dilshat
 */
public class AptRepoExposer {

    private Server server;
    private String aptRepoPath;
    private int aptRepoPort;

    public void setAptRepoPath(String aptRepoPath) {
        this.aptRepoPath = aptRepoPath;
    }

    public void setAptRepoPort(int aptRepoPort) {
        this.aptRepoPort = aptRepoPort;
    }

    public void init() {
        if (server == null) {
            try {
                server = new Server();
                SelectChannelConnector connector = new SelectChannelConnector();
                connector.setPort(aptRepoPort);
                server.addConnector(connector);

                ResourceHandler resource_handler = new ResourceHandler();
                resource_handler.setDirectoriesListed(true);
                resource_handler.setResourceBase(aptRepoPath);

                HandlerList handlers = new HandlerList();
                handlers.setHandlers(new Handler[]{resource_handler});
                server.setHandler(handlers);

                server.start();
            } catch (Exception ex) {
                Logger.getLogger(AptRepoExposer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void destroy() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception ex) {
                Logger.getLogger(AptRepoExposer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
