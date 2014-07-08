package org.safehaus.subutai.impl.fstracker;


import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.safehaus.subutai.api.fstracker.Listener;
import org.safehaus.subutai.shared.protocol.enums.RequestType;
import org.safehaus.subutai.shared.protocol.settings.Common;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;


@Command( scope = "fs-tracker", name = "cli" )
public class ShellCommands extends OsgiCommandSupport {

    private Listener listener;

    private ActiveMQConnectionFactory amqFactory;


    @Override
    protected Object doExecute() {

//        FSTrackerTest.sayHello();
        testConnection();

        return null;
    }

    private void testConnection() {
        System.out.println( "Test connect [start]" );

        ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory( "failover:tcp://localhost:61616" );

        amqFactory.setCheckForDuplicates( true );

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory( amqFactory );

        pooledConnectionFactory.setMaxConnections( 2 );
        pooledConnectionFactory.start();

        try {
            sendMessage( pooledConnectionFactory );
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println( "Testing connect [end]" );
    }

    public void sendMessage(PooledConnectionFactory pooledConnectionFactory) {

            Connection connection = null;
            Session session = null;
            MessageProducer producer = null;

            try {
                connection = pooledConnectionFactory.createConnection();
                connection.start();
                session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
                Destination destination = session.createTopic( "BROADCAST_TOPIC" );
                                                                           //: command.getUuid().toString() );

                producer = session.createProducer( destination );
                producer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
                producer.setTimeToLive( 1000 );

//                String json = CommandJson.getJson( command );

//                if ( !RequestType.HEARTBEAT_REQUEST.equals( command.getType() ) ) {
//                    System.out.println( "\nSending: {0}" + json );
//                }

                String json = "{"
                  + "\"command\": { "
                  + "\"source\": \"COMMAND-RUNNER\", "
                  + "\"type\": \"EXECUTE_REQUEST\", "
                  + "\"uuid\": \"499f4b6e-36f7-4588-92a2-13d9008de628\", "
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
}
