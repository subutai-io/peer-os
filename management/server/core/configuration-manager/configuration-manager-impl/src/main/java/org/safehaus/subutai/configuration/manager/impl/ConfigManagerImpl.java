/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.impl;


import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.impl.utils.CassandraYamlLoader;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.exceptions.ConfigurationException;


/**
 * This is an implementation of LxcManager
 */
public class ConfigManagerImpl implements ConfigManager {


    public BundleContext bcontext;
    private Bundle bundle;


    public void start() {
        bundle = bcontext.getBundle();
    }


    /*private String readFile( InputStream is ) throws IOException {
        java.util.Scanner s = new java.util.Scanner( is ).useDelimiter( "\\A" );
        return s.hasNext() ? s.next() : "";
    }*/


    public void setBcontext( BundleContext bcontext ) {
        this.bcontext = bcontext;
    }


    @Override
    public void injectConfiguration( final Object conf, final String path, final Agent agent ) {


    }


    @Override
    public Config getCassandraConfig() {
        CassandraYamlLoader c = new CassandraYamlLoader();
        Config config = null;
        try {
            InputStream is = bundle.getEntry( "cassandra.2.0.4/cassandra.yaml" ).openStream();
            /*BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                System.out.println( line );
            }*/
            config = c.loadConfig( is );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
        return config;
    }
}
