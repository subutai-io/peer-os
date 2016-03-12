package io.subutai.core.karaf.manager.impl;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.core.karaf.manager.api.KarafManager;


/**
 * Manages/Executes Karaf Commands
 */
public class KarafManagerImpl implements KarafManager
{

    private CommandProcessor commandProcessor = null;
    protected ExecutorService executor = SubutaiExecutors.newCachedThreadPool();


    @Override
    public String executeShellCommand( final String commandStr )
    {
        String response;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream( byteArrayOutputStream );
        final CommandSession commandSession = commandProcessor.createSession( System.in, printStream, System.err );
        FutureTask<String> commandFuture = new FutureTask<String>( new Callable<String>()
        {
            public String call()
            {
                try
                {
                    //for ( String command : commands )
                    //{
                        System.err.println( commandStr );
                        commandSession.execute( commandStr );
                    //}
                }
                catch ( Exception e )
                {
                    e.printStackTrace( System.err );
                }
                return byteArrayOutputStream.toString();
            }
        } );

        try
        {
            executor.submit( commandFuture );
            response = commandFuture. get( 5000, TimeUnit.MILLISECONDS );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.err );
            response = "SHELL COMMAND TIMED OUT: ";
        }

        return response;
    }



    /* *************
    */
    public String executeJMXCommand( final String commandStr)
    {
        String result= "No Result";

        try
        {
            HashMap   environment = new HashMap();
            String[]  credentials = new String[] {"admin", "secret"};
            environment.put (JMXConnector.CREDENTIALS, credentials);

            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
            JMXConnector connector = null;
            connector = JMXConnectorFactory.connect( url, environment );
            MBeanServerConnection mbeanServer = connector.getMBeanServerConnection();
            ObjectName systemMBean = new ObjectName("org.apache.karaf:type=bundle,name=root");
            mbeanServer.invoke(systemMBean,commandStr, null, null);
            connector.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
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
