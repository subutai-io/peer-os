package org.safehaus.subutai.impl.fstracker;


import java.util.UUID;
import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.communicationmanager.CommunicationManager;
import org.safehaus.subutai.api.communicationmanager.ResponseListener;
import org.safehaus.subutai.api.fstracker.Listener;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Request;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;
import org.safehaus.subutai.shared.protocol.enums.RequestType;
import org.safehaus.subutai.shared.protocol.enums.ResponseType;
import org.safehaus.subutai.shared.protocol.settings.Common;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import com.google.common.collect.Sets;


@Command( scope = "fs-tracker", name = "cli" )
public class ShellCommands extends OsgiCommandSupport implements ResponseListener {

    private Listener listener;

    private ActiveMQConnectionFactory amqFactory;
    private CommunicationManager communicationManager;
    private CommandRunner commandRunner;
    private AgentManager agentManager;

    public void setCommunicationManager( CommunicationManager communicationManager ) {
        this.communicationManager = communicationManager;
    }


    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }



    protected Object doExecute() {
        try {
//            doAction();
            doExecute3();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    protected Object doExecute3() {


//        FSTrackerTest.sayHello();
        //testConnection();

//        System.out.println( "communicationManager: " + communicationManager );
//        System.out.println( "commmandRunner: " + commandRunner );
//        System.out.println( "agentManager: " + agentManager );

        if ( agentManager.getAgents().isEmpty() ) {
            System.out.println( "ERROR: No agents!" );
            return null;
        }

//        testConnection();

//        org.safehaus.subutai.api.commandrunner.Command command = commandRunner.createCommand(
//                new RequestBuilder( "date" )
//                        .withType( RequestType.INOTIFY_SHOW_REQUEST )
//                        .withTimeout( 60 ),
//                agentManager.getAgents() );


        communicationManager.addListener( this );

        Agent agent = agentManager.getAgentByUUID( UUID.fromString( "2a6dbdcd-daf9-40cb-9271-e6daed300325" ) );

//        communicationManager.sendRequest( getRequest( firstAgent.getUuid() ) );

        org.safehaus.subutai.api.commandrunner.Command command =
//                doCancel( agent )
//                doShow( agent )
                doRequest( agent )
        ;

        commandRunner.runCommandAsync( command );

//        CommandCallback callback = new CommandCallback() {
//            public void onResponse( Response response, AgentResult agentResult,
//                                    org.safehaus.subutai.api.commandrunner.Command command ) {
//                System.out.println( ">> response: " + response );
//
//            }
//        };

//        commandRunner.runCommandAsync( command, callback );
//        AgentResult agentResult = command.getResults().get( firstAgent.getUuid() );
//        System.out.println( "result: " + agentResult );

        return null;
    }

    private org.safehaus.subutai.api.commandrunner.Command doShow( Agent agent ) {
         return commandRunner.createCommand(
             new RequestBuilder( "pwd" )
                     .withType( RequestType.INOTIFY_SHOW_REQUEST )
                     .withTimeout( 60 ),
             Sets.newHashSet( agent ) );
    }

    private org.safehaus.subutai.api.commandrunner.Command doRequest( Agent agent ) {

        String confPoints[] = {
                "/etc/ksks-agent",
                "/etc/subutai/",
//                "/etc/test1",
//                "/opt/inotify-test",
        };

        return commandRunner.createCommand(
                new RequestBuilder( "pwd" )
                        .withType( RequestType.INOTIFY_REQUEST )
                        .withTimeout( 60 )
                        .withConfPoints( confPoints ),
                Sets.newHashSet( agent ) );
    }


    private org.safehaus.subutai.api.commandrunner.Command doCancel( Agent agent ) {

        String confPoints[] = {
            "/etc/ksks-agent",
        };

        return commandRunner.createCommand(
                new RequestBuilder( "pwd" )
                        .withType( RequestType.INOTIFY_CANCEL_REQUEST )
                        .withTimeout( 60 )
                        .withConfPoints( confPoints ),
                Sets.newHashSet( agent ) );
    }


//    Modify_file, Delete_File, Create_File, Create_Folder, Delete_Folder

    public void onResponse( Response response ) {
        if ( response == null ) {
            return;
        }

        if ( response.getType() == ResponseType.INOTIFY_RESPONSE
                || response.getType() == ResponseType.INOTIFY_SHOW_RESPONSE ) {
            System.out.println( "response: " + response );
        }
    }





    public static Request getRequest( UUID agentUuid ) {
        return new Request( "FS_TRACKER",
                RequestType.INOTIFY_SHOW_REQUEST, // type
                agentUuid, //                        !! agent uuid
                UUID.randomUUID(), //                        !! task uuid
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                null, 30 ); //
    }


    protected Object doExecute_() {

//        testConnection();

        return null;
    }


    private void doAction() {

//        ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory( "failover:tcp://localhost:61616" );
//        amqFactory.setCheckForDuplicates( true );
//        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory( amqFactory );
//        pooledConnectionFactory.setMaxConnections( 2 );
//        pooledConnectionFactory.start();

        try {
//            setupListener( pooledConnectionFactory );
//            setupListener();
//            sendMessage( pooledConnectionFactory );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private void setupListener() {
//        try {
//            Connection connection = pooledConnectionFactory.createConnection();
//            // Do not close this connection otherwise server listener will be closed
//            connection.start();
//            Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
////            Destination adminQueue = session.createTopic( "BROADCAST_TOPIC" );
//            Destination adminQueue = session.createTopic( "INOTIFY_TOPIC" );
//
//            MessageConsumer consumer = session.createConsumer( adminQueue );
//
//            TestMessageListener testMessageListener = new TestMessageListener();
//            consumer.setMessageListener( testMessageListener );
//
////            Destination advisoryDestination = AdvisorySupport.getConnectionAdvisoryTopic();
////            MessageConsumer advConsumer = session.createConsumer( advisoryDestination );
////            advConsumer.setMessageListener( testMessageListener );
//        }
//        catch ( JMSException e ) {
//            e.printStackTrace();
//        }
    }


    public void sendMessage(PooledConnectionFactory pooledConnectionFactory) {


        String uuid = "cae85e27-6b0f-4a29-ab8f-4cbeba1dc07d";
//        String uuid = "95d37e69-c118-4706-b65b-752a6aca9ad7";
//        String uuid = "b4dcfcbe-7838-4d39-b08d-63008c4d8ab4";

            Connection connection = null;
            Session session = null;
            MessageProducer producer = null;

            try {
                connection = pooledConnectionFactory.createConnection();
                connection.start();
                session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
//                Destination destination = session.createTopic( "BROADCAST_TOPIC" );
//                Destination destination = session.createTopic( "SERVICE_TOPIC" );
                Destination destination = session.createTopic( uuid );

                producer = session.createProducer( destination );
                producer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
                producer.setTimeToLive( 1000 );


                String json = "";

                /*
                json = "{"
                  + "\"command\": { "
                  + "\"source\": \"FS_TRACKER\", "
                  + "\"type\": \"EXECUTE_REQUEST\", "
                  + "\"uuid\": \"95d37e69-c118-4706-b65b-752a6aca9ad7\", "
                  + "\"taskUuid\": \"bcf198b8-05ca-11e4-b8fa-d1dcc525e0e4\", "
                  + "\"requestSequenceNumber\": 1, "
                  + "\"workingDirectory\": \"/\", "
                  + "\"program\": \"pwd\", "
                  + "\"stdOut\": \"RETURN\", "
                  + "\"stdErr\": \"RETURN\", "
                  + "\"runAs\": \"root\", "
                  + "\"pid\": 0, "
                  + "\"timeout\": 30 "
                  + "}"
                  + "}";
                  */

                json = "{"
                        + "\"command\": { "
                        + "\"source\": \"FS_TRACKER\", "
                        + "\"type\": \"INOTIFY_SHOW_REQUEST\", "
                        + "\"uuid\": \"" + uuid + "\", "
                        + "\"taskUuid\": \"bcf198b8-05ca-11e4-b8fa-d1dcc525e0e3\" "
                        + "}"
                        + "}";

/*

                json = "{"
                      + "\"command\": { "
                      + "\"source\": \"FS_TRACKER\", "
                      + "\"type\": \"INOTIFY_REQUEST\", "
                      + "\"uuid\": \"" + uuid + "\", "
                      + "\"taskUuid\": \"bcf198b8-05ca-11e4-b8fa-d1dcc525e0e4\", "
//                      + "\"confPoints\":[\"/etc/ksks-agent\"] "
                      + "\"confPoints\":[\"/opt/inotify-test\"] "
                      + "}"
                      + "}";
*/

/*
                json = "{"
                      + "\"command\": { "
                      + "\"source\": \"FS_TRACKER\", "
                      + "\"type\": \"INOTIFY_CANCEL_REQUEST\", "
                      + "\"uuid\": \"" + uuid + "\", "
                      + "\"taskUuid\": \"bcf198b8-05ca-11e4-b8fa-d1dcc525e0e5\", "
                      + "\"confPoints\":[\"/opt\"] "
                      + "}"
                      + "}";*/

                System.out.println( "json: " + json );

                TextMessage message = session.createTextMessage( json );
                producer.send( message );
            }
            catch ( JMSException e ) {
                e.printStackTrace();
            }
            finally {
                try {
                    if ( producer != null ) {
                        producer.close();
                    }
                    if ( session != null ) {
                        session.close();
                    }
                    if ( connection != null ) {
                        connection.close();
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
    }



    private void setupListener( PooledConnectionFactory pooledConnectionFactory ) {
        try {
            Connection connection = pooledConnectionFactory.createConnection();
            // Do not close this connection otherwise server listener will be closed
            connection.start();
            Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
//            Destination adminQueue = session.createTopic( "BROADCAST_TOPIC" );
            Destination adminQueue = session.createTopic( "INOTIFY_TOPIC" );

            MessageConsumer consumer = session.createConsumer( adminQueue );

            TestMessageListener testMessageListener = new TestMessageListener();
            consumer.setMessageListener( testMessageListener );

//            Destination advisoryDestination = AdvisorySupport.getConnectionAdvisoryTopic();
//            MessageConsumer advConsumer = session.createConsumer( advisoryDestination );
//            advConsumer.setMessageListener( testMessageListener );
        }
        catch ( JMSException e ) {
            e.printStackTrace();
        }
    }


}
