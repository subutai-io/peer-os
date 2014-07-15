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

import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.communicationmanager.CommunicationManager;
import org.safehaus.subutai.api.fstracker.Listener;
import org.safehaus.subutai.shared.protocol.Request;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;
import org.safehaus.subutai.shared.protocol.enums.RequestType;
import org.safehaus.subutai.shared.protocol.settings.Common;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;


@Command( scope = "fs-tracker", name = "cli" )
public class ShellCommands extends OsgiCommandSupport {

    private Listener listener;

    private ActiveMQConnectionFactory amqFactory;
    private CommunicationManager communicationManager;
    private CommandRunner commandRunner;

    public void setCommunicationManager( CommunicationManager communicationManager ) {
        this.communicationManager = communicationManager;
    }


    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }


    @Override
    protected Object doExecute() {

//        FSTrackerTest.sayHello();
        //testConnection();

        System.out.println( "communicationManager: " + communicationManager );
        System.out.println( "commmandRunner: " + commandRunner );

        return null;
    }

    private void testConnection() {

        ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory( "failover:tcp://localhost:61616" );

        amqFactory.setCheckForDuplicates( true );

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory( amqFactory );

        pooledConnectionFactory.setMaxConnections( 2 );
        pooledConnectionFactory.start();

        try {
//            setupListener( pooledConnectionFactory );
            sendMessage( pooledConnectionFactory );
        } catch (Exception e) {
            e.printStackTrace();
        }

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

            Destination advisoryDestination = AdvisorySupport.getConnectionAdvisoryTopic();
            MessageConsumer advConsumer = session.createConsumer( advisoryDestination );
            advConsumer.setMessageListener( testMessageListener );
        }
        catch ( JMSException e ) {
            e.printStackTrace();
        }
    }



    public static Request getRequestTemplate( UUID uuid ) {
        return new Request( "SOURCE", RequestType.EXECUTE_REQUEST, // type
                uuid, //                        !! agent uuid
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


}
