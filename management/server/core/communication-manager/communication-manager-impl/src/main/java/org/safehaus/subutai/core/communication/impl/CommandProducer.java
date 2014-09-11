package org.safehaus.subutai.core.communication.impl;


import org.safehaus.subutai.core.communication.api.CommandJson;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.settings.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

//import java.util.logging.Logger;

//import java.util.logging.Logger;


/**
 * This class is used internally by CommunicationManagerImpl for sending requests to agents.
 */
class CommandProducer implements Runnable {

	//    private static final Logger LOG = Logger.getLogger( CommandProducer.class.getName() );
	private static final Logger LOG = LoggerFactory.getLogger(CommandProducer.class.getName());
	private final Request command;
	private final CommunicationManagerImpl communicationManagerImpl;
	private final boolean isBroadcast;


	public CommandProducer(Request command, CommunicationManagerImpl communicationManagerImpl) {
		this(command, communicationManagerImpl, false);
	}


	public CommandProducer(final Request command, final CommunicationManagerImpl communicationManagerImpl,
	                       final boolean isBroadcast) {
		this.command = command;
		this.communicationManagerImpl = communicationManagerImpl;
		this.isBroadcast = isBroadcast;
	}


	/**
	 * Called by executor to send message to agent
	 */
	public void run() {
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		try {
			connection = communicationManagerImpl.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination =
					session.createTopic(isBroadcast ? Common.BROADCAST_TOPIC : command.getUuid().toString());
			producer = session.createProducer(destination);
			producer.setDeliveryMode(communicationManagerImpl.isPersistentMessages() ? DeliveryMode.PERSISTENT :
					DeliveryMode.NON_PERSISTENT);
			producer.setTimeToLive(communicationManagerImpl.getAmqMaxMessageToAgentTtlSec() * 1000);
			String json = CommandJson.getJson(command);

			if (!RequestType.HEARTBEAT_REQUEST.equals(command.getType())) {
//                LOG.log( Level.INFO, "\nSending: {0}", json );
				LOG.info("\nSending: {}", json);
			}

			TextMessage message = session.createTextMessage(json);
			producer.send(message);
		} catch (JMSException e) {
//            LOG.log( Level.SEVERE, "Error in CommandProducer.run", ex );
			LOG.error("Error to send a message: ", e);
		} finally {
			if (producer != null) {
				try {
					producer.close();
				} catch (Exception e) {
				}
			}
			if (session != null) {
				try {
					session.close();
				} catch (Exception e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
