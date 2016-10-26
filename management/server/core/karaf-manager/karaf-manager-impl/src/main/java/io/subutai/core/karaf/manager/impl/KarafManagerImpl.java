package io.subutai.core.karaf.manager.impl;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;

import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.security.utils.SafeCloseUtil;
import io.subutai.common.settings.Common;
import io.subutai.core.karaf.manager.api.KarafManager;


/**
 * Manages/Executes Karaf Commands
 */
@PermitAll
public class KarafManagerImpl implements KarafManager
{

    private static final Logger LOG = LoggerFactory.getLogger( KarafManagerImpl.class.getName() );


    private CommandProcessor commandProcessor = null;
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();


    /* ***********************************************
     */
    @RolesAllowed( {
            "Karaf-Server-Administration|Write", "Karaf-Server-Administration|Read", "System-Management|Write",
            "System-Management|Update"
    } )
    @Override
    public String executeShellCommand( final String commandStr )
    {
        String response = "";
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream( byteArrayOutputStream );
        final CommandSession commandSession = commandProcessor.createSession( System.in, printStream, System.err );


        //************************************************
        FutureTask<String> commandFuture = new FutureTask<>( new Callable<String>()
        {
            public String call()
            {
                try
                {
                    commandSession.execute( commandStr );
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage() );
                }

                printStream.flush();
                return byteArrayOutputStream.toString();
            }
        } );
        //************************************************

        try
        {
            executor.submit( commandFuture );

            do
            {
                response += commandFuture.get( Common.DEFAULT_EXECUTOR_REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS );
            }
            while ( !commandFuture.isDone() );
        }
        catch ( Exception e )
        {
            response += "Command Timeout: ";

            response += byteArrayOutputStream.toString();
        }

        return response;
    }


    /* ***********************************************
     */
    @RolesAllowed( {
            "Karaf-Server-Administration|Write", "Karaf-Server-Administration|Read", "System-Management|Write",
            "System-Management|Update"
    } )
    @Override
    public String executeJMXCommand( final String commandStr )
    {
        String result = "No Result";

        JMXConnector connector = null;
        try
        {
            HashMap<String, String[]> environment = new HashMap<>();
            String[] credentials = new String[] { "admin", "secret" };
            environment.put( JMXConnector.CREDENTIALS, credentials );

            JMXServiceURL url = new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root" );
            connector = JMXConnectorFactory.connect( url, environment );
            MBeanServerConnection mbeanServer = connector.getMBeanServerConnection();
            ObjectName systemMBean = new ObjectName( "org.apache.karaf:type=bundle,name=root" );
            mbeanServer.invoke( systemMBean, commandStr, null, null );
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage() );
        }
        finally
        {
            SafeCloseUtil.close( connector );
        }

        return result;
    }


    public CommandProcessor getCommandProcessor()
    {
        return commandProcessor;
    }


    public void setCommandProcessor( final CommandProcessor commandProcessor )
    {
        this.commandProcessor = commandProcessor;
    }
}
