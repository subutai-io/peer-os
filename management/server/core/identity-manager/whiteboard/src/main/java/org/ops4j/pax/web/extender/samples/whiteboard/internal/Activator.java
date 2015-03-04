/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.web.extender.samples.whiteboard.internal;


import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.jetty.osgi.boot.OSGiServerConstants;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Activator implements BundleActivator
{

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger( Activator.class );


    public void start( final BundleContext bundleContext ) throws Exception
    {
        String jettyHome = "/root/talas/subutai-2.0.0/etc";
        Server server = new Server();
        server.addConnector( new SslSelectChannelConnector() );
        //server configuration goes here
        String serverName = "jetty-server";
        Dictionary<String, String> serverProps = new Hashtable<>();
        serverProps.put( OSGiServerConstants.MANAGED_JETTY_SERVER_NAME, serverName );
        serverProps.put( OSGiServerConstants.JETTY_PORT, "9763" );
        //        serverProps.put( "jetty.etc.config.urls", "file:" + jettyHome + File.separator + "jetty.xml" );

        //register as an OSGi Service for Jetty to find
        bundleContext.registerService( Server.class.getName(), server, serverProps );
    }


    public void stop( BundleContext bundleContext ) throws Exception
    {
    }
}
